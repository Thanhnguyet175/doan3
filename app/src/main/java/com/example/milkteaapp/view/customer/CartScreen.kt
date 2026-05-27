package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.model.data.CartItem
import com.example.milkteaapp.viewmodel.customer.CartViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauXanh    = Color(0xFF4A7C59)

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onDatHangThanhCong: () -> Unit,
    viewModel: CartViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.orderPlaced) {
        if (uiState.orderPlaced) {
            onDatHangThanhCong()
            viewModel.onOrderPlacedHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
    ) {
        // 🟢 CỐ ĐỊNH HEADER TRÊN CÙNG: Dù giỏ hàng có rỗng hay không thì nút quay lại vẫn hiển thị chuẩn bài!
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MauNauDam)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
            Text("Giỏ hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.weight(1f))

            if (!uiState.isEmpty) {
                Text("${uiState.itemCount} món", fontSize = 13.sp, color = Color(0xFFD7CCC8))
            }
        }

        // Tách biệt luồng thân màn hình xuống phía dưới thanh Header chung
        if (uiState.isEmpty) {
            // Giao diện khi Giỏ hàng trống (Vẫn giữ được thanh Back ở trên cùng)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Giỏ hàng trống", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            // Danh sách món trong giỏ khi có hàng
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.items, key = { it.cartItemId }) { item ->
                    TheCartItem(
                        item       = item,
                        onTangSoLuong  = { viewModel.increaseQty(item.cartItemId) },
                        onGiamSoLuong  = { viewModel.decreaseQty(item.cartItemId) },
                        onXoa          = { viewModel.removeItem(item.cartItemId) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.orderNote,
                        onValueChange = { viewModel.setOrderNote(it) },
                        label = { Text("Ghi chú đơn hàng") },
                        placeholder = { Text("VD: Giao trước 12h...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }

                item {
                    PhanThanhToan(
                        phuongThucHienTai = uiState.paymentMethod,
                        onChon            = { viewModel.setPaymentMethod(it) }
                    )
                }
            }

            // Thanh tính tiền + nút đặt hàng dính dưới đáy màn hình
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tạm tính", color = Color.Gray)
                        Text("${"%,d".format(uiState.subtotal)}đ", color = MauNauDam)
                    }
                    if (uiState.discountAmount > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Giảm giá", color = MauXanh)
                            Text("-${"%,d".format(uiState.discountAmount)}đ", color = MauXanh)
                        }
                    }
                    HorizontalDivider() // Đã đổi sang bản Material3 chuẩn thay cho Divider cũ

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", fontWeight = FontWeight.Bold, color = MauNauDam)
                        Text(
                            "${"%,d".format(uiState.finalAmount)}đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MauNau
                        )
                    }

                    if (uiState.errorMessage != null) {
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.placeOrder() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MauNau)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Đặt hàng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TheCartItem(
    item: CartItem,
    onTangSoLuong: () -> Unit,
    onGiamSoLuong: () -> Unit,
    onXoa: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEBE9)),
                contentAlignment = Alignment.Center
            ) { Text("☕", fontSize = 24.sp) }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.productName, fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 14.sp)
                Text(item.optionSummary, fontSize = 12.sp, color = Color.Gray)
                if (item.note.isNotBlank()) {
                    Text("📝 ${item.note}", fontSize = 11.sp, color = Color(0xFF795548))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${"%,d".format(item.subtotal)}đ", fontWeight = FontWeight.Bold, color = MauNau)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onGiamSoLuong, contentPadding = PaddingValues(4.dp)) {
                            Text("-", fontSize = 18.sp, color = MauNau)
                        }
                        Text("${item.quantity}", fontWeight = FontWeight.Bold, color = MauNauDam)
                        TextButton(onClick = onTangSoLuong, contentPadding = PaddingValues(4.dp)) {
                            Text("+", fontSize = 18.sp, color = MauNau)
                        }
                    }
                }
            }

            IconButton(onClick = onXoa, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = Color(0xFFE57373))
            }
        }
    }
}

@Composable
private fun PhanThanhToan(phuongThucHienTai: String, onChon: (String) -> Unit) {
    val danhSach = listOf("CASH" to "💵 Tiền mặt", "MOMO" to "🟣 MoMo", "ZALOPAY" to "🔵 ZaloPay")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Thanh toán", fontWeight = FontWeight.Bold, color = MauNauDam)
        danhSach.forEach { (ma, ten) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (phuongThucHienTai == ma) Color(0xFFEFEBE9) else Color.White)
                    .clickable { onChon(ma) } // 🟢 Bổ sung thêm clickable toàn vùng chọn cho khách hàng dễ ấn chọn
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = phuongThucHienTai == ma,
                    onClick  = { onChon(ma) },
                    colors   = RadioButtonDefaults.colors(selectedColor = MauNau)
                )
                Text(ten, fontSize = 14.sp, color = MauNauDam)
            }
        }
    }
}