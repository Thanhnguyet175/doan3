package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.CartItem
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
     * ID đơn hàng được sinh theo định dạng "NL-XXXX" với kiểm tra trùng lặp.
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

            val orderMap = mapOf(
                "id" to order.id,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                // 🟢 ĐÃ FIX: Map chính xác theo các thuộc tính SNAPSHOT của OrderItem trong Order.kt
                "items" to order.items.map { item ->
                    mapOf(
                        "productId" to item.productId,
                        "productName" to item.productName,
                        "productImageUrl" to item.productImageUrl,
                        "size" to item.size.name,
                        "sugarLevel" to item.sugarLevel.name,
                        "iceLevel" to item.iceLevel.name,
                        "selectedToppings" to item.selectedToppings.map { t ->
                            mapOf(
                                "id" to t.id,
                                "name" to t.name,
                                "price" to t.price,
                                "isAvailable" to t.isAvailable
                            )
                        },
                        "unitPrice" to item.unitPrice,
                        "quantity" to item.quantity,
                        "note" to item.note
                    )
                },
                "totalAmount" to order.totalAmount,
                "discountAmount" to order.discountAmount,
                "finalAmount" to order.finalAmount,
                "status" to order.status.name,
                "paymentMethod" to order.paymentMethod,
                "deliveryAddress" to order.deliveryAddress,
                "note" to order.note,
                "createdAt" to com.google.firebase.Timestamp(order.createdAt.seconds, order.createdAt.nanoseconds),
                "updatedAt" to com.google.firebase.Timestamp(order.updatedAt.seconds, order.updatedAt.nanoseconds)
            )

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            suspendCancellableCoroutine { continuation ->
                db.collection("orders").document(order.id).set(orderMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(task.exception ?: Exception("Place order failed"))
                        }
                    }
            }

            if (!promotionId.isNullOrBlank()) {
                runCatching { firestoreSource.incrementPromotionUsage(promotionId) }
            }

            order
        }
    }

    // ── Cập nhật trạng thái ───────────────────────────────────────────────────

    /**
     * Nhân viên cập nhật trạng thái đơn hàng (ví dụ: CONFIRMED -> BREWING).
     */
    suspend fun updateStatus(orderId: String, newStatus: OrderStatus): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val updates = mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                suspendCancellableCoroutine { continuation ->
                    db.collection("orders").document(orderId).update(updates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Update status failed"))
                            }
                        }
                }
            }
        }

    /** Huỷ đơn hàng */
    suspend fun cancelOrder(orderId: String): Result<Unit> =
        updateStatus(orderId, OrderStatus.CANCELLED)

    // ── Truy vấn dữ liệu ──────────────────────────────────────────────────────

    /** Lắng nghe Realtime lịch sử đơn hàng của riêng một khách hàng */
    fun observeCustomerOrders(customerId: String): Flow<List<Order>> = callbackFlow {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val listener = db.collection("orders")
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Order.fromMap(doc.id, it) } // 🟢 ĐÃ FIX: Truyền thêm đối số doc.id vào hàm fromMap chuẩn cấu trúc mới
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    /** Lấy toàn bộ danh sách đơn hàng (Admin) */
    suspend fun getAllOrders(): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                suspendCancellableCoroutine { continuation ->
                    db.collection("orders")
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val orders = task.result?.documents?.mapNotNull { doc ->
                                    doc.data?.let { Order.fromMap(doc.id, it) } // 🟢 ĐÃ FIX: Thêm doc.id đồng bộ
                                }?.sortedByDescending { it.createdAt } ?: emptyList()
                                continuation.resume(orders)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Get all orders failed"))
                            }
                        }
                }
            }
        }

    /** Đơn hàng lọc theo trạng thái cụ thể (Staff / Admin) */
    suspend fun getOrdersByStatus(status: OrderStatus): Result<List<Order>> =
        withContext(Dispatchers.IO) {
            runCatching {
                getAllOrders().getOrThrow().filter { it.status == status }
            }
        }

    /** Lắng nghe toàn bộ các đơn hàng chưa hoàn thành (Realtime Flow cho Dashboard Nhân viên) */
    fun observeActiveOrders(): Flow<List<Order>> = callbackFlow {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val listener = db.collection("orders")
            .whereIn("status", listOf(OrderStatus.PENDING.name, OrderStatus.CONFIRMED.name, OrderStatus.BREWING.name, OrderStatus.READY.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { Order.fromMap(doc.id, it) } // 🟢 ĐÃ FIX: Thêm doc.id đồng bộ
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Sinh ID đơn hàng dạng "NL-XXXX" với kiểm tra trùng lặp trên Firestore.
     * Thử tối đa 10 lần, fallback sang timestamp nếu vẫn trùng.
     */
    private suspend fun generateOrderId(): String {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val allowedChars = ('A'..'Z') + ('0'..'9')

        repeat(10) {
            val randomStr = (1..4).map { allowedChars.random() }.joinToString("")
            val id = "NL-$randomStr"
            val exists = suspendCancellableCoroutine { cont ->
                db.collection("orders").document(id).get()
                    .addOnCompleteListener { task ->
                        cont.resume(task.result?.exists() == true)
                    }
            }
            if (!exists) return id
        }

        return "NL-${System.currentTimeMillis() % 1_000_000}"
    }
}