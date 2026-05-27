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
    suspend fun getUser(uid: String): User? {
        val snap = usersCol.document(uid).get().await()
        return if (snap.exists()) User.fromMap(snap.data ?: emptyMap()) else null
    }

    suspend fun saveUser(user: User) {
        usersCol.document(user.uid).set(user.toMap()).await()
    }

    // ════════════════════════════════════════════════════════════════════════
    // PRODUCT & TOPPING
    // ════════════════════════════════════════════════════════════════════════
    suspend fun getAllProducts(): List<Product> =
        productsCol.get().await().documents
            .mapNotNull { it.toObject(ProductDto::class.java)?.toDomain() }

    suspend fun getProduct(productId: String): Product? {
        val snap = productsCol.document(productId).get().await()
        return snap.toObject(ProductDto::class.java)?.toDomain()
    }

    suspend fun getAllCategories(): List<Category> =
        categoriesCol.get().await().documents
            .mapNotNull { snap ->
                val name = snap.getString("name") ?: ""
                val icon = snap.getString("iconName") ?: ""
                Category(id = snap.id, name = name, iconName = icon)
            }

    suspend fun getAllToppings(): List<Topping> =
        toppingsCol.get().await().documents
            .mapNotNull { snap ->
                val name = snap.getString("name") ?: ""
                val price = snap.getLong("price") ?: 0L
                val avail = snap.getBoolean("isAvailable") ?: true
                Topping(id = snap.id, name = name, price = price, isAvailable = avail)
            }

    // ════════════════════════════════════════════════════════════════════════
    // ORDER (GIỎ HÀNG & LỊCH SỬ ĐẶT HÀNG)
    // ════════════════════════════════════════════════════════════════════════

    /** Lưu đơn hàng mới lên Firestore */
    suspend fun saveOrder(order: Order) {
        val mapData = mutableMapOf<String, Any?>(
            "customerId"      to order.customerId,
            "customerName"    to order.customerName,
            "customerPhone"   to order.customerPhone,
            "totalAmount"     to order.totalAmount,
            "discountAmount"  to order.discountAmount,
            "promotionId"     to order.promotionId,
            "status"          to order.status.name,
            "paymentMethod"   to order.paymentMethod,
            "deliveryAddress" to order.deliveryAddress,
            "note"            to order.note,
            "createdAt"       to order.createdAt,
            "updatedAt"       to order.updatedAt
        )

        val itemsList = order.items.map { item ->
            mapOf(
                "productId"       to item.productId,
                "productName"     to item.productName,
                "productImageUrl" to item.productImageUrl,
                "size"            to item.size.name,
                "sugarLevel"      to item.sugarLevel.name,
                "iceLevel"        to item.iceLevel.name,
                "unitPrice"       to item.unitPrice,
                "quantity"        to item.quantity,
                "note"            to item.note,
                "selectedToppings" to item.selectedToppings.map { t ->
                    mapOf("id" to t.id, "name" to t.name, "price" to t.price, "isAvailable" to t.isAvailable)
                }
            )
        }
        mapData["items"] = itemsList

        ordersCol.document(order.id).set(mapData).await()
    }

    /** Lắng nghe danh sách đơn hàng của 1 khách hàng theo thời gian thực (Realtime Flow) */
    fun observeCustomerOrders(customerId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCol
            .whereEqualTo("customerId", customerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { d -> Order.fromMap(doc.id, d) } // 🟢 ĐÃ FIX: Truyền thêm doc.id vào tham số thứ nhất
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    /** Lấy toàn bộ danh sách đơn hàng hệ thống (Dành cho màn hình quản lý của Admin) */
    suspend fun getAllOrders(): List<Order> =
        ordersCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Order.fromMap(it.id, d) } } // 🟢 ĐÃ FIX: Truyền thêm it.id vào tham số thứ nhất

    // ════════════════════════════════════════════════════════════════════════
    // PROMOTION
    // ════════════════════════════════════════════════════════════════════════
    suspend fun getActivePromotions(): List<Promotion> =
        promotionsCol.whereEqualTo("isActive", true)
            .get().await().documents
            .mapNotNull { it.data?.let { d -> Promotion.fromMap(d) } }

    suspend fun getPromotion(promotionId: String): Promotion? {
        val snap = promotionsCol.document(promotionId).get().await()
        return if (snap.exists()) Promotion.fromMap(snap.data ?: emptyMap()) else null
    }

    suspend fun savePromotion(promotion: Promotion) {
        promotionsCol.document(promotion.id).set(promotion.toMap()).await()
    }

    suspend fun incrementPromotionUsage(promotionId: String) {
        promotionsCol.document(promotionId)
            .update("currentUsage", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }
}