package com.example.milkteaapp.model.data

import com.google.firebase.Timestamp

enum class PromotionType {
    PERCENT_DISCOUNT, // Giảm %
    FIXED_DISCOUNT,   // Giảm tiền mặt
    BUY_X_GET_Y,      // Mua X tặng Y
    FREE_GIFT         // Quà tặng
}

data class Promotion(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val bannerImageUrl: String? = null,
    val type: PromotionType = PromotionType.PERCENT_DISCOUNT,
    val discountValue: Double = 0.0,
    val buyQuantity: Int = 1,
    val getQuantity: Int = 1,
    val applicableCategoryIds: List<String> = emptyList(),
    val minOrderAmount: Long = 0L,
    val maxUsageCount: Int = 0,
    val currentUsage: Int = 0,
    val startAt: Timestamp = Timestamp.now(),
    val endAt: Timestamp? = null,
    val isActive: Boolean = true
) {
    // 1. Kiểm tra hiệu lực: Dùng bối cảnh phủ định (Guard Clauses) để code bớt lồng nhau
    fun isValid(): Boolean {
        val now = Timestamp.now()

        if (!isActive) return false
        if (now < startAt) return false
        if (endAt != null && now > endAt) return false
        if (maxUsageCount in 1..currentUsage) return false // Nếu có giới hạn và đã dùng hết

        return true
    }

    // 2. Tính số tiền giảm: Viết ngắn gọn bằng biểu thức when
    fun calculateDiscount(orderTotal: Long): Long {
        if (orderTotal < minOrderAmount) return 0L

        return when (type) {
            PromotionType.PERCENT_DISCOUNT -> (orderTotal * (discountValue / 100)).toLong()
            PromotionType.FIXED_DISCOUNT -> discountValue.toLong()
            else -> 0L
        }.coerceAtMost(orderTotal) // Đảm bảo số tiền giảm không lớn hơn tổng hóa đơn
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "bannerImageUrl" to bannerImageUrl,
        "type" to type.name,
        "discountValue" to discountValue,
        "buyQuantity" to buyQuantity,
        "getQuantity" to getQuantity,
        "applicableCategoryIds" to applicableCategoryIds,
        "minOrderAmount" to minOrderAmount,
        "maxUsageCount" to maxUsageCount,
        "currentUsage" to currentUsage,
        "startAt" to startAt,
        "endAt" to endAt,
        "isActive" to isActive
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): Promotion = Promotion(
            id = map["id"] as? String ?: "",
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            bannerImageUrl = map["bannerImageUrl"] as? String,
            type = runCatching {
                PromotionType.valueOf(map["type"] as? String ?: "")
            }.getOrDefault(PromotionType.PERCENT_DISCOUNT),
            discountValue = (map["discountValue"] as? Number)?.toDouble() ?: 0.0,
            buyQuantity = (map["buyQuantity"] as? Long)?.toInt() ?: 1,
            getQuantity = (map["getQuantity"] as? Long)?.toInt() ?: 1,
            applicableCategoryIds = map["applicableCategoryIds"] as? List<String> ?: emptyList(),
            minOrderAmount = map["minOrderAmount"] as? Long ?: 0L,
            maxUsageCount = (map["maxUsageCount"] as? Long)?.toInt() ?: 0,
            currentUsage = (map["currentUsage"] as? Long)?.toInt() ?: 0,
            startAt = map["startAt"] as? Timestamp ?: Timestamp.now(),
            endAt = map["endAt"] as? Timestamp,
            isActive = map["isActive"] as? Boolean ?: true
        )
    }
}