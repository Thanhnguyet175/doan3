package com.example.milkteaapp.model.data

import com.google.firebase.Timestamp

// ─────────────────────────────────────────────────────────
// ORDER STATUS
// ─────────────────────────────────────────────────────────

/**
 * Vòng đời của một đơn hàng.
 *
 * PENDING ──► CONFIRMED ──► BREWING ──► READY ──► COMPLETED
 * │
 * CANCELLED ◄──────────────────────────────── ┘ (bất kỳ bước nào)
 *
 * DELAYED: trạng thái đặc biệt khi nhân viên đánh dấu đơn bị trễ.
 */
enum class OrderStatus(val label: String, val colorHex: String) {
    PENDING("Chờ xác nhận", "#B5A99A"),
    CONFIRMED("Đã xác nhận", "#4A7C59"),
    BREWING("Đang pha chế", "#3B82F6"),
    READY("Sẵn sàng lấy", "#F59E0B"),
    COMPLETED("Hoàn thành", "#22C55E"),
    CANCELLED("Đã huỷ", "#EF4444"),
    DELAYED("Bị trễ", "#F97316")
}

// ─────────────────────────────────────────────────────────
// ORDER ITEM (snapshot tại thời điểm đặt)
// ─────────────────────────────────────────────────────────

/**
 * Một dòng sản phẩm trong đơn hàng đã đặt.
 * Đây là SNAPSHOT – không thay đổi dù sản phẩm gốc bị sửa/xoá sau này.
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String? = null,
    val size: DrinkSize = DrinkSize.MEDIUM,
    val sugarLevel: SugarLevel = SugarLevel.ONE_HUNDRED, // 🟢 ĐÃ FIX: Đổi sang ONE_HUNDRED đồng bộ với Enum mới
    val iceLevel: IceLevel = IceLevel.NORMAL,
    val selectedToppings: List<Topping> = emptyList(),
    val unitPrice: Long = 0L,
    val quantity: Int = 1,
    val note: String = ""
) {
    val subtotal: Long
        get() = unitPrice * quantity

    companion object {
        fun fromCartItem(cartItem: CartItem): OrderItem {
            return OrderItem(
                productId        = cartItem.productId,
                productName      = cartItem.productName,
                productImageUrl  = cartItem.productImageUrl,
                size             = cartItem.size,
                sugarLevel       = cartItem.sugarLevel,
                iceLevel         = cartItem.iceLevel,
                selectedToppings = cartItem.selectedToppings,
                unitPrice        = cartItem.unitPrice,
                quantity         = cartItem.quantity,
                note             = cartItem.note
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// ORDER DOMAIN MODEL
// ─────────────────────────────────────────────────────────

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Long = 0L,
    val discountAmount: Long = 0L,
    val promotionId: String? = null,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: String = "CASH",
    val deliveryAddress: String? = null,
    val note: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    val finalAmount: Long
        get() = (totalAmount - discountAmount).coerceAtLeast(0L)

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): Order {
            val itemsMapList = map["items"] as? List<Map<String, Any?>> ?: emptyList()
            val orderItems = itemsMapList.map { itemMap ->
                val sizeStr = itemMap["size"] as? String ?: "MEDIUM"
                val sugarStr = itemMap["sugarLevel"] as? String ?: "ONE_HUNDRED" // 🟢 ĐÃ FIX: Đổi sang ONE_HUNDRED
                val iceStr = itemMap["iceLevel"] as? String ?: "NORMAL"

                val toppingMaps = itemMap["selectedToppings"] as? List<Map<String, Any?>> ?: emptyList()
                val toppings = toppingMaps.map { tMap ->
                    Topping(
                        id          = tMap["id"] as? String ?: "",
                        name        = tMap["name"] as? String ?: "",
                        price       = (tMap["price"] as? Number)?.toLong() ?: 0L,
                        isAvailable = tMap["isAvailable"] as? Boolean ?: true
                    )
                }

                OrderItem(
                    productId        = itemMap["productId"] as? String ?: "",
                    productName      = itemMap["productName"] as? String ?: "",
                    productImageUrl  = itemMap["productImageUrl"] as? String,
                    size             = try { DrinkSize.valueOf(sizeStr) } catch(e: Exception) { DrinkSize.MEDIUM },
                    sugarLevel       = try { SugarLevel.valueOf(sugarStr) } catch(e: Exception) { SugarLevel.ONE_HUNDRED }, // 🟢 ĐÃ FIX: Đổi sang ONE_HUNDRED
                    iceLevel         = try { IceLevel.valueOf(iceStr) } catch(e: Exception) { IceLevel.NORMAL },
                    selectedToppings = toppings,
                    unitPrice        = (itemMap["unitPrice"] as? Number)?.toLong() ?: 0L,
                    quantity         = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                    note             = itemMap["note"] as? String ?: ""
                )
            }

            val statusStr = map["status"] as? String ?: "PENDING"
            val orderStatus = try { OrderStatus.valueOf(statusStr) } catch(e: Exception) { OrderStatus.PENDING }

            return Order(
                id              = id,
                customerId      = map["customerId"] as? String ?: "",
                customerName    = map["customerName"] as? String ?: "",
                customerPhone   = map["customerPhone"] as? String ?: "",
                items           = orderItems,
                totalAmount     = (map["totalAmount"] as? Number)?.toLong() ?: 0L,
                discountAmount  = (map["discountAmount"] as? Number)?.toLong() ?: 0L,
                promotionId     = map["promotionId"] as? String,
                status          = orderStatus,
                paymentMethod   = map["paymentMethod"] as? String ?: "CASH",
                deliveryAddress = map["deliveryAddress"] as? String,
                note            = map["note"] as? String ?: "",
                createdAt       = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt       = map["updatedAt"] as? Timestamp ?: Timestamp.now()
            )
        }

        /**
         * Tạo đơn hàng mới từ giỏ hàng của khách.
         */
        fun fromCart(
            orderId: String,
            customer: User,
            cartItems: List<CartItem>,
            promotionId: String? = null,
            discountAmount: Long = 0L,
            paymentMethod: String = "CASH",
            deliveryAddress: String? = null,
            note: String = ""
        ): Order {
            val orderItems = cartItems.map { OrderItem.fromCartItem(it) }
            val total = orderItems.sumOf { it.subtotal }
            return Order(
                id              = orderId,
                customerId      = customer.uid,
                customerName    = customer.fullName,
                customerPhone   = customer.phoneNumber ?: "",
                items           = orderItems,
                totalAmount     = total,
                discountAmount  = discountAmount,
                promotionId     = promotionId,
                status          = OrderStatus.PENDING,
                paymentMethod   = paymentMethod,
                deliveryAddress = deliveryAddress,
                note            = note
            )
        }
    }
}