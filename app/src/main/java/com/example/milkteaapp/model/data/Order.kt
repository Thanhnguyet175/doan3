package com.example.milkteaapp.model.data

import com.google.firebase.Timestamp

// ─────────────────────────────────────────────────────────
// ORDER STATUS
// ─────────────────────────────────────────────────────────

/**
 * Vòng đời của một đơn hàng.
 *
 *  PENDING ──► CONFIRMED ──► BREWING ──► READY ──► COMPLETED
 *                                                       │
 *           CANCELLED ◄──────────────────────────────── ┘ (bất kỳ bước nào)
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
 *
 * @param productId       ID sản phẩm gốc (để truy vết)
 * @param productName     Tên tại thời điểm đặt
 * @param productImageUrl Ảnh tại thời điểm đặt
 * @param size            Kích cỡ đã chọn
 * @param sugarLevel      Mức đường
 * @param iceLevel        Mức đá
 * @param toppings        Danh sách topping đã chọn (snapshot)
 * @param unitPrice       Giá một ly (đã bao gồm topping)
 * @param quantity        Số lượng
 * @param note            Ghi chú riêng
 */
data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String? = null,
    val size: DrinkSize = DrinkSize.MEDIUM,
    val sugarLevel: SugarLevel = SugarLevel.FULL,
    val iceLevel: IceLevel = IceLevel.NORMAL,
    val toppings: List<Topping> = emptyList(),
    val unitPrice: Long = 0L,
    val quantity: Int = 1,
    val note: String = ""
) {
    val subtotal: Long get() = unitPrice * quantity

    fun toMap(): Map<String, Any?> = mapOf(
        "productId"       to productId,
        "productName"     to productName,
        "productImageUrl" to productImageUrl,
        "size"            to size.name,
        "sugarLevel"      to sugarLevel.name,
        "iceLevel"        to iceLevel.name,
        "toppings"        to toppings.map { it.toMap() },
        "unitPrice"       to unitPrice,
        "quantity"        to quantity,
        "note"            to note
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): OrderItem {
            val toppingList = (map["toppings"] as? List<Map<String, Any?>>)
                ?.map { Topping.fromMap(it) } ?: emptyList()

            return OrderItem(
                productId       = map["productId"] as? String ?: "",
                productName     = map["productName"] as? String ?: "",
                productImageUrl = map["productImageUrl"] as? String,
                size            = runCatching { DrinkSize.valueOf(map["size"] as? String ?: "") }
                    .getOrDefault(DrinkSize.MEDIUM),
                sugarLevel      = runCatching { SugarLevel.valueOf(map["sugarLevel"] as? String ?: "") }
                    .getOrDefault(SugarLevel.FULL),
                iceLevel        = runCatching { IceLevel.valueOf(map["iceLevel"] as? String ?: "") }
                    .getOrDefault(IceLevel.NORMAL),
                toppings        = toppingList,
                unitPrice       = map["unitPrice"] as? Long ?: 0L,
                quantity        = (map["quantity"] as? Long)?.toInt() ?: 1,
                note            = map["note"] as? String ?: ""
            )
        }

        /** Chuyển từ CartItem sang OrderItem khi khách xác nhận đặt hàng */
        fun fromCartItem(cartItem: CartItem): OrderItem = OrderItem(
            productId       = cartItem.productId,
            productName     = cartItem.productName,
            productImageUrl = cartItem.productImageUrl,
            size            = cartItem.size,
            sugarLevel      = cartItem.sugarLevel,
            iceLevel        = cartItem.iceLevel,
            toppings        = cartItem.selectedToppings,
            unitPrice       = cartItem.unitPrice,
            quantity        = cartItem.quantity,
            note            = cartItem.note
        )
    }
}

// ─────────────────────────────────────────────────────────
// ORDER
// ─────────────────────────────────────────────────────────

/**
 * Data class đại diện cho một đơn hàng hoàn chỉnh.
 *
 * @param id              ID đơn hàng (document ID Firestore, vd: "NL-6842")
 * @param customerId      UID khách đặt hàng
 * @param customerName    Tên khách (snapshot)
 * @param customerPhone   SĐT khách (snapshot)
 * @param items           Danh sách món trong đơn
 * @param totalAmount     Tổng tiền (VNĐ, đã trừ giảm giá nếu có)
 * @param discountAmount  Số tiền được giảm (từ promotion)
 * @param promotionId     ID khuyến mãi áp dụng (tuỳ chọn)
 * @param status          Trạng thái đơn hàng
 * @param paymentMethod   Phương thức thanh toán ("CASH", "MOMO", "ZALOPAY"…)
 * @param isPaid          Đã thanh toán chưa
 * @param deliveryAddress Địa chỉ giao hàng (null = mua tại quán)
 * @param note            Ghi chú chung của đơn
 * @param assignedStaffId UID nhân viên đang xử lý (tuỳ chọn)
 * @param createdAt       Thời điểm đặt hàng
 * @param updatedAt       Thời điểm cập nhật trạng thái gần nhất
 */
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
    val isPaid: Boolean = false,
    val deliveryAddress: String? = null,
    val note: String = "",
    val assignedStaffId: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /** Số món trong đơn (tổng quantity) */
    val totalItemCount: Int get() = items.sumOf { it.quantity }

    /** Số tiền thực trả sau giảm giá */
    val finalAmount: Long get() = (totalAmount - discountAmount).coerceAtLeast(0L)

    fun toMap(): Map<String, Any?> = mapOf(
        "id"              to id,
        "customerId"      to customerId,
        "customerName"    to customerName,
        "customerPhone"   to customerPhone,
        "items"           to items.map { it.toMap() },
        "totalAmount"     to totalAmount,
        "discountAmount"  to discountAmount,
        "promotionId"     to promotionId,
        "status"          to status.name,
        "paymentMethod"   to paymentMethod,
        "isPaid"          to isPaid,
        "deliveryAddress" to deliveryAddress,
        "note"            to note,
        "assignedStaffId" to assignedStaffId,
        "createdAt"       to createdAt,
        "updatedAt"       to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): Order {
            val itemList = (map["items"] as? List<Map<String, Any?>>)
                ?.map { OrderItem.fromMap(it) } ?: emptyList()

            return Order(
                id              = map["id"] as? String ?: "",
                customerId      = map["customerId"] as? String ?: "",
                customerName    = map["customerName"] as? String ?: "",
                customerPhone   = map["customerPhone"] as? String ?: "",
                items           = itemList,
                totalAmount     = map["totalAmount"] as? Long ?: 0L,
                discountAmount  = map["discountAmount"] as? Long ?: 0L,
                promotionId     = map["promotionId"] as? String,
                status          = runCatching { OrderStatus.valueOf(map["status"] as? String ?: "") }
                    .getOrDefault(OrderStatus.PENDING),
                paymentMethod   = map["paymentMethod"] as? String ?: "CASH",
                isPaid          = map["isPaid"] as? Boolean ?: false,
                deliveryAddress = map["deliveryAddress"] as? String,
                note            = map["note"] as? String ?: "",
                assignedStaffId = map["assignedStaffId"] as? String,
                createdAt       = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt       = map["updatedAt"] as? Timestamp ?: Timestamp.now()
            )
        }

        /**
         * Tạo đơn hàng mới từ giỏ hàng của khách.
         * [orderId] nên được sinh trước (vd: "NL-" + random 4 số) và
         * truyền vào để đồng bộ với document ID trên Firestore.
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