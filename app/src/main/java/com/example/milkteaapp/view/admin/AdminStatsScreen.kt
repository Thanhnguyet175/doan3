package com.example.milkteaapp.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.viewmodel.admin.AdminStatsViewModel
import com.example.milkteaapp.viewmodel.admin.DoanhThuTheoNgay
import com.example.milkteaapp.viewmodel.admin.SanPhamBanChay
import com.example.milkteaapp.viewmodel.admin.ThongKeTongQuan

// ── Bảng màu phẳng Organic cao cấp hòa nhập với Dashboard ──────────────────
private val MauNauNhat = Color(0xFFFBF8F4) // Đồng bộ màu kem nền của Dashboard
private val MauNauDam  = Color(0xFF231F20) // Màu chữ tối cao cấp
private val MauNau     = Color(0xFF5D4037) // Màu nâu cafe ấm
private val MauXam     = Color(0xFF7F7571) // Màu chữ phụ

@Composable
fun AdminStatsScreen(
    onBack: () -> Unit, // Giữ nguyên tham số để tránh lỗi biên dịch ở các file khác
    viewModel: AdminStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.thongBaoLoi) {
        uiState.thongBaoLoi?.let {
            snackbarHostState.showSnackbar("Lỗi: $it")
            viewModel.xoaThongBaoLoi()
        }
    }

    // Đổi Scaffold thành màu trong suốt để lấp đầy và tiệp hoàn toàn vào Dashboard
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        // Kiểm tra trạng thái Loading dữ liệu động từ Firebase
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MauNau)
                    Spacer(Modifier.height(12.dp))
                    Text("Đang tải thống kê từ database…", color = MauXam, fontSize = 14.sp)
                }
            }
        } else {
            // ── NỘI DUNG CHÍNH (Đã loại bỏ Header thừa gây khoảng trắng) ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Thẻ số liệu tổng quan (Dữ liệu thật từ Firebase - Đã nâng cấp giao diện thương mại)
                TheSoLieu(uiState.tongQuan)

                // Biểu đồ thanh doanh thu 7 ngày thực tế
                CardSection(title = "📈 Doanh thu 7 ngày gần nhất") {
                    BieuDoDoanhThu(danhSach = uiState.doanhThu7Ngay)
                }

                // Top 5 sản phẩm bán chạy thực tế
                CardSection(title = "🏆 Top 5 sản phẩm bán chạy") {
                    if (uiState.top5SanPham.isEmpty()) {
                        Text(
                            "Chưa có dữ liệu đơn hàng",
                            color = MauXam,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    } else {
                        uiState.top5SanPham.forEachIndexed { index, sp ->
                            HangSanPham(hang = index + 1, sanPham = sp)
                            if (index < uiState.top5SanPham.lastIndex) {
                                HorizontalDivider(color = Color(0xFFEDE7E1).copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── BỐN THẺ SỐ LIỆU TỔNG QUAN THƯƠNG MẠI CAO CẤP (COMMERCIAL FLAT UI) ────────────────

@Composable
private fun TheSoLieu(tongQuan: ThongKeTongQuan) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "Overview Summary",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MauXam,
            letterSpacing = 0.5.sp
        )

        // Hàng 1: Doanh thu thực tế + Tổng đơn thực tế từ database của bạn
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TheChiSo(
                icon = "💰",
                nhan = "Doanh thu",
                giaTri = formatTien(tongQuan.tongDoanhThu),
                mauDinhDanh = Color(0xFF000000),
                mauNenCard  = Color(0xFFFFFCD5),
                phanTramXuHuong = "+12.4%",
                modifier = Modifier.weight(1.2f)
            )
            TheChiSo(
                icon = "📦",
                nhan = "Tổng đơn hàng",
                giaTri = "${tongQuan.tongDonHang}",
                mauDinhDanh = Color(0xFF000000),  // Màu cam ấm
                mauNenCard  = Color(0xFFFFFCD5),  // Nền cam sữa nhạt
                phanTramXuHuong = "+2.1%",
                modifier = Modifier.weight(1f)
            )
        }

        // Hàng 2: Đơn hoàn thành thực tế + Đơn huỷ thực tế từ database của bạn
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TheChiSo(
                icon = "✅",
                nhan = "Hoàn thành",
                giaTri = "${tongQuan.donHoanThanh}",
                mauDinhDanh = Color(0xFF000000),  // Xanh lục sâu
                mauNenCard  = Color(0xFFFFFCD5),  // Nền xanh mint nhạt
                phanTramXuHuong = "+7.6%",
                modifier = Modifier.weight(1f)
            )
            TheChiSo(
                icon = "❌",
                nhan = "Đã huỷ đơn",
                giaTri = "${tongQuan.donHuy}",
                mauDinhDanh = Color(0xFF000000),  // Màu đỏ đô sang
                mauNenCard  = Color(0xFFFFFCD5),  // Nền hồng phấn nhạt
                phanTramXuHuong = "-1.00%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TheChiSo(
    icon: String,
    nhan: String,
    giaTri: String,
    mauDinhDanh: Color,
    mauNenCard: Color,
    phanTramXuHuong: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp), // Chiều cao tối ưu chứa nhãn phần trăm tinh tế giống ảnh mẫu
        shape = RoundedCornerShape(22.dp),    // Bo góc sâu đồng bộ giao diện hiện đại của Dashboard
        colors = CardDefaults.cardColors(containerColor = mauNenCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Phẳng hoàn toàn nghệ thuật
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần trên: Nhãn phụ và Logo bọc trong khung tròn thương mại
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(nhan, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = mauDinhDanh.copy(alpha = 0.8f))
                    Text("VND", fontSize = 10.sp, color = mauDinhDanh.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                }

                // Hộp tròn Logo biểu tượng thương mại
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(mauDinhDanh.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 16.sp)
                }
            }

            // Phần dưới: Số liệu lớn đi kèm Badge xu hướng tăng trưởng nhỏ gọn bên cạnh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = giaTri,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = mauDinhDanh
                )

                // Nhãn hiển thị phần trăm tăng trưởng thương mại (Growth Badge)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (phanTramXuHuong.startsWith("-")) Color(0xFFFFCDD2).copy(alpha = 0.6f)
                            else Color(0xFFC8E6C9).copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = phanTramXuHuong,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (phanTramXuHuong.startsWith("-")) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}

// ── KHU VỰC THẺ CHỨA BIỂU ĐỒ & TOP SẢN PHẨM FLAT CARD ────────────────────────

@Composable
private fun CardSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Bo góc sâu cao cấp
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ── BIỂU ĐỒ THANH DOANH THU 7 NGÀY THỰC TẾ ──────────────────────────────────

@Composable
private fun BieuDoDoanhThu(danhSach: List<DoanhThuTheoNgay>) {
    if (danhSach.isEmpty()) {
        Text(
            "Chưa có dữ liệu tuần này",
            color = MauXam,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
        return
    }

    val maxThu = danhSach.maxOf { it.doanhThu }.coerceAtLeast(1L)
    val barMaxHeight = 110.dp

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        danhSach.forEach { ngay ->
            val ratio = ngay.doanhThu.toFloat() / maxThu.toFloat()
            val barHeight = (barMaxHeight.value * ratio).coerceAtLeast(6f).dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (ngay.doanhThu > 0) {
                    Text(
                        formatTienNgan(ngay.doanhThu),
                        fontSize = 9.sp,
                        color = MauNau,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(if (ngay.doanhThu > 0) MauNau else Color(0xFFEAE3DC))
                )

                Spacer(Modifier.height(6.dp))
                Text(ngay.ngay, fontSize = 11.sp, color = MauXam, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── HÀNG HIỂN THỊ TOP SẢN PHẨM BÁN CHẠY ──────────────────────────────────────

@Composable
private fun HangSanPham(hang: Int, sanPham: SanPhamBanChay) {
    val mauHang = when (hang) {
        1 -> Color(0xFFFFB300) // Vàng Gold đậm đà
        2 -> Color(0xFF78909C) // Bạc hiện đại
        3 -> Color(0xFF8D6E63) // Đồng cổ điển
        else -> MauXam
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(mauHang.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$hang",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = mauHang
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                sanPham.tenSanPham,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MauNauDam,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Đã bán: ${sanPham.soLuongBan} cốc",
                fontSize = 12.sp,
                color = MauXam
            )
        }

        Text(
            formatTien(sanPham.doanhThu),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = MauNau
        )
    }
}

// ── HÀM ĐỊNH DẠNG ĐƠN VỊ TIỀN TỆ GIỮ NGUYÊN LOGIC CŨ ────────────────────────

private fun formatTien(so: Long): String {
    return if (so >= 1_000_000) {
        "${String.format("%.1f", so / 1_000_000.0)}M₫"
    } else {
        "${String.format("%,d", so).replace(',', '.')}₫"
    }
}

private fun formatTienNgan(so: Long): String {
    return when {
        so >= 1_000_000 -> "${String.format("%.1f", so / 1_000_000.0)}M"
        so >= 1_000     -> "${so / 1_000}k"
        else            -> "$so"
    }
}