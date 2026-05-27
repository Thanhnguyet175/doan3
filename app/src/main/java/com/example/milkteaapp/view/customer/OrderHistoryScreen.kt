package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    // Hiện Snackbar khi huỷ đơn thành công
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) {
            snackbarHostState.showSnackbar("Đã huỷ đơn hàng.")
            viewModel.onCancelHandled()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MauNauNhat)
                .padding(padding)
        ) {
            // ── Tiêu đề ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MauNauDam)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text("Lịch sử đơn hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // ── Tabs lọc trạng thái ───────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab "Tất cả"
                item {
                    TabTrangThai(
                        nhan     = "Tất cả",
                        dangChon = uiState.selectedStatus == null,
                        onClick  = { viewModel.filterByStatus(null) }
                    )
                }
                // Tab từng trạng thái
                items(OrderStatus.entries) { trangThai ->
                    TabTrangThai(
                        nhan     = trangThai.label,
                        dangChon = uiState.selectedStatus == trangThai,
                        onClick  = { viewModel.filterByStatus(trangThai) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MauNau)
                }
            } else if (uiState.filteredOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có đơn hàng nào 🫙", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredOrders, key = { it.id }) { don ->
                        TheDonHang(
                            don      = don,
                            onBamVao = { viewModel.selectOrder(don) },
                            onHuy    = { viewModel.cancelOrder(don.id) }
                        )
                    }
                }
            }
        }

        // ── Dialog xem chi tiết đơn ────────────────────────────────────────
        if (uiState.selectedOrder != null) {
            DialogChiTietDon(
                don    = uiState.selectedOrder!!,
                onDong = { viewModel.clearSelectedOrder() }
            )
        }
    }
}

// ── Card đơn hàng ────────────────────────────────────────────────────────────

@Composable
private fun TheDonHang(don: Order, onBamVao: () -> Unit, onHuy: () -> Unit) {
    // Lấy màu badge từ OrderStatus
    val mauTrangThai = Color(android.graphics.Color.parseColor(don.status.colorHex))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBamVao() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mã đơn + badge trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${don.id}", fontWeight = FontWeight.Bold, color = MauNauDam)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(mauTrangThai.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(don.status.label, fontSize = 12.sp, color = mauTrangThai, fontWeight = FontWeight.Bold)
                }
            }

            // Danh sách món
            don.items.forEach { item ->
                Text("• ${item.productName} ×${item.quantity}", fontSize = 13.sp, color = Color.Gray)
            }

            Divider()

            // Tổng tiền + nút huỷ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${"%,d".format(don.finalAmount)}đ",
                    fontWeight = FontWeight.Bold,
                    color = MauNau,
                    fontSize = 16.sp
                )
                // Chỉ hiện nút Huỷ khi đơn đang PENDING
                if (don.status == OrderStatus.PENDING) {
                    OutlinedButton(
                        onClick = onHuy,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373))
                    ) {
                        Text("Huỷ đơn", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ── Dialog chi tiết đơn ──────────────────────────────────────────────────────

@Composable
private fun DialogChiTietDon(don: Order, onDong: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDong,
        title = { Text("Chi tiết đơn #${don.id}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                don.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} ×${item.quantity}", fontSize = 14.sp)
                        Text("${"%,d".format(item.subtotal)}đ", fontSize = 14.sp, color = MauNau)
                    }
                }
                Divider()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng", fontWeight = FontWeight.Bold)
                    Text("${"%,d".format(don.finalAmount)}đ", fontWeight = FontWeight.Bold, color = MauNau)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDong) { Text("Đóng", color = MauNau) }
        }
    )
}

// ── Tab lọc trạng thái ───────────────────────────────────────────────────────

@Composable
private fun TabTrangThai(nhan: String, dangChon: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (dangChon) MauNau else Color(0xFFD7CCC8))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(nhan, color = if (dangChon) Color.White else MauNauDam, fontSize = 12.sp)
    }
}