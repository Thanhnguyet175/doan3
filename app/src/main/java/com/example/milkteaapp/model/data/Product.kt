package com.example.milkteaapp.model.data

/**
 * Enum mức độ đường (sugar level) theo yêu cầu mới.
 */
enum class SugarLevel(val label: String) {
    EIGHTY("80%"),
    NINETY("90%"),
    ONE_HUNDRED("100%"),
    ONE_TEN("110%"),
    ONE_TWENTY("120%")
}

/**
 * Enum hình thức đóng gói đá độc lập cho UI.
 */
enum class IcePackOption(val label: String) {
    DA_RIENG("Đá riêng"),
    DA_CHUNG("Đá chung")
}

/**
 * Enum mức độ đá khi chọn đá chung.
 */
enum class IceLevel(val label: String) {
    LESS("Ít đá"),
    NORMAL("Đá bình thường"),
    EXTRA("Nhiều đá")
}

/**
 * Enum kích cỡ ly.
 */
enum class DrinkSize(val label: String) {
    MEDIUM("M"),
    LARGE("L")
}

// ─────────────────────────────────────────────────────────
// DOMAIN MODEL
// ─────────────────────────────────────────────────────────

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrl: String? = null,
    val basePrice: Long = 0L,
    val isAvailable: Boolean = true,
    val availableSizes: List<DrinkSize> = listOf(DrinkSize.MEDIUM),
    val sizePrices: Map<String, Long> = emptyMap(),

    // Tự động gán danh sách để giao diện bốc ra dùng trực tiếp
    val sugarOptions: List<SugarLevel> = SugarLevel.values().toList(),
    val iceOptions: List<IceLevel> = IceLevel.values().toList(),
    val toppingIds: List<String> = emptyList()
)


data class ProductDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrl: String? = null,
    val basePrice: Long = 0L,
    val isAvailable: Boolean = true,
    val sizePrices: Map<String, Long> = emptyMap()
) {
    fun toDomain(): Product = Product(
        id          = id,
        name        = name,
        description = description,
        categoryId  = categoryId,
        imageUrl    = imageUrl,
        basePrice   = basePrice,
        sizePrices = this.sizePrices,
        isAvailable = isAvailable
    )

    companion object {
        fun fromDomain(product: Product): ProductDto = ProductDto(
            id          = product.id,
            name        = product.name,
            description = product.description,
            categoryId  = product.categoryId,
            imageUrl    = product.imageUrl,
            basePrice   = product.basePrice,
            sizePrices = product.sizePrices,
            isAvailable = product.isAvailable
        )
    }
}