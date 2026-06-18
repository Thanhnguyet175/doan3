package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
        // ─── FIXED HEADER ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MauNauDam)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🟢 ĐÃ FIX: Gắn onClick = onBack để nút Trở Về hoạt động
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
            Text("Giỏ hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.weight(1f))

            if (uiState.items.isNotEmpty()) {
                val tongSoLuongMon = uiState.items.sumOf { it.quantity }
                Text("$tongSoLuongMon món", fontSize = 13.sp, color = Color(0xFFD7CCC8))
            }
        }

        // ─── THÂN MÀN HÌNH ───────────────
        if (uiState.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Giỏ hàng trống", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.items, key = { it.cartItemId }) { item ->
                    TheCartItem(
                        item = item,
                        onTangSoLuong = { viewModel.increaseQty(item.cartItemId) },
                        onGiamSoLuong = { viewModel.decreaseQty(item.cartItemId) },
                        onXoa = { viewModel.removeItem(item.cartItemId) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.orderNote,
                        onValueChange = { text -> viewModel.setOrderNote(text) },
                        label = { Text("Ghi chú đơn hàng") },
                        placeholder = { Text("VD: Ít đường, giao trước 12h...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }

                item {
                    PhanThanhToan(
                        phuongThucHienTai = uiState.paymentMethod,
                        onChon = { phuongThuc -> viewModel.setPaymentMethod(phuongThuc) }
                    )
                }
            }

            // ─── KHU VỰC TÍNH TIỀN & NÚT ĐẶT HÀNG ──────────────────────────────
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
                        Text("${"%,d".format(uiState.subtotal.toInt())}đ", color = MauNauDam)
                    }
                    if (uiState.discountAmount > 0) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Giảm giá", color = MauXanh)
                            Text("-${"%,d".format(uiState.discountAmount)}đ", color = MauXanh)
                        }
                    }
                    HorizontalDivider()

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MauNau)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = "Đặt hàng",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
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
    // 🟢 ĐÃ FIX: Lấy đúng thuộc tính `unitPrice` từ data class CartItem
    val tongTienTungMon = item.unitPrice * item.quantity

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
            ) {
                // 🟢 ĐÃ FIX: Lấy đúng thuộc tính `productImageUrl` thay vì imageUrl trống
                AsyncImage(
                    model = item.productImageUrl,
                    contentDescription = item.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.productName, fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 14.sp)

                // 🟢 ĐÃ FIX: Hiển thị optionSummary (Size, Đường, Đá) để khách biết mình đang order cái gì
                Text(item.optionSummary, fontSize = 11.sp, color = Color.Gray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 🟢 ĐÃ FIX: Hiển thị giá
                    Text("${"%,d".format(tongTienTungMon)}đ", fontWeight = FontWeight.Bold, color = MauNau)
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
    val danhSach = listOf("CASH" to "Tiền mặt", "Chuyển khoản" to "Chuyển khoản")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Thanh toán", fontWeight = FontWeight.Bold, color = MauNauDam)
        danhSach.forEach { (ma, ten) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (phuongThucHienTai == ma) Color(0xFFEFEBE9) else Color.White)
                    .clickable { onChon(ma) }
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