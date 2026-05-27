package com.example.milkteaapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import java.text.SimpleDateFormat
import java.util.Locale

// ── Màu chủ đạo ─────────────────────────────────────────────────────────────
private val MauNau    = Color(0xFF4E342E)
private val MauNauDam = Color(0xFF3E2723)
private val MauXam    = Color(0xFF9E9E9E)

// ─────────────────────────────────────────────────────────────────────────────
// ORDER CARD – dùng chung ở OrderHistoryScreen (customer) và AdminOrderScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Card hiển thị tóm tắt một đơn hàng.
 *
 * @param donHang       Đơn hàng cần hiển thị
 * @param hienThiKhach  true → hiển thị tên người đặt (dùng ở màn admin/staff)
 * @param onClick       Callback khi bấm vào card (xem chi tiết)
 * @param actionContent Slot tuỳ chọn – nút hành động nằm ở cuối card
 *                      (vd: nút "Xác nhận", "Huỷ" ở màn staff/admin)
 */
@Composable
fun OrderCard(
    donHang: Order,
    hienThiKhach: Boolean = false,
    onClick: (() -> Unit)? = null,
    actionContent: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val mauTrangThai = Color(android.graphics.Color.parseColor(donHang.status.colorHex))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ── Hàng trên: mã đơn + badge trạng thái ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon loại đơn
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(mauTrangThai.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconTrangThai(donHang.status), fontSize = 18.sp)
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Đơn #${donHang.id.takeLast(6).uppercase()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MauNauDam
                    )
                    Text(
                        formatNgay(donHang.createdAt),
                        fontSize = 11.sp,
                        color = MauXam
                    )
                }

                BadgeTrangThaiDon(trangThai = donHang.status)
            }

            // ── Tên khách hàng (chỉ hiện ở màn admin/staff) ───────────────────
            if (hienThiKhach && donHang.customerName.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👤", fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(donHang.customerName, fontSize = 13.sp, color = MauNauDam)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEDE7E1))
            Spacer(Modifier.height(10.dp))

            // ── Danh sách sản phẩm tóm tắt ───────────────────────────────────
            donHang.items.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• ", color = MauXam, fontSize = 13.sp)
                    Text(
                        "${item.productName}",
                        fontSize = 13.sp,
                        color = MauNauDam,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "x${item.quantity}",
                        fontSize = 13.sp,
                        color = MauXam,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Nếu nhiều hơn 3 món
            if (donHang.items.size > 3) {
                Text(
                    "+${donHang.items.size - 3} món khác…",
                    fontSize = 12.sp,
                    color = MauXam,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEDE7E1))
            Spacer(Modifier.height(10.dp))

            // ── Tổng tiền ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${donHang.items.sumOf { it.quantity }} món",
                    fontSize = 13.sp,
                    color = MauXam
                )
                Text(
                    formatTien(donHang.finalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MauNau
                )
            }

            // ── Slot hành động tuỳ chỉnh (nếu có) ────────────────────────────
            if (actionContent != null) {
                Spacer(Modifier.height(10.dp))
                actionContent()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hàm phụ
// ─────────────────────────────────────────────────────────────────────────────

private fun iconTrangThai(status: OrderStatus): String = when (status) {
    OrderStatus.PENDING   -> "🕐"
    OrderStatus.CONFIRMED -> "✅"
    OrderStatus.BREWING   -> "🧋"
    OrderStatus.READY     -> "🔔"
    OrderStatus.COMPLETED -> "🎉"
    OrderStatus.CANCELLED -> "❌"
    OrderStatus.DELAYED   -> "⏳"
}

private fun formatNgay(timestamp: com.google.firebase.Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}