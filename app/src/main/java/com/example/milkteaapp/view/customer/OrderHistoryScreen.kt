package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.viewmodel.customer.OrderHistoryViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)

@Composable
fun OrderHistoryScreen(
    onBack: () -> Unit,
    viewModel: OrderHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            snackbarHostState.showSnackbar("Đã huỷ đơn hàng.")
            viewModel.onCancelHandled()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MauNauNhat)
                // CHỈ giữ lại padding đáy, XOÁ luôn dòng .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // ── Tiêu đề ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MauNauDam)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
// ... phần dưới giữ nguyên ...
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                }
                Text("Lịch sử đơn hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // ── Tabs lọc trạng thái ───────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TabTrangThai(
                        nhan     = "Tất cả",
                        dangChon = uiState.selectedStatus == null,
                        onClick  = { viewModel.filterByStatus(null) }
                    )
                }
                items(OrderStatus.entries) { trangThai ->
                    TabTrangThai(
                        nhan     = trangThai.label,
                        dangChon = uiState.selectedStatus == trangThai,
                        onClick  = { viewModel.filterByStatus(trangThai) }
                    )
                }
            }

            // ── Nội dung chính (Chỉ dùng dữ liệu thật) ────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MauNau)
                }
            } else if (uiState.filteredOrders.isEmpty()) {
                // Giao diện khi trống đơn hàng
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có đơn hàng nào ở trạng thái này 🫙", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredOrders, key = { it.id }) { don ->
                        TheDonHangThat(
                            don      = don,
                            onBamVao = { viewModel.selectOrder(don) },
                            onHuy    = { viewModel.cancelOrder(don.id) }
                        )
                    }
                }
            }
        }

        // ── Dialog xem chi tiết đơn ──────────────────────────────────────────
        if (uiState.selectedOrder != null) {
            DialogChiTietDon(
                don    = uiState.selectedOrder!!,
                onDong = { viewModel.clearSelectedOrder() }
            )
        }
    }
}

// ── COMPOSABLE: Thẻ Đơn Hàng ─────────────────────────────────────────────────
@Composable
private fun TheDonHangThat(don: Order, onBamVao: () -> Unit, onHuy: () -> Unit) {
    val mauTrangThai = try {
        Color(android.graphics.Color.parseColor(don.status.colorHex))
    } catch (e: Exception) { Color.Gray }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onBamVao() }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: ID và Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Mã đơn: #${don.id}", fontWeight = FontWeight.ExtraBold, color = MauNauDam, fontSize = 15.sp)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(mauTrangThai.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(don.status.label, fontSize = 12.sp, color = mauTrangThai, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color(0xFFF5F0EB))

            // Danh sách món ăn
            don.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEFEBE9))) {
                        AsyncImage(
                            model = item.productImageUrl, // Lấy trực tiếp link ảnh thật
                            contentDescription = item.productName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink),
                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MauNauDam)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Số lượng: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("${"%,d".format(item.subtotal)}đ", fontWeight = FontWeight.Bold, color = MauNau)
                }
            }

            HorizontalDivider(color = Color(0xFFF5F0EB))

            // Footer: Tổng tiền + Nút huỷ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Tổng cộng", fontSize = 12.sp, color = Color.Gray)
                    Text("${"%,d".format(don.finalAmount)}đ", fontWeight = FontWeight.ExtraBold, color = MauNau, fontSize = 18.sp)
                }
                if (don.status == OrderStatus.PENDING) {
                    OutlinedButton(
                        onClick = onHuy,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373))
                    ) {
                        Text("Huỷ đơn", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Tab lọc trạng thái và Dialog ─────────────────────────────────────────────

@Composable
private fun DialogChiTietDon(don: Order, onDong: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDong,
        title = { Text("Chi tiết đơn #${don.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                don.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} ×${item.quantity}", fontSize = 14.sp)
                        Text("${"%,d".format(item.subtotal)}đ", fontSize = 14.sp, color = MauNau)
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng", fontWeight = FontWeight.Bold)
                    Text("${"%,d".format(don.finalAmount)}đ", fontWeight = FontWeight.Bold, color = MauNau)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDong) { Text("Đóng", color = MauNau) } }
    )
}

@Composable
private fun TabTrangThai(nhan: String, dangChon: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (dangChon) MauNau else Color(0xFFD7CCC8))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(nhan, color = if (dangChon) Color.White else MauNauDam, fontSize = 13.sp, fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Medium)
    }
}