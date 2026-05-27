package com.example.milkteaapp.model.remote

import com.example.milkteaapp.model.data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lớp nguồn dữ liệu Firestore – mọi truy vấn đọc/ghi đều đi qua đây.
 *
 * Cấu trúc collection trên Firestore:
 *   /users/{uid}
 *   /products/{productId}
 *   /categories/{categoryId}
 *   /toppings/{toppingId}
 *   /orders/{orderId}
 *   /promotions/{promotionId}
 */
@Singleton
class FirestoreSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    // ── Collection references ─────────────────────────────────────────────────
    private val usersCol      = db.collection("users")
    private val productsCol   = db.collection("products")
    private val categoriesCol = db.collection("categories")
    private val toppingsCol   = db.collection("toppings")
    private val ordersCol     = db.collection("orders")
    private val promotionsCol = db.collection("promotions")

    // ════════════════════════════════════════════════════════════════════════
    // USER
    // ════════════════════════════════════════════════════════════════════════

    /** Lưu thông tin User sau khi đăng ký thành công */
    suspend fun saveUser(user: User) {
        usersCol.document(user.uid).set(user.toMap()).await()
    }

    /** Lấy thông tin User theo UID */
    suspend fun getUser(uid: String): User? {
        val snap = usersCol.document(uid).get().await()
        return if (snap.exists()) User.fromMap(snap.data ?: emptyMap()) else null
    }

    /** Cập nhật một số field của User (partial update) */
    suspend fun updateUser(uid: String, fields: Map<String, Any?>) {
        usersCol.document(uid).update(fields).await()
    }

    /** Lấy tất cả user (Admin) */
    suspend fun getAllUsers(): List<User> =
        usersCol.get().await().documents
            .mapNotNull { it.data?.let { d -> User.fromMap(d) } }

    /** Khoá / mở khoá tài khoản user (Admin) */
    suspend fun setUserActive(uid: String, isActive: Boolean) {
        usersCol.document(uid).update("isActive", isActive).await()
    }

    // ════════════════════════════════════════════════════════════════════════
    // CATEGORY
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy danh sách danh mục, sắp xếp theo sortOrder */
    suspend fun getCategories(): List<Category> =
        categoriesCol.orderBy("sortOrder").get().await().documents
            .mapNotNull { it.data?.let { d -> Category.fromMap(d) } }

    /** Lưu / ghi đè một danh mục */
    suspend fun saveCategory(category: Category) {
        categoriesCol.document(category.id).set(category.toMap()).await()
    }

    /** Xoá danh mục theo ID */
    suspend fun deleteCategory(categoryId: String) {
        categoriesCol.document(categoryId).delete().await()
    }

    // ════════════════════════════════════════════════════════════════════════
    // TOPPING
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả topping */
    suspend fun getToppings(): List<Topping> =
        toppingsCol.get().await().documents
            .mapNotNull { it.data?.let { d -> Topping.fromMap(d) } }

    /** Lấy topping theo danh sách ID */
    suspend fun getToppingsByIds(ids: List<String>): List<Topping> {
        if (ids.isEmpty()) return emptyList()
        // Firestore whereIn giới hạn 30 phần tử
        return ids.chunked(30).flatMap { chunk ->
            toppingsCol.whereIn("id", chunk).get().await().documents
                .mapNotNull { it.data?.let { d -> Topping.fromMap(d) } }
        }
    }

    /** Lưu / ghi đè một topping */
    suspend fun saveTopping(topping: Topping) {
        toppingsCol.document(topping.id).set(topping.toMap()).await()
    }

    /** Xoá topping */
    suspend fun deleteTopping(toppingId: String) {
        toppingsCol.document(toppingId).delete().await()
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRODUCT
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả sản phẩm đang bán */
    suspend fun getAvailableProducts(): List<Product> =
        productsCol.whereEqualTo("isAvailable", true)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Product.fromMap(d) } }

    /** Lấy sản phẩm theo danh mục */
    suspend fun getProductsByCategory(categoryId: String): List<Product> =
        productsCol.whereEqualTo("categoryId", categoryId)
            .whereEqualTo("isAvailable", true)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Product.fromMap(d) } }

    /** Lấy sản phẩm bán chạy nhất (top N) */
    suspend fun getBestSellerProducts(limit: Long = 5): List<Product> =
        productsCol.whereEqualTo("isBestSeller", true)
            .orderBy("soldCount", Query.Direction.DESCENDING)
            .limit(limit).get().await().documents
            .mapNotNull { it.data?.let { d -> Product.fromMap(d) } }

    /** Lấy sản phẩm nổi bật cho banner */
    suspend fun getFeaturedProducts(): List<Product> =
        productsCol.whereEqualTo("isFeatured", true)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Product.fromMap(d) } }

    /** Lấy chi tiết một sản phẩm */
    suspend fun getProduct(productId: String): Product? {
        val snap = productsCol.document(productId).get().await()
        return if (snap.exists()) Product.fromMap(snap.data ?: emptyMap()) else null
    }

    /** Lưu / ghi đè sản phẩm (Admin) */
    suspend fun saveProduct(product: Product) {
        productsCol.document(product.id).set(product.toMap()).await()
    }

    /** Cập nhật một số field sản phẩm (Admin) */
    suspend fun updateProduct(productId: String, fields: Map<String, Any?>) {
        productsCol.document(productId).update(fields).await()
    }

    /** Xoá sản phẩm (Admin) */
    suspend fun deleteProduct(productId: String) {
        productsCol.document(productId).delete().await()
    }

    // ════════════════════════════════════════════════════════════════════════
    // ORDER
    // ════════════════════════════════════════════════════════════════════════

    /** Tạo đơn hàng mới */
    suspend fun createOrder(order: Order) {
        ordersCol.document(order.id).set(order.toMap()).await()
    }

    /** Cập nhật trạng thái đơn hàng (Staff / Admin) */
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        ordersCol.document(orderId).update(
            mapOf(
                "status"    to status.name,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
        ).await()
    }

    /** Cập nhật nhiều field đơn hàng */
    suspend fun updateOrder(orderId: String, fields: Map<String, Any?>) {
        ordersCol.document(orderId).update(fields).await()
    }

    /** Lấy lịch sử đơn của khách */
    suspend fun getOrdersByCustomer(customerId: String): List<Order> =
        ordersCol.whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Order.fromMap(d) } }

    /** Lấy tất cả đơn hàng (Admin) */
    suspend fun getAllOrders(): List<Order> =
        ordersCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Order.fromMap(d) } }

    /** Lấy đơn hàng đang xử lý realtime – Staff Dashboard dùng Flow */
    fun getActiveOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val activeStatuses = listOf(
            OrderStatus.PENDING.name,
            OrderStatus.CONFIRMED.name,
            OrderStatus.BREWING.name,
            OrderStatus.DELAYED.name
        )
        val listener = ordersCol
            .whereIn("status", activeStatuses)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents
                    ?.mapNotNull { it.data?.let { d -> Order.fromMap(d) } }
                    ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    /** Lấy đơn hàng theo trạng thái cụ thể */
    suspend fun getOrdersByStatus(status: OrderStatus): List<Order> =
        ordersCol.whereEqualTo("status", status.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Order.fromMap(d) } }

    // ════════════════════════════════════════════════════════════════════════
    // PROMOTION
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy các khuyến mãi đang hoạt động */
    suspend fun getActivePromotions(): List<Promotion> =
        promotionsCol.whereEqualTo("isActive", true)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Promotion.fromMap(d) } }

    /** Lấy chi tiết một khuyến mãi */
    suspend fun getPromotion(promotionId: String): Promotion? {
        val snap = promotionsCol.document(promotionId).get().await()
        return if (snap.exists()) Promotion.fromMap(snap.data ?: emptyMap()) else null
    }

    /** Lưu / ghi đè khuyến mãi (Admin) */
    suspend fun savePromotion(promotion: Promotion) {
        promotionsCol.document(promotion.id).set(promotion.toMap()).await()
    }

    /** Tăng số lần dùng khuyến mãi sau khi đặt hàng thành công */
    suspend fun incrementPromotionUsage(promotionId: String) {
        promotionsCol.document(promotionId)
            .update("currentUsage", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }

    /** Xoá khuyến mãi (Admin) */
    suspend fun deletePromotion(promotionId: String) {
        promotionsCol.document(promotionId).delete().await()
    }
}