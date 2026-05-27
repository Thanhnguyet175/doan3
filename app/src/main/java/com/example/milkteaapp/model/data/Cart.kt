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
    val sugarLevel: SugarLevel = SugarLevel.FULL,
    val iceLevel: IceLevel = IceLevel.NORMAL,
    val selectedToppings: List<Topping> = emptyList(),
    val unitPrice: Long = 0L,
    val quantity: Int = 1,
    val note: String = ""
) {
    /** Tổng tiền của dòng này = đơn giá × số lượng */
    val subtotal: Long get() = unitPrice * quantity

    /** Mô tả ngắn gọn các tuỳ chọn để hiển thị dưới tên sản phẩm */
    val optionSummary: String
        get() = buildString {
            append(size.label)
            append(" | Đường ${sugarLevel.label}")
            append(" | ${iceLevel.label}")
            if (selectedToppings.isNotEmpty()) {
                append(" | +${selectedToppings.joinToString(", ") { it.name }}")
            }
        }

    /**
     * Tạo bản sao với số lượng mới.
     * Dùng trong CartViewModel khi user nhấn +/-.
     */
    fun withQuantity(newQty: Int): CartItem = copy(quantity = newQty.coerceAtLeast(1))
}

// ─────────────────────────────────────────────────────────
// Extension helpers
// ─────────────────────────────────────────────────────────

/** Tổng tiền của toàn bộ giỏ hàng */
fun List<CartItem>.totalAmount(): Long = sumOf { it.subtotal }

/** Tổng số lượng món trong giỏ (dùng cho badge icon) */
fun List<CartItem>.totalQuantity(): Int = sumOf { it.quantity }