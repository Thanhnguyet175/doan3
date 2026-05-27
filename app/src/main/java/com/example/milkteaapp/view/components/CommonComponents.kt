package com.example.milkteaapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.milkteaapp.model.data.OrderStatus

// ── Màu chủ đạo (đồng bộ toàn app) ─────────────────────────────────────────
private val MauNau    = Color(0xFF4E342E)
private val MauNauDam = Color(0xFF3E2723)
private val MauXam    = Color(0xFF9E9E9E)

// ─────────────────────────────────────────────────────────────────────────────
// LOADING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Màn hình loading toàn trang – dùng khi chờ dữ liệu lần đầu.
 */
@Composable
fun LoadingFullScreen(thongBao: String = "Đang tải…") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0EB)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MauNau, strokeWidth = 3.dp)
            Spacer(Modifier.height(14.dp))
            Text(thongBao, fontSize = 14.sp, color = MauXam)
        }
    }
}

/**
 * Loading nhỏ dùng inline bên trong một card hoặc section.
 */
@Composable
fun LoadingInline(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MauNau, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hiển thị khi danh sách rỗng.
 * @param icon   Emoji minh hoạ (vd: "🧋", "📦")
 * @param tieuDe Dòng chữ lớn
 * @param moTa   Dòng chữ phụ (tuỳ chọn)
 */
@Composable
fun TrangThaiRong(
    icon: String = "😅",
    tieuDe: String,
    moTa: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 48.sp)
        Text(tieuDe, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MauNauDam, textAlign = TextAlign.Center)
        if (moTa != null) {
            Text(moTa, fontSize = 13.sp, color = MauXam, textAlign = TextAlign.Center)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BADGE TRẠNG THÁI ĐƠN HÀNG
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Badge màu tương ứng với OrderStatus – dùng ở cả màn customer, staff, admin.
 */
@Composable
fun BadgeTrangThaiDon(trangThai: OrderStatus, modifier: Modifier = Modifier) {
    val mauNen = Color(android.graphics.Color.parseColor(trangThai.colorHex)).copy(alpha = 0.15f)
    val mauChu = Color(android.graphics.Color.parseColor(trangThai.colorHex))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(mauNen)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = trangThai.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = mauChu
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG XÁC NHẬN
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dialog xác nhận dùng chung (xoá, khoá, huỷ đơn…).
 * @param tieuDe       Tiêu đề dialog
 * @param noiDung      Nội dung câu hỏi
 * @param nhanXacNhan  Nhãn nút xác nhận (vd: "Xoá", "Huỷ đơn")
 * @param mauXacNhan   Màu nút xác nhận (mặc định đỏ cảnh báo)
 * @param onXacNhan    Callback khi bấm xác nhận
 * @param onHuy        Callback khi bấm huỷ / đóng
 */
@Composable
fun DialogXacNhan(
    tieuDe: String,
    noiDung: String,
    nhanXacNhan: String = "Xác nhận",
    mauXacNhan: Color = Color(0xFFEF4444),
    onXacNhan: () -> Unit,
    onHuy: () -> Unit
) {
    Dialog(onDismissRequest = onHuy) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(tieuDe, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                Text(noiDung, fontSize = 14.sp, color = MauXam)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onHuy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Huỷ") }

                    Button(
                        onClick = onXacNhan,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mauXacNhan)
                    ) { Text(nhanXacNhan, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ĐỊNH DẠNG TIỀN
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Định dạng số tiền sang chuỗi hiển thị, vd: 85000 → "85.000₫"
 */
fun formatTien(so: Long): String =
    "%,d".format(so).replace(',', '.') + "₫"

/**
 * Định dạng rút gọn, vd: 1_500_000 → "1.5M₫", 85_000 → "85k₫"
 */
fun formatTienNgan(so: Long): String = when {
    so >= 1_000_000 -> "%.1fM₫".format(so / 1_000_000.0)
    so >= 1_000     -> "${so / 1_000}k₫"
    else            -> "${so}₫"
}