package com.example.milkteaapp.view.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.milkteaapp.viewmodel.staff.StaffOrderViewModel
import com.example.milkteaapp.viewmodel.staff.StaffTab

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauXanh    = Color(0xFF4A7C59)
private val MauCam     = Color(0xFFF97316)

@Composable
fun StaffDashboardScreen(
    viewModel: StaffOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiện snackbar khi có thông báo
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MauNauNhat)
                .padding(padding)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MauNauDam)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Trà Sữa NL – Nhân viên", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Quản lý đơn hàng", fontSize = 13.sp, color = Color(0xFFD7CCC8))
                }
            }

            // ── 2 Tab: Đang xử lý | Sẵn sàng ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StaffTab.entries.forEach { tab ->
                    val soLuong = if (tab == StaffTab.IN_PROGRESS) uiState.pendingCount else 0
                    NutTab(
                        nhan       = tab.label,
                        dangChon   = uiState.selectedTab == tab,
                        badge      = soLuong,
                        onClick    = { viewModel.selectTab(tab) },
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            // ── Danh sách đơn ────────────────────────────────────────────────
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MauNau)
                }
            } else if (uiState.displayedOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có đơn nào 🎉", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.displayedOrders, key = { it.id }) { don ->
                        TheDonHangStaff(
                            don             = don,
                            dangCapNhat     = uiState.updatingOrderId == don.id,
                            onBuocTiepTheo  = { viewModel.processNextStep(don.id) },
                            onTre           = { viewModel.markDelayed(don.id) },
                            onTiepTuc       = { viewModel.resumeDelayed(don.id) },
                            onNhanDon       = { viewModel.assignSelf(don.id) },
                            onBamVao        = { viewModel.selectOrder(don) }
                        )
                    }
                }
            }
        }

        // ── Dialog chi tiết đơn ───────────────────────────────────────────────
        if (uiState.selectedOrder != null) {
            DialogChiTietDonStaff(
                don    = uiState.selectedOrder!!,
                onDong = { viewModel.clearSelectedOrder() }
            )
        }
    }
}

// ── Card đơn hàng cho nhân viên ──────────────────────────────────────────────

@Composable
private fun TheDonHangStaff(
    don: Order,
    dangCapNhat: Boolean,
    onBuocTiepTheo: () -> Unit,
    onTre: () -> Unit,
    onTiepTuc: () -> Unit,
    onNhanDon: () -> Unit,
    onBamVao: () -> Unit
) {
    val mauTrangThai = Color(android.graphics.Color.parseColor(don.status.colorHex))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBamVao() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Dòng 1: Mã đơn + Badge trạng thái ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#${don.id}", fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 15.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(mauTrangThai.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(don.status.label, fontSize = 12.sp, color = mauTrangThai, fontWeight = FontWeight.Bold)
                }
            }

            // Tên khách
            Text(don.customerName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MauNauDam)

            // Danh sách món
            don.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• ${item.productName}", fontSize = 13.sp, color = Color.Gray)
                    Text("×${item.quantity}", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Divider()

            // Tổng tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL AMOUNT", fontSize = 11.sp, color = Color.LightGray)
                Text("${"%,d".format(don.finalAmount)}đ", fontWeight = FontWeight.Bold, color = MauNau)
            }

            // ── Các nút thao tác ─────────────────────────────────────────────
            if (dangCapNhat) {
                // Đang cập nhật → hiện loading thay cho các nút
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MauNau)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (don.status) {
                        // Đơn chờ: nút Nhận đơn + Xử lý
                        OrderStatus.PENDING -> {
                            OutlinedButton(
                                onClick = onNhanDon,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Nhận đơn", fontSize = 13.sp) }

                            Button(
                                onClick = onBuocTiepTheo,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MauXanh)
                            ) { Text("Xác nhận", color = Color.White, fontSize = 13.sp) }
                        }

                        // Đã xác nhận: nút Bắt đầu pha + Đánh dấu trễ
                        OrderStatus.CONFIRMED -> {
                            OutlinedButton(
                                onClick = onTre,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Bị trễ", color = MauCam, fontSize = 13.sp) }

                            Button(
                                onClick = onBuocTiepTheo,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MauNau)
                            ) { Text("Bắt đầu pha", color = Color.White, fontSize = 13.sp) }
                        }

                        // Đang pha: nút Trễ + Hoàn thành pha
                        OrderStatus.BREWING -> {
                            OutlinedButton(
                                onClick = onTre,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Bị trễ", color = MauCam, fontSize = 13.sp) }

                            Button(
                                onClick = onBuocTiepTheo,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MauXanh)
                            ) { Text("Sẵn sàng", color = Color.White, fontSize = 13.sp) }
                        }

                        // Bị trễ: nút Tiếp tục xử lý
                        OrderStatus.DELAYED -> {
                            Button(
                                onClick = onTiepTuc,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MauCam)
                            ) { Text("Tiếp tục xử lý", color = Color.White, fontSize = 13.sp) }
                        }

                        // Sẵn sàng giao: nút Hoàn thành đơn
                        OrderStatus.READY -> {
                            Button(
                                onClick = onBuocTiepTheo,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MauXanh)
                            ) { Text("Hoàn thành đơn", color = Color.White, fontSize = 13.sp) }
                        }

                        else -> { /* COMPLETED / CANCELLED → không hiện nút */ }
                    }
                }
            }
        }
    }
}

// ── Dialog chi tiết đơn ──────────────────────────────────────────────────────

@Composable
private fun DialogChiTietDonStaff(don: Order, onDong: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDong,
        title = {
            Column {
                Text("Đơn #${don.id}", fontWeight = FontWeight.Bold)
                Text(don.customerName, fontSize = 13.sp, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                don.items.forEach { item ->
                    Column {
                        Text("${item.productName} ×${item.quantity}", fontWeight = FontWeight.Medium)
                        // Hiển thị đầy đủ tuỳ chọn: size, đường, đá
                        Text(
                            "${item.size.label} | Đường ${item.sugarLevel.label} | ${item.iceLevel.label}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (item.selectedToppings.isNotEmpty()) { // 🟢 ĐÃ FIX: Đổi toppings thành selectedToppings
                            Text(
                                text = "Topping: ${item.selectedToppings.joinToString { it.name }}", // 🟢 ĐÃ FIX: Tương tự ở đây
                                fontSize = 12.sp,
                                color = Color(0xFF795548)
                            )
                        }
                        if (item.note.isNotBlank()) {
                            Text("📝 ${item.note}", fontSize = 12.sp, color = Color(0xFF795548))
                        }
                    }
                }
                Divider()
                if (don.note.isNotBlank()) {
                    Text("Ghi chú đơn: ${don.note}", fontSize = 13.sp, color = Color.Gray)
                }
                Text(
                    "Tổng: ${"%,d".format(don.finalAmount)}đ",
                    fontWeight = FontWeight.Bold,
                    color = MauNau
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDong) { Text("Đóng", color = MauNau) }
        }
    )
}

// ── Nút tab có badge ─────────────────────────────────────────────────────────

@Composable
private fun NutTab(
    nhan: String,
    dangChon: Boolean,
    badge: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (dangChon) MauNau else Color(0xFFD7CCC8),
                contentColor   = if (dangChon) Color.White else MauNauDam
            )
        ) {
            Text(nhan, fontSize = 13.sp, fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Normal)
        }
        // Badge số đơn đang chờ
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE57373)),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}