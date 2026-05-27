package com.example.milkteaapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────
data class AdminCategoryUiState(
    val isLoadingCategories: Boolean = false,
    // Map: categoryId -> đang load sản phẩm của danh mục đó
    val isLoadingProducts: Map<String, Boolean> = emptyMap(),
    val danhSachDanhMuc: List<Category> = emptyList(),
    // Map: categoryId -> danh sách sản phẩm đã tải
    val sanPhamTheoDanhMuc: Map<String, List<Product>> = emptyMap(),
    // ID danh mục đang mở rộng (accordion), null = tất cả đóng
    val expandedCategoryId: String? = null,
    val dangMoForm: Boolean = false,
    val danhMucDangSua: Category? = null,
    val thongBaoThanhCong: String? = null,
    val thongBaoLoi: String? = null
)

@HiltViewModel
class AdminCategoryViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoryUiState())
    val uiState: StateFlow<AdminCategoryUiState> = _uiState.asStateFlow()

    init { taiDanhSachDanhMuc() }

    // ── Tải danh sách danh mục từ Firestore ──────────────────────────────────
    fun taiDanhSachDanhMuc() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true, thongBaoLoi = null) }
            try {
                val snapshot = db.collection("categories")
                    .orderBy("sortOrder")
                    .get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Category.fromMap(data + mapOf("id" to doc.id))
                }
                _uiState.update { it.copy(isLoadingCategories = false, danhSachDanhMuc = list) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingCategories = false, thongBaoLoi = e.message) }
            }
        }
    }

    // ── Toggle accordion: bấm vào danh mục để mở/đóng ───────────────────────
    fun toggleDanhMuc(category: Category) {
        val currentExpanded = _uiState.value.expandedCategoryId

        if (currentExpanded == category.id) {
            // Đang mở → đóng lại
            _uiState.update { it.copy(expandedCategoryId = null) }
            return
        }

        // Mở danh mục mới
        _uiState.update { it.copy(expandedCategoryId = category.id) }

        // Nếu chưa có sản phẩm của danh mục này → tải từ Firestore
        if (!_uiState.value.sanPhamTheoDanhMuc.containsKey(category.id)) {
            taiSanPhamTheoDanhMuc(category.id)
        }
    }

    // ── Tải sản phẩm của một danh mục (lazy load) ────────────────────────────
    private fun taiSanPhamTheoDanhMuc(categoryId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingProducts = it.isLoadingProducts + (categoryId to true))
            }
            try {
                val snapshot = db.collection("products")
                    .whereEqualTo("categoryId", categoryId)
                    .get().await()
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Product.fromMap(data + mapOf("id" to doc.id))
                }
                _uiState.update {
                    it.copy(
                        isLoadingProducts = it.isLoadingProducts + (categoryId to false),
                        sanPhamTheoDanhMuc = it.sanPhamTheoDanhMuc + (categoryId to list)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingProducts = it.isLoadingProducts + (categoryId to false),
                        thongBaoLoi = "Không thể tải sản phẩm: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Refresh sản phẩm khi đã mở (sau khi thêm/sửa/xóa sản phẩm) ──────────
    fun laiTaiSanPham(categoryId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingProducts = it.isLoadingProducts + (categoryId to true),
                    // Xóa cache cũ để force reload
                    sanPhamTheoDanhMuc = it.sanPhamTheoDanhMuc - categoryId
                )
            }
            taiSanPhamTheoDanhMuc(categoryId)
        }
    }

    // ── Form CRUD danh mục ────────────────────────────────────────────────────
    fun moFormThem() {
        _uiState.update { it.copy(dangMoForm = true, danhMucDangSua = null) }
    }

    fun moFormSua(category: Category) {
        _uiState.update { it.copy(dangMoForm = true, danhMucDangSua = category) }
    }

    fun dongForm() {
        _uiState.update { it.copy(dangMoForm = false, danhMucDangSua = null) }
    }

    fun luuDanhMuc(tenDanhMuc: String) {
        if (tenDanhMuc.isBlank()) {
            _uiState.update { it.copy(thongBaoLoi = "Tên danh mục không được để trống") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            try {
                val dmSua = _uiState.value.danhMucDangSua
                if (dmSua == null) {
                    db.collection("categories").add(mapOf(
                        "name"      to tenDanhMuc,
                        "sortOrder" to _uiState.value.danhSachDanhMuc.size,
                        "isVisible" to true
                    )).await()
                } else {
                    db.collection("categories").document(dmSua.id)
                        .update("name", tenDanhMuc).await()
                }
                _uiState.update { it.copy(dangMoForm = false, thongBaoThanhCong = "Lưu danh mục thành công!") }
                taiDanhSachDanhMuc()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingCategories = false, thongBaoLoi = e.message) }
            }
        }
    }

    fun xoaDanhMuc(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            try {
                db.collection("categories").document(categoryId).delete().await()
                _uiState.update {
                    it.copy(
                        thongBaoThanhCong = "Xóa danh mục thành công!",
                        expandedCategoryId = if (it.expandedCategoryId == categoryId) null else it.expandedCategoryId,
                        sanPhamTheoDanhMuc = it.sanPhamTheoDanhMuc - categoryId
                    )
                }
                taiDanhSachDanhMuc()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingCategories = false, thongBaoLoi = e.message) }
            }
        }
    }

    fun xoaThongBao() {
        _uiState.update { it.copy(thongBaoLoi = null, thongBaoThanhCong = null) }
    }
}