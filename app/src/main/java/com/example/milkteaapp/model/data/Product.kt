package com.example.milkteaapp.model.data

/**
 * Enum mức độ đường (sugar level) khách hàng có thể chọn.
 */
enum class SugarLevel(val label: String) {
    ZERO("0%"),
    THIRTY("30%"),
    FIFTY("50%"),
    SEVENTY("70%"),
    FULL("100%")
}

/**
 * Enum mức độ đá (ice level) khách hàng có thể chọn.
 */
enum class IceLevel(val label: String) {
    NO_ICE("Không đá"),
    LESS("Ít đá"),
    NORMAL("Đá bình thường"),
    EXTRA("Nhiều đá")
}

/**
 * Enum kích cỡ ly.
 */
enum class DrinkSize(val label: String) {
    SMALL("S"),
    MEDIUM("M"),
    LARGE("L")
}

// ─────────────────────────────────────────────────────────
// DOMAIN MODEL – dùng trong ViewModel / UI
// ─────────────────────────────────────────────────────────

/**
 * Data class đại diện cho một sản phẩm (món uống / đồ ăn vặt).
 *
 * @param id              ID sản phẩm (document ID trên Firestore)
 * @param name            Tên sản phẩm (vd: "Trà Xanh Kem Muối")
 * @param description     Mô tả ngắn
 * @param categoryId      ID danh mục liên kết với [Category]
 * @param imageUrl        URL ảnh sản phẩm
 * @param basePrice       Giá gốc (cỡ M / mặc định) tính bằng VNĐ
 * @param sizePrices      Giá theo kích cỡ — key: DrinkSize, value: giá VNĐ
 * @param availableToppings Danh sách ID topping có thể chọn thêm
 * @param sugarOptions    Mức đường cho phép chọn
 * @param iceOptions      Mức đá cho phép chọn
 * @param isBestSeller    Đánh dấu "Bán chạy nhất" hiển thị trên HomeScreen
 * @param isFeatured      Đưa vào banner nổi bật
 * @param isAvailable     Sản phẩm đang được bán (true) hay tạm hết (false)
 * @param soldCount       Tổng số lượng đã bán (dùng để sắp xếp best-seller)
 * @param rating          Điểm đánh giá trung bình (0.0 – 5.0)
 * @param reviewCount     Số lượt đánh giá
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrl: String? = null,
    val basePrice: Long = 0L,
    val sizePrices: Map<DrinkSize, Long> = emptyMap(),
    val availableToppings: List<String> = emptyList(),
    val sugarOptions: List<SugarLevel> = SugarLevel.entries,
    val iceOptions: List<IceLevel> = IceLevel.entries,
    val isBestSeller: Boolean = false,
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true,
    val soldCount: Long = 0L,
    val rating: Float = 0f,
    val reviewCount: Int = 0
) {
    /** Lấy giá theo kích cỡ, nếu không tìm thấy thì trả về basePrice */
    fun priceForSize(size: DrinkSize): Long = sizePrices[size] ?: basePrice

    fun toMap(): Map<String, Any?> = mapOf(
        "id"                  to id,
        "name"                to name,
        "description"         to description,
        "categoryId"          to categoryId,
        "imageUrl"            to imageUrl,
        "basePrice"           to basePrice,
        // Firestore không dùng được enum key → chuyển sang String
        "sizePrices"          to sizePrices.map { (k, v) -> k.name to v }.toMap(),
        "availableToppings"   to availableToppings,
        "sugarOptions"        to sugarOptions.map { it.name },
        "iceOptions"          to iceOptions.map { it.name },
        "isBestSeller"        to isBestSeller,
        "isFeatured"          to isFeatured,
        "isAvailable"         to isAvailable,
        "soldCount"           to soldCount,
        "rating"              to rating,
        "reviewCount"         to reviewCount
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): Product {
            val rawSizePrices = map["sizePrices"] as? Map<String, Any?> ?: emptyMap()
            val sizePrices = rawSizePrices.mapNotNull { (k, v) ->
                runCatching { DrinkSize.valueOf(k) to (v as? Long ?: 0L) }.getOrNull()
            }.toMap()

            val sugarOptions = (map["sugarOptions"] as? List<String>)
                ?.mapNotNull { runCatching { SugarLevel.valueOf(it) }.getOrNull() }
                ?: SugarLevel.entries

            val iceOptions = (map["iceOptions"] as? List<String>)
                ?.mapNotNull { runCatching { IceLevel.valueOf(it) }.getOrNull() }
                ?: IceLevel.entries

            return Product(
                id                = map["id"] as? String ?: "",
                name              = map["name"] as? String ?: "",
                description       = map["description"] as? String ?: "",
                categoryId        = map["categoryId"] as? String ?: "",
                imageUrl          = map["imageUrl"] as? String,
                basePrice         = map["basePrice"] as? Long ?: 0L,
                sizePrices        = sizePrices,
                availableToppings = map["availableToppings"] as? List<String> ?: emptyList(),
                sugarOptions      = sugarOptions,
                iceOptions        = iceOptions,
                isBestSeller      = map["isBestSeller"] as? Boolean ?: false,
                isFeatured        = map["isFeatured"] as? Boolean ?: false,
                isAvailable       = map["isAvailable"] as? Boolean ?: true,
                soldCount         = map["soldCount"] as? Long ?: 0L,
                rating            = (map["rating"] as? Number)?.toFloat() ?: 0f,
                reviewCount       = (map["reviewCount"] as? Long)?.toInt() ?: 0
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// DTO – dùng khi gọi API bên ngoài hoặc import dữ liệu hàng loạt
// ─────────────────────────────────────────────────────────

/**
 * ProductDto là dạng "phẳng" (flat) của Product dùng để:
 * - Import/export CSV
 * - Gọi REST API bên ngoài (nếu có)
 * - Serialize / deserialize đơn giản
 *
 * Khác với [Product], DTO dùng String thay vì enum để dễ dàng
 * serialize mà không phụ thuộc vào Kotlin enum.
 */
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrl: String? = null,
    val basePrice: Long = 0L,
    val isAvailable: Boolean = true
) {
    /** Chuyển DTO sang domain model với các giá trị mặc định */
    fun toDomain(): Product = Product(
        id          = id,
        name        = name,
        description = description,
        categoryId  = categoryId,
        imageUrl    = imageUrl,
        basePrice   = basePrice,
        isAvailable = isAvailable
    )

    companion object {
        /** Chuyển domain model sang DTO (bỏ qua các trường phức tạp) */
        fun fromDomain(product: Product): ProductDto = ProductDto(
            id          = product.id,
            name        = product.name,
            description = product.description,
            categoryId  = product.categoryId,
            imageUrl    = product.imageUrl,
            basePrice   = product.basePrice,
            isAvailable = product.isAvailable
        )
    }
}