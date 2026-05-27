package com.example.milkteaapp.model.data

/**
 * Data class đại diện cho một loại topping (thêm vào sản phẩm).
 * Ví dụ: Trân châu đen, Thạch, Pudding, Kem mặn…
 *
 * @param id          ID topping (document ID trên Firestore)
 * @param name        Tên topping (vd: "Trân Châu Đen")
 * @param price       Giá cộng thêm khi chọn topping này (VNĐ)
 * @param imageUrl    Ảnh minh hoạ topping (tuỳ chọn)
 * @param isAvailable Topping có đang còn nguyên liệu / bán không
 */
data class Topping(
    val id: String = "",
    val name: String = "",
    val price: Long = 0L,           // đơn vị VNĐ, dùng Long tránh overflow
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id"          to id,
        "name"        to name,
        "price"       to price,
        "imageUrl"    to imageUrl,
        "isAvailable" to isAvailable
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Topping = Topping(
            id          = map["id"] as? String ?: "",
            name        = map["name"] as? String ?: "",
            price       = map["price"] as? Long ?: 0L,
            imageUrl    = map["imageUrl"] as? String,
            isAvailable = map["isAvailable"] as? Boolean ?: true
        )
    }
}