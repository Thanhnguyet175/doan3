package com.example.milkteaapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.model.repository.OrderRepository
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

data class AdminOrderUiState(
    val isLoading: Boolean = false,
    val tatCaDonHang: List<Order> = emptyList(),  // toàn bộ đơn hàng
    val danhSachHienThi: List<Order> = emptyList(),// sau khi lọc
    val tabDangChon: TabDonHang = TabDonHang.TAT_CA,
    val donDangXem: Order? = null,                // đơn đang xem chi tiết
    val idDonDangCapNhat: String? = null,         // loading per-card
    val thongBaoThanhCong: String? = null,
    val thongBaoLoi: String? = null
)

// 3 tab hiển thị trên màn hình Admin quản lý đơn
enum class TabDonHang(val nhanHien: String) {
    TAT_CA("Tất cả"),
    CHO_XAC_NHAN("Chờ xác nhận"),
    DANG_XU_LY("Đang xử lý"),
    HOAN_THANH("Hoàn thành"), // Tùy code cũ má đang để DA_GIAO hay HOAN_THANH
    DA_HUY("Đã huỷ")
}

// ─────────────────────────────────────────────────────────
// VIEW MODEL
// ─────────────────────────────────────────────────────────

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init {
        taiTatCaDonHang()
    }

    // ── Tải dữ liệu ──────────────────────────────────────────────────────────

    fun taiTatCaDonHang() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, thongBaoLoi = null) }

            orderRepository.getAllOrders().fold(
                onSuccess = { danhSach ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading       = false,
                            tatCaDonHang    = danhSach,
                            danhSachHienThi = locTheoTab(danhSach, state.tabDangChon)
                        )
                    }
                },
                onFailure = { loi ->
                    _uiState.update { it.copy(isLoading = false, thongBaoLoi = loi.message) }
                }
            )
        }
    }

    // ── Chuyển tab ────────────────────────────────────────────────────────────

    fun chonTab(tab: TabDonHang) {
        _uiState.update { state ->
            state.copy(
                tabDangChon     = tab,
                danhSachHienThi = locTheoTab(state.tatCaDonHang, tab)
            )
        }
    }

    // ── Xem chi tiết ─────────────────────────────────────────────────────────

    fun xemChiTiet(donHang: Order) = _uiState.update { it.copy(donDangXem = donHang) }
    fun dongChiTiet()              = _uiState.update { it.copy(donDangXem = null) }

    // ── Cập nhật trạng thái ───────────────────────────────────────────────────

    // Admin có thể cập nhật bất kỳ trạng thái nào (không giới hạn như Staff)
    fun capNhatTrangThai(donHangId: String, trangThaiMoi: OrderStatus) {
        viewModelScope.launch {
            // Hiển thị loading trên đúng card đó
            _uiState.update { it.copy(idDonDangCapNhat = donHangId) }

            orderRepository.updateStatus(donHangId, trangThaiMoi).fold(
                onSuccess = {
                    // Cập nhật ngay trong danh sách local (không cần reload toàn bộ)
                    _uiState.update { state ->
                        val danhSachMoi = state.tatCaDonHang.map { don ->
                            if (don.id == donHangId) don.copy(status = trangThaiMoi) else don
                        }
                        state.copy(
                            idDonDangCapNhat  = null,
                            tatCaDonHang      = danhSachMoi,
                            danhSachHienThi   = locTheoTab(danhSachMoi, state.tabDangChon),
                            thongBaoThanhCong = "Đã cập nhật: ${trangThaiMoi.label}"
                        )
                    }
                },
                onFailure = { loi ->
                    _uiState.update {
                        it.copy(idDonDangCapNhat = null, thongBaoLoi = loi.message)
                    }
                }
            )
        }
    }

    // ── Dọn thông báo ─────────────────────────────────────────────────────────

    fun xoaThongBaoThanhCong() = _uiState.update { it.copy(thongBaoThanhCong = null) }
    fun xoaThongBaoLoi()       = _uiState.update { it.copy(thongBaoLoi = null) }

    // ── Hàm nội bộ ───────────────────────────────────────────────────────────

    // Lọc danh sách đơn hàng theo tab đang chọn
    private fun locTheoTab(danhSach: List<Order>, tab: TabDonHang): List<Order> {
        return when (tab) {
            TabDonHang.TAT_CA -> danhSach

            // Lọc riêng đơn mới chờ xác nhận
            TabDonHang.CHO_XAC_NHAN -> danhSach.filter { don ->
                don.status == OrderStatus.PENDING
            }

            // Các đơn đang trong quá trình pha chế, giao hàng
            TabDonHang.DANG_XU_LY -> danhSach.filter { don ->
                don.status in listOf(
                    OrderStatus.CONFIRMED,
                    OrderStatus.BREWING,
                    OrderStatus.READY,
                    OrderStatus.DELAYED
                )
            }

            // Đơn đã hoàn thành (Lưu ý: Nếu enum của bạn tên là HOAN_THANH thì sửa chữ DA_GIAO lại nha)
            TabDonHang.HOAN_THANH -> danhSach.filter { don ->
                don.status == OrderStatus.COMPLETED
            }

            // Đơn đã huỷ
            TabDonHang.DA_HUY -> danhSach.filter { don ->
                don.status == OrderStatus.CANCELLED
            }
        }
    }
}