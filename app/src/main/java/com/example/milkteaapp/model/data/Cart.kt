package com.example.milkteaapp.model.data

/**
 * Data class đại diện cho một dòng sản phẩm trong giỏ hàng.
 *
 * Giỏ hàng được quản lý hoàn toàn ở phía client (in-memory + local cache),
 * chỉ được đẩy lên Firestore khi khách hàng xác nhận đặt hàng → tạo [Order].
 *
 * @param cartItemId      ID dòng giỏ hàng (UUID sinh ở client, dùng để remove/update)
 * @param productId       ID sản phẩm liên kết với [Product]
 * @param productName     Tên sản phẩm (snapshot tại thời điểm thêm vào giỏ)
 * @param productImageUrl Ảnh sản phẩm (snapshot)
 * @param size            Kích cỡ đã chọn
 * @param sugarLevel      Mức đường đã chọn
 * @param iceLevel        Mức đá đã chọn
 * @param selectedToppings Danh sách topping đã chọn (snapshot)
 * @param unitPrice       Giá một ly (đã bao gồm kích cỡ + topping)
 * @param quantity        Số lượng
 * @param note            Ghi chú riêng của khách (tuỳ chọn)
 */
data class CartItem(
    val cartItemId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String? = null,
    val size: DrinkSize = DrinkSize.MEDIUM,
    val sugarLevel: SugarLevel = SugarLevel.ONE_HUNDRED,
    val iceLevel: IceLevel = IceLevel.NORMAL,
    val selectedToppings: List<Topping> = emptyList(),
    val unitPrice: Long = 0L,
    val quantity: Int = 1,
    val note: String = "",
    val price: Long = 0L,
    val imageUrl: String? = null
) {
    val subtotal: Long get() = unitPrice * quantity

    val optionSummary: String
        get() = buildString {
            append(size.label)
            append(" | Đường ${sugarLevel.label}")
            append(" | ${iceLevel.label}")
            if (selectedToppings.isNotEmpty()) {
                append(" | +${selectedToppings.joinToString(", ") { it.name }}")
            }
        }

    fun withQuantity(newQty: Int): CartItem = copy(quantity = newQty.coerceAtLeast(1))
}

// ─────────────────────────────────────────────────────────
// Extension helpers
// ─────────────────────────────────────────────────────────

fun List<CartItem>.totalAmount(): Long = sumOf { it.subtotal }

fun List<CartItem>.totalQuantity(): Int = sumOf { it.quantity }