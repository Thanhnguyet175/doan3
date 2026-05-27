package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.model.data.ProductDto
import com.example.milkteaapp.model.data.Topping
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository quản lý sản phẩm, danh mục và topping.
 * Thực hiện các thao tác dữ liệu và đồng bộ an toàn trực tiếp với Firestore.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val firestoreSource: FirestoreSource
) {
    // ════════════════════════════════════════════════════════════════════════
    // PRODUCT
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả sản phẩm đang bán (isAvailable == true) */
    suspend fun getAvailableProducts(): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllProducts().filter { it.isAvailable }
            }
        }

    /** Lấy sản phẩm theo danh mục cụ thể */
    suspend fun getProductsByCategory(categoryId: String): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllProducts().filter { it.categoryId == categoryId }
            }
        }

    /** Lấy sản phẩm bán chạy nhất (Mặc định lấy 5 món đầu tiên) */
    suspend fun getBestSellers(limit: Long = 5): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllProducts()
                    .filter { it.isAvailable }
                    .take(limit.toInt())
            }
        }

    /** Lấy sản phẩm nổi bật cho banner */
    suspend fun getFeaturedProducts(): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllProducts().filter { it.isAvailable }
            }
        }

    /** Lấy chi tiết một sản phẩm theo ID */
    suspend fun getProductById(productId: String): Result<Product> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getProduct(productId)
                    ?: throw Exception("Sản phẩm không tồn tại.")
            }
        }

    // ── CRUD PRODUCT (Admin) ──────────────────────────────────────────────────

    /** Thêm sản phẩm mới lên hệ thống */
    suspend fun addProduct(product: Product): Result<Product> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (product.id.isBlank())
                    product.copy(id = UUID.randomUUID().toString()) else product

                // FIX: dùng import thay vì fully-qualified name
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val dto = ProductDto.fromDomain(toSave)

                suspendCancellableCoroutine { continuation ->
                    db.collection("products").document(toSave.id).set(dto)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Add product failed"))
                            }
                        }
                }
                toSave
            }
        }

    /** Cập nhật thông tin sản phẩm */
    suspend fun updateProduct(product: Product): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // FIX: dùng import thay vì fully-qualified name
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val dto = ProductDto.fromDomain(product)

                suspendCancellableCoroutine { continuation ->
                    db.collection("products").document(product.id).set(dto)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Update product failed"))
                            }
                        }
                }
            }
        }

    /** Ẩn/hiện sản phẩm nhanh bằng cách cập nhật trạng thái isAvailable */
    suspend fun setProductAvailability(productId: String, isAvailable: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                suspendCancellableCoroutine { continuation ->
                    db.collection("products").document(productId).update("isAvailable", isAvailable)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Update availability failed"))
                            }
                        }
                }
            }
        }

    /** Xoá hoàn toàn sản phẩm khỏi hệ thống */
    suspend fun deleteProduct(productId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                suspendCancellableCoroutine { continuation ->
                    db.collection("products").document(productId).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Delete product failed"))
                            }
                        }
                }
            }
        }

    // ════════════════════════════════════════════════════════════════════════
    // CATEGORY
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy danh sách danh mục */
    suspend fun getCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getAllCategories() }
        }

    /** Thêm / cập nhật danh mục */
    suspend fun saveCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (category.id.isBlank())
                    category.copy(id = UUID.randomUUID().toString()) else category

                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val mapData = mapOf("name" to toSave.name, "iconName" to toSave.iconName)

                suspendCancellableCoroutine { continuation ->
                    db.collection("categories").document(toSave.id).set(mapData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Save category failed"))
                            }
                        }
                }
            }
        }

    /** Xoá danh mục */
    suspend fun deleteCategory(categoryId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                suspendCancellableCoroutine { continuation ->
                    db.collection("categories").document(categoryId).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Delete category failed"))
                            }
                        }
                }
            }
        }

    // ════════════════════════════════════════════════════════════════════════
    // TOPPING
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả topping hiện có */
    suspend fun getAllToppings(): Result<List<Topping>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getAllToppings() }
        }

    /** Lấy danh sách Topping lọc theo danh sách ID truyền vào */
    suspend fun getToppingsByIds(ids: List<String>): Result<List<Topping>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllToppings().filter { ids.contains(it.id) }
            }
        }

    /** Thêm / cập nhật topping */
    suspend fun saveTopping(topping: Topping): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (topping.id.isBlank())
                    topping.copy(id = UUID.randomUUID().toString()) else topping

                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val mapData = mapOf(
                    "name" to toSave.name,
                    "price" to toSave.price,
                    "isAvailable" to toSave.isAvailable
                )

                suspendCancellableCoroutine { continuation ->
                    db.collection("toppings").document(toSave.id).set(mapData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Save topping failed"))
                            }
                        }
                }
            }
        }

    /** Xoá topping */
    suspend fun deleteTopping(toppingId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                suspendCancellableCoroutine { continuation ->
                    db.collection("toppings").document(toppingId).delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Delete topping failed"))
                            }
                        }
                }
            }
        }
}