package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.*
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý vòng đời đơn hàng:
 * tạo mới, cập nhật trạng thái, truy vấn theo customer / admin / staff.
 */
@Singleton
class OrderRepository @Inject constructor(
    private val firestoreSource: FirestoreSource
) {
    // ── Tạo đơn hàng ─────────────────────────────────────────────────────────

    /**
     * Tạo đơn hàng mới từ giỏ hàng của khách.
     * ID đơn hàng được sinh theo định dạng "NL-XXXX".
     *
     * @return [Result.success] chứa [Order] vừa tạo
     */
    suspend fun placeOrder(
        customer: User,
        cartItems: List<CartItem>,
        promotionId: String? = null,
        discountAmount: Long = 0L,
        paymentMethod: String = "CASH",
        deliveryAddress: String? = null,
        note: String = ""
    ): Result<Order> = withContext(Dispatchers.IO) {
        runCatching {
            if (cartItems.isEmpty()) throw Exception("Giỏ hàng trống.")

            val orderId = generateOrderId()
            val order = Order.fromCart(
                orderId         = orderId,
                customer        = customer,
                cartItems       = cartItems,
                promotionId     = promotionId,
                discountAmount  = discountAmount,
                paymentMethod   = paymentMethod,
                deliveryAddress = deliveryAddress,
                note            = note
            )
            firestoreSource.createOrder(order)

            // Tăng usage counter nếu có áp dụng khuyến mãi
            promotionId?.let { firestoreSource.incrementPromotionUsage(it) }

            order
        }
    }

    // ── Cập nhật trạng thái ───────────────────────────────────────────────────

    /**
     * Cập nhật trạng thái đơn hàng (Staff / Admin).
     * Tự động cập nhật [Order.updatedAt].
     */
    suspend fun updateStatus(orderId: String, status: OrderStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.updateOrderStatus(orderId, status) }
        }

    /** Gán nhân viên xử lý đơn */
    suspend fun assignStaff(orderId: String, staffId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.updateOrder(orderId, mapOf("assignedStaffId" to staffId))
            }
        }

    /** Đánh dấu đơn đã thanh toán */
    suspend fun markAsPaid(orderId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.updateOrder(orderId, mapOf("isPaid" to true))
            }
        }

    /** Huỷ đơn hàng */
    suspend fun cancelOrder(orderId: String): Result<Unit> =
        updateStatus(orderId, OrderStatus.CANCELLED)

    // ── Truy vấn ─────────────────────────────────────────────────────────────

    /** Lịch sử đơn hàng của khách */
    suspend fun getOrderHistory(customerId: String): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getOrdersByCustomer(customerId) }
        }

    /** Tất cả đơn hàng (Admin) */
    suspend fun getAllOrders(): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getAllOrders() }
        }

    /** Đơn hàng theo trạng thái cụ thể (Admin) */
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getOrdersByStatus(status) }
        }

    /**
     * Realtime Flow các đơn đang xử lý – dùng cho Staff Dashboard.
     * Emit mỗi khi Firestore có thay đổi.
     */
    fun observeActiveOrders(): Flow<List<Order>> =
        firestoreSource.getActiveOrdersFlow()

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Sinh ID đơn hàng dạng "NL-XXXX" (4 ký tự hex ngẫu nhiên, viết hoa).
     */
    private fun generateOrderId(): String {
        val suffix = UUID.randomUUID().toString().take(4).uppercase()
        return "NL-$suffix"
    }
}