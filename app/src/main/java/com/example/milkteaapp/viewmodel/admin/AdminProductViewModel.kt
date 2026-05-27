package com.example.milkteaapp.viewmodel.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.model.source.CloudinarySource // 1. ĐÃ SỬA: Đúng package source
import com.example.milkteaapp.model.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.milkteaapp.model.data.Category // <-- THÊM DÒNG NÀY
// ─────────────────────────────────────────────────────────
// UI STATE
// ─────────────────────────────────────────────────────────

data class AdminProductUiState(
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val danhSachSanPham: List<Product> = emptyList(),
    val danhSachHienThi: List<Product> = emptyList(),
    val danhSachDanhMuc: List<Category> = emptyList(),
    val tuKhoaTim: String = "",
    val sanPhamDangSua: Product? = null,
    val dangMoForm: Boolean = false,
    val selectedImageUri: Uri? = null,
    val thongBaoThanhCong: String? = null,
    val thongBaoLoi: String? = null
)

// ─────────────────────────────────────────────────────────
// VIEW MODEL
// ─────────────────────────────────────────────────────────

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storageSource: CloudinarySource,
    private val db: com.google.firebase.firestore.FirebaseFirestore, // Đảm bảo inject thêm DB để lấy categories nhanh
    @ApplicationContext private val context: Context // Inject Context để hỗ trợ đọc Uri từ máy thành đường dẫn file
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductUiState())
    val uiState: StateFlow<AdminProductUiState> = _uiState.asStateFlow()

    init {
        taiDanhSachSanPham()
        taiDanhSachDanhMuc()
    }
    fun taiDanhSachDanhMuc() {
        viewModelScope.launch {
            try {
                db.collection("categories").get().addOnSuccessListener { snapshot ->
                    // SỬA TẠI ĐÂY: Chuyển đổi Firestore document sang đối tượng Category và gán ID tài liệu vào luôn
                    val listCategories = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    }
                    _uiState.update { it.copy(danhSachDanhMuc = listCategories) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun taiDanhSachSanPham() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, thongBaoLoi = null) }
            productRepository.getAvailableProducts().fold(
                onSuccess = { danhSach ->
                    _uiState.update {
                        it.copy(
                            isLoading       = false,
                            danhSachSanPham = danhSach,
                            danhSachHienThi = danhSach
                        )
                    }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(isLoading = false, thongBaoLoi = loi.message) }
                }
            )
        }
    }

    fun timKiem(tuKhoa: String) {
        _uiState.update { it.copy(tuKhoaTim = tuKhoa) }
        locDanhSach()
    }

    private fun locDanhSach() {
        val state = _uiState.value
        val ketQua = if (state.tuKhoaTim.isBlank()) {
            state.danhSachSanPham
        } else {
            state.danhSachSanPham.filter { sp ->
                sp.name.contains(state.tuKhoaTim.trim(), ignoreCase = true)
            }
        }
        _uiState.update { it.copy(danhSachHienThi = ketQua) }
    }

    // ── Thêm / Sửa ───────────────────────────────────────────────────────────

    fun batDauThemMoi() {
        _uiState.update { it.copy(sanPhamDangSua = Product(), dangMoForm = true, selectedImageUri = null) }
    }

    fun batDauSua(sanPham: Product) {
        _uiState.update { it.copy(sanPhamDangSua = sanPham, dangMoForm = true, selectedImageUri = null) }
    }

    /** Gọi khi user chọn ảnh từ gallery/camera */
    fun chonAnh(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    /** Helper chuyển đổi Uri thành file Path tạm thời để Cloudinary upload */
    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val filePath = context.cacheDir.absolutePath + "/temp_upload_image.jpg"
            val file = java.io.File(filePath)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Lưu sản phẩm: upload ảnh nếu có URI mới, sau đó lưu Firestore */
    fun luuSanPham(sanPham: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Upload ảnh mới nếu user đã chọn file từ máy
            val imageUrl: String? = if (_uiState.value.selectedImageUri != null) {
                _uiState.update { it.copy(isUploadingImage = true) }

                val path = getFilePathFromUri(_uiState.value.selectedImageUri!!)
                if (path == null) {
                    _uiState.update { it.copy(isLoading = false, isUploadingImage = false, thongBaoLoi = "Không thể đọc tệp tin ảnh từ thiết bị.") }
                    return@launch
                }

                runCatching {
                    // 2. ĐÃ SỬA: Truyền đường dẫn String (path) vào cho Class CloudinarySource xử lý
                    storageSource.uploadImage(path)
                }.getOrElse { loi ->
                    _uiState.update {
                        it.copy(isLoading = false, isUploadingImage = false,
                            thongBaoLoi = "Upload ảnh thất bại: ${loi.message}")
                    }
                    return@launch
                }.also {
                    _uiState.update { s -> s.copy(isUploadingImage = false) }
                }
            } else {
                sanPham.imageUrl   // giữ nguyên URL cũ nếu không đổi ảnh
            }

            val sanPhamFinal = sanPham.copy(imageUrl = imageUrl)

            val ketQua = if (sanPham.id.isBlank()) {
                productRepository.addProduct(sanPhamFinal)
            } else {
                productRepository.updateProduct(sanPhamFinal).map { sanPhamFinal }
            }

            ketQua.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading         = false,
                            dangMoForm        = false,
                            sanPhamDangSua    = null,
                            selectedImageUri  = null,
                            thongBaoThanhCong = "Lưu sản phẩm thành công!"
                        )
                    }
                    taiDanhSachSanPham()
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(isLoading = false, thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Ẩn / Hiện ────────────────────────────────────────────────────────────

    fun doiTrangThaiBan(sanPhamId: String, dangBan: Boolean) {
        viewModelScope.launch {
            productRepository.setProductAvailability(sanPhamId, dangBan).fold(
                onSuccess = {
                    val trangThai = if (dangBan) "đang bán" else "tạm ẩn"
                    _uiState.update { it.copy(thongBaoThanhCong = "Sản phẩm đã được $trangThai.") }
                    taiDanhSachSanPham()
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Xoá ──────────────────────────────────────────────────────────────────

    fun xoaSanPham(sanPham: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Xoá ảnh trên Cloudinary nếu link ảnh chứa từ khoá cloudinary
            sanPham.imageUrl?.let { url ->
                if (url.contains("cloudinary")) {
                    // 3. ĐÃ SỬA: Đảm bảo cơ chế không crash khi gọi hàm xoá ảnh
                    storageSource.deleteImageByUrl(url)
                }
            }
            productRepository.deleteProduct(sanPham.id).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, thongBaoThanhCong = "Đã xoá sản phẩm.") }
                    taiDanhSachSanPham()
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(isLoading = false, thongBaoLoi = loi.message) }
                }
            )
        }
    }
    private fun deleteImageByUrl(url: String) {
        viewModelScope.launch {
            try {
                // Gọi sang Cloudinary để xóa, tạm thời bọc trong try-catch để tránh crash app nếu Cloudinary chưa cấu hình xóa
                storageSource.deleteImageByUrl(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // ── Dọn dẹp ──────────────────────────────────────────────────────────────

    fun xoaThongBaoThanhCong() = _uiState.update { it.copy(thongBaoThanhCong = null) }
    fun xoaThongBaoLoi()       = _uiState.update { it.copy(thongBaoLoi = null) }
    fun dongForm()             = _uiState.update { it.copy(dangMoForm = false, sanPhamDangSua = null, selectedImageUri = null) }
}