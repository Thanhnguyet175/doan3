package com.example.milkteaapp.model.data

/**
 * Data class đại diện cho một danh mục sản phẩm.
 * Ví dụ: Trà Sữa, Trà Trái Cây, Cà Phê, Ăn Vặt
 *
 * @param id          ID danh mục (document ID trên Firestore)
 * @param name        Tên hiển thị (vd: "Trà Sữa")
 * @param iconUrl     URL icon/hình ảnh đại diện danh mục (tuỳ chọn)
 * @param sortOrder   Thứ tự hiển thị trên màn hình (số nhỏ hơn → hiện trước)
 * @param isVisible   Ẩn/hiện danh mục trên app khách hàng
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val iconUrl: String? = null,
    val sortOrder: Int = 0,
    val isVisible: Boolean = true
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id"        to id,
        "name"      to name,
        "iconUrl"   to iconUrl,
        "sortOrder" to sortOrder,
        "isVisible" to isVisible
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Category = Category(
            id        = map["id"] as? String ?: "",
            name      = map["name"] as? String ?: "",
            iconUrl   = map["iconUrl"] as? String,
            sortOrder = (map["sortOrder"] as? Long)?.toInt() ?: 0,
            isVisible = map["isVisible"] as? Boolean ?: true
        )

        /** Danh mục mặc định khởi tạo lần đầu */
        val defaults = listOf(
            Category(id = "tra_sua",      name = "Trà Sữa",      sortOrder = 0),
            Category(id = "tra_trai_cay", name = "Trà Trái Cây", sortOrder = 1),
            Category(id = "ca_phe",       name = "Cà Phê",       sortOrder = 2),
            Category(id = "an_vat",       name = "Ăn Vặt",       sortOrder = 3)
        )
    }
}