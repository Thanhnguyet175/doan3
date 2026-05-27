package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.model.data.Topping
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý sản phẩm, danh mục và topping.
 * Cung cấp giao diện sạch cho ViewModel, che giấu chi tiết Firestore.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val firestoreSource: FirestoreSource
) {
    // ════════════════════════════════════════════════════════════════════════
    // PRODUCT
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả sản phẩm đang bán */
    suspend fun getAvailableProducts(): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getAvailableProducts() }
        }

    /** Lấy sản phẩm theo danh mục */
    suspend fun getProductsByCategory(categoryId: String): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getProductsByCategory(categoryId) }
        }

    /** Lấy sản phẩm bán chạy nhất */
    suspend fun getBestSellers(limit: Long = 5): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getBestSellerProducts(limit) }
        }

    /** Lấy sản phẩm nổi bật cho banner */
    suspend fun getFeaturedProducts(): Result<List<Product>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getFeaturedProducts() }
        }

    /** Lấy chi tiết một sản phẩm */
    suspend fun getProductById(productId: String): Result<Product> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getProduct(productId)
                    ?: throw Exception("Sản phẩm không tồn tại.")
            }
        }

    // ── CRUD (Admin) ──────────────────────────────────────────────────────────

    /**
     * Thêm sản phẩm mới (Admin).
     * Tự sinh ID nếu [product.id] rỗng.
     */
    suspend fun addProduct(product: Product): Result<Product> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (product.id.isBlank())
                    product.copy(id = UUID.randomUUID().toString()) else product
                firestoreSource.saveProduct(toSave)
                toSave
            }
        }

    /** Cập nhật sản phẩm (Admin) */
    suspend fun updateProduct(product: Product): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.saveProduct(product) }
        }

    /** Ẩn/hiện sản phẩm nhanh mà không cần load toàn bộ object (Admin) */
    suspend fun setProductAvailability(productId: String, isAvailable: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.updateProduct(productId, mapOf("isAvailable" to isAvailable))
            }
        }

    /** Xoá sản phẩm (Admin) */
    suspend fun deleteProduct(productId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.deleteProduct(productId) }
        }

    // ════════════════════════════════════════════════════════════════════════
    // CATEGORY
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy danh sách danh mục (sắp xếp theo sortOrder) */
    suspend fun getCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getCategories() }
        }

    /** Thêm / cập nhật danh mục (Admin) */
    suspend fun saveCategory(category: Category): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (category.id.isBlank())
                    category.copy(id = UUID.randomUUID().toString()) else category
                firestoreSource.saveCategory(toSave)
            }
        }

    /** Xoá danh mục (Admin) */
    suspend fun deleteCategory(categoryId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.deleteCategory(categoryId) }
        }

    // ════════════════════════════════════════════════════════════════════════
    // TOPPING
    // ════════════════════════════════════════════════════════════════════════

    /** Lấy tất cả topping */
    suspend fun getAllToppings(): Result<List<Topping>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getToppings() }
        }

    /** Lấy topping theo danh sách ID (dùng ở ProductDetailScreen) */
    suspend fun getToppingsByIds(ids: List<String>): Result<List<Topping>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getToppingsByIds(ids) }
        }

    /** Thêm / cập nhật topping (Admin) */
    suspend fun saveTopping(topping: Topping): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val toSave = if (topping.id.isBlank())
                    topping.copy(id = UUID.randomUUID().toString()) else topping
                firestoreSource.saveTopping(toSave)
            }
        }

    /** Xoá topping (Admin) */
    suspend fun deleteTopping(toppingId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.deleteTopping(toppingId) }
        }
}