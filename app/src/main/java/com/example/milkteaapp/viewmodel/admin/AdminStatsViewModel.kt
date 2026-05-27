package com.example.milkteaapp.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.model.repository.OrderRepository
import com.example.milkteaapp.model.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────
// DATA CLASS PHỤ – chứa thống kê đã tính toán
// ─────────────────────────────────────────────────────────

// Thống kê tổng quan hiển thị ở đầu trang
data class ThongKeTongQuan(
    val tongDoanhThu: Long = 0L,        // tổng tiền tất cả đơn hoàn thành
    val tongDonHang: Int = 0,           // tổng số đơn
    val donHoanThanh: Int = 0,          // số đơn đã xong
    val donHuy: Int = 0                 // số đơn bị huỷ
)

// Doanh thu theo từng ngày (dùng để vẽ biểu đồ đường)
data class DoanhThuTheoNgay(
    val ngay: String = "",              // vd: "13/05"
    val doanhThu: Long = 0L
)

// Sản phẩm bán chạy (dùng để vẽ biểu đồ cột)
data class SanPhamBanChay(
    val tenSanPham: String = "",
    val soLuongBan: Int = 0,
    val doanhThu: Long = 0L
)

// ─────────────────────────────────────────────────────────
// UI STATE
// ─────────────────────────────────────────────────────────

data class AdminStatsUiState(
    val isLoading: Boolean = false,
    val tongQuan: ThongKeTongQuan = ThongKeTongQuan(),
    val doanhThu7Ngay: List<DoanhThuTheoNgay> = emptyList(),  // biểu đồ 7 ngày gần nhất
    val top5SanPham: List<SanPhamBanChay> = emptyList(),      // top 5 sản phẩm bán chạy
    val thongBaoLoi: String? = null
)

// ─────────────────────────────────────────────────────────
// VIEW MODEL
// ─────────────────────────────────────────────────────────

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStatsUiState())
    val uiState: StateFlow<AdminStatsUiState> = _uiState.asStateFlow()

    init {
        taiThongKe()
    }

    // ── Tải & tính toán ──────────────────────────────────────────────────────

    fun taiThongKe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, thongBaoLoi = null) }

            // Tải song song để nhanh hơn
            val donHangDeferred   = async { orderRepository.getAllOrders() }
            val sanPhamDeferred   = async { productRepository.getAvailableProducts() }

            val tatCaDon  = donHangDeferred.await().getOrDefault(emptyList())
            val sanPhamList = sanPhamDeferred.await().getOrDefault(emptyList())

            // Tính các thống kê từ dữ liệu vừa tải về
            val tongQuan     = tinhTongQuan(tatCaDon)
            val doanhThu7Ngay = tinhDoanhThu7Ngay(tatCaDon)
            val top5          = tinhTop5SanPham(tatCaDon)

            _uiState.update {
                it.copy(
                    isLoading      = false,
                    tongQuan       = tongQuan,
                    doanhThu7Ngay  = doanhThu7Ngay,
                    top5SanPham    = top5
                )
            }
        }
    }

    // ── Các hàm tính toán (private) ───────────────────────────────────────────

    // Tính thống kê tổng quan: doanh thu, số đơn, hoàn thành, huỷ
    private fun tinhTongQuan(danhSachDon: List<Order>): ThongKeTongQuan {
        // Chỉ tính doanh thu từ đơn đã hoàn thành
        val donHoanThanh = danhSachDon.filter { it.status == OrderStatus.COMPLETED }
        val tongDoanhThu = donHoanThanh.sumOf { it.finalAmount }
        val soHuy        = danhSachDon.count { it.status == OrderStatus.CANCELLED }

        return ThongKeTongQuan(
            tongDoanhThu = tongDoanhThu,
            tongDonHang  = danhSachDon.size,
            donHoanThanh = donHoanThanh.size,
            donHuy       = soHuy
        )
    }

    // Tính doanh thu từng ngày trong 7 ngày gần nhất
    private fun tinhDoanhThu7Ngay(danhSachDon: List<Order>): List<DoanhThuTheoNgay> {
        // Tạo danh sách 7 ngày gần nhất (hôm nay → 6 ngày trước)
        val calendar = java.util.Calendar.getInstance()
        val format   = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        // Nhóm đơn hoàn thành theo ngày (key = "dd/MM")
        val donHoanThanh = danhSachDon.filter { it.status == OrderStatus.COMPLETED }
        val nhomTheoNgay = donHoanThanh.groupBy { don ->
            val cal = java.util.Calendar.getInstance()
            cal.time = don.createdAt.toDate()
            format.format(cal.time)
        }

        // Tạo danh sách 7 ngày, điền 0 nếu ngày đó không có đơn
        return (6 downTo 0).map { soNgayTruoc ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -soNgayTruoc)
            val nhanNgay   = format.format(cal.time)
            val doanhThu   = nhomTheoNgay[nhanNgay]?.sumOf { it.finalAmount } ?: 0L
            DoanhThuTheoNgay(ngay = nhanNgay, doanhThu = doanhThu)
        }
    }

    // Tính top 5 sản phẩm bán chạy nhất theo doanh thu
    private fun tinhTop5SanPham(danhSachDon: List<Order>): List<SanPhamBanChay> {
        // Gom tất cả các dòng sản phẩm trong đơn đã hoàn thành
        val donHoanThanh = danhSachDon.filter { it.status == OrderStatus.COMPLETED }

        // Nhóm theo tên sản phẩm, tính tổng số lượng và doanh thu
        val nhom = mutableMapOf<String, SanPhamBanChay>()

        donHoanThanh.forEach { don ->
            don.items.forEach { item ->
                val hienTai = nhom[item.productName]
                nhom[item.productName] = if (hienTai == null) {
                    // Lần đầu gặp sản phẩm này
                    SanPhamBanChay(
                        tenSanPham  = item.productName,
                        soLuongBan  = item.quantity,
                        doanhThu    = item.subtotal
                    )
                } else {
                    // Cộng dồn vào kết quả cũ
                    hienTai.copy(
                        soLuongBan = hienTai.soLuongBan + item.quantity,
                        doanhThu   = hienTai.doanhThu + item.subtotal
                    )
                }
            }
        }

        // Sắp xếp giảm dần theo doanh thu, lấy top 5
        return nhom.values
            .sortedByDescending { it.doanhThu }
            .take(5)
    }

    // ── Dọn thông báo ─────────────────────────────────────────────────────────

    fun xoaThongBaoLoi() = _uiState.update { it.copy(thongBaoLoi = null) }
}