package com.example.milkteaapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.repository.AuthRepository
import com.example.milkteaapp.model.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────
// UI STATE
// ─────────────────────────────────────────────────────────

data class AdminUserUiState(
    val isLoading: Boolean = false,
    val tatCaNguoiDung: List<User> = emptyList(),  // toàn bộ user
    val danhSachHienThi: List<User> = emptyList(), // sau khi lọc/tìm kiếm
    val roleDangChon: UserRole? = null,             // null = tất cả role
    val tuKhoaTim: String = "",
    val nguoiDungDangXem: User? = null,            // xem chi tiết
    val thongBaoThanhCong: String? = null,
    val thongBaoLoi: String? = null,

    // Trạng thái dialog tạo tài khoản mới
    val hienDialogTaoTaiKhoan: Boolean = false,
    val dangTaoTaiKhoan: Boolean = false
)

// ─────────────────────────────────────────────────────────
// VIEW MODEL
// ─────────────────────────────────────────────────────────

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository          // ← thêm để tạo tài khoản
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserUiState())
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    init {
        taiDanhSachNguoiDung()
    }

    // ── Tải dữ liệu ──────────────────────────────────────────────────────────

    fun taiDanhSachNguoiDung() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, thongBaoLoi = null) }

            userRepository.getAllUsers().fold(
                onSuccess = { danhSach ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading       = false,
                            tatCaNguoiDung  = danhSach,
                            danhSachHienThi = locDanhSach(danhSach, state.roleDangChon, state.tuKhoaTim)
                        )
                    }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(isLoading = false, thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Tìm kiếm & Lọc ───────────────────────────────────────────────────────

    fun timKiem(tuKhoa: String) {
        _uiState.update { state ->
            state.copy(
                tuKhoaTim       = tuKhoa,
                danhSachHienThi = locDanhSach(state.tatCaNguoiDung, state.roleDangChon, tuKhoa)
            )
        }
    }

    fun locTheoRole(role: UserRole?) {
        _uiState.update { state ->
            state.copy(
                roleDangChon    = role,
                danhSachHienThi = locDanhSach(state.tatCaNguoiDung, role, state.tuKhoaTim)
            )
        }
    }

    // ── Xem chi tiết ─────────────────────────────────────────────────────────

    fun xemNguoiDung(nguoiDung: User) = _uiState.update { it.copy(nguoiDungDangXem = nguoiDung) }
    fun dongChiTiet()                  = _uiState.update { it.copy(nguoiDungDangXem = null) }

    // ── Dialog tạo tài khoản mới ─────────────────────────────────────────────

    fun moDialogTaoTaiKhoan() = _uiState.update { it.copy(hienDialogTaoTaiKhoan = true) }
    fun dongDialogTaoTaiKhoan() = _uiState.update { it.copy(hienDialogTaoTaiKhoan = false) }

    /**
     * Admin tạo tài khoản Staff hoặc Admin mới.
     * Dùng secondary Firebase App nên Admin hiện tại KHÔNG bị đăng xuất.
     */
    fun taoTaiKhoan(
        hoTen: String,
        email: String,
        matKhau: String,
        xacNhanMatKhau: String,
        role: UserRole,           // chỉ STAFF hoặc ADMIN
        soDienThoai: String = ""
    ) {
        // ── Validation ────────────────────────────────────────────────────────
        when {
            hoTen.isBlank()  -> { loi("Vui lòng nhập họ tên."); return }
            email.isBlank()  -> { loi("Vui lòng nhập email."); return }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                -> { loi("Email không hợp lệ."); return }
            matKhau.length < 6
                -> { loi("Mật khẩu tối thiểu 6 ký tự."); return }
            matKhau != xacNhanMatKhau
                -> { loi("Mật khẩu xác nhận không khớp."); return }
            role == UserRole.CUSTOMER
                -> { loi("Không thể tạo tài khoản khách hàng từ đây."); return }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(dangTaoTaiKhoan = true, thongBaoLoi = null) }

            authRepository.registerWithRole(
                fullName    = hoTen.trim(),
                email       = email.trim(),
                password    = matKhau,
                role        = role,
                phoneNumber = soDienThoai.trim().ifBlank { null }
            ).fold(
                onSuccess = { nguoiDungMoi ->
                    // Thêm vào danh sách local ngay, không cần reload
                    _uiState.update { state ->
                        val tatCaMoi = state.tatCaNguoiDung + nguoiDungMoi
                        state.copy(
                            dangTaoTaiKhoan     = false,
                            hienDialogTaoTaiKhoan = false,
                            tatCaNguoiDung      = tatCaMoi,
                            danhSachHienThi     = locDanhSach(tatCaMoi, state.roleDangChon, state.tuKhoaTim),
                            thongBaoThanhCong   = "Đã tạo tài khoản ${tenRole(role)} cho ${nguoiDungMoi.fullName}."
                        )
                    }
                },
                onFailure = { loi ->
                    _uiState.update {
                        it.copy(
                            dangTaoTaiKhoan = false,
                            thongBaoLoi     = loi.message ?: "Tạo tài khoản thất bại."
                        )
                    }
                }
            )
        }
    }

    // ── Đổi vai trò ──────────────────────────────────────────────────────────

    fun doiVaiTro(uid: String, roleHienTai: UserRole, roleMoi: UserRole) {
        if (roleHienTai == UserRole.ADMIN && roleMoi != UserRole.ADMIN) {
            _uiState.update { it.copy(thongBaoLoi = "Không thể hạ cấp tài khoản Admin.") }
            return
        }

        viewModelScope.launch {
            userRepository.changeUserRole(uid, roleMoi).fold(
                onSuccess = {
                    capNhatNguoiDungTrongDanhSach(uid) { it.copy(role = roleMoi) }
                    _uiState.update {
                        it.copy(thongBaoThanhCong = "Đã đổi vai trò thành ${tenRole(roleMoi)}.")
                    }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Khoá / Mở khoá tài khoản ─────────────────────────────────────────────

    fun khoaTaiKhoan(uid: String) {
        viewModelScope.launch {
            userRepository.lockUser(uid).fold(
                onSuccess = {
                    capNhatNguoiDungTrongDanhSach(uid) { it.copy(isActive = false) }
                    _uiState.update { it.copy(thongBaoThanhCong = "Đã khoá tài khoản.") }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(thongBaoLoi = loi.message) }
                }
            )
        }
    }

    fun moKhoaTaiKhoan(uid: String) {
        viewModelScope.launch {
            userRepository.unlockUser(uid).fold(
                onSuccess = {
                    capNhatNguoiDungTrongDanhSach(uid) { it.copy(isActive = true) }
                    _uiState.update { it.copy(thongBaoThanhCong = "Đã mở khoá tài khoản.") }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Dọn thông báo ─────────────────────────────────────────────────────────

    fun xoaThongBaoThanhCong() = _uiState.update { it.copy(thongBaoThanhCong = null) }
    fun xoaThongBaoLoi()       = _uiState.update { it.copy(thongBaoLoi = null) }

    // ── Hàm nội bộ ───────────────────────────────────────────────────────────

    private fun locDanhSach(
        danhSach: List<User>,
        role: UserRole?,
        tuKhoa: String
    ): List<User> {
        return danhSach.filter { nguoiDung ->
            val dungRole    = role == null || nguoiDung.role == role
            val dungTuKhoa  = tuKhoa.isBlank() ||
                    nguoiDung.fullName.contains(tuKhoa.trim(), ignoreCase = true) ||
                    nguoiDung.email.contains(tuKhoa.trim(), ignoreCase = true)
            dungRole && dungTuKhoa
        }
    }

    private fun capNhatNguoiDungTrongDanhSach(uid: String, thayDoi: (User) -> User) {
        _uiState.update { state ->
            val tatCaMoi   = state.tatCaNguoiDung.map { if (it.uid == uid) thayDoi(it) else it }
            val hienThiMoi = state.danhSachHienThi.map { if (it.uid == uid) thayDoi(it) else it }
            state.copy(tatCaNguoiDung = tatCaMoi, danhSachHienThi = hienThiMoi)
        }
    }

    private fun loi(msg: String) = _uiState.update { it.copy(thongBaoLoi = msg) }

    private fun tenRole(role: UserRole): String = when (role) {
        UserRole.ADMIN    -> "Admin"
        UserRole.STAFF    -> "Nhân viên"
        UserRole.CUSTOMER -> "Khách hàng"
    }
}