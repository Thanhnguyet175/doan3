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
    // Lấy State thực tế từ ViewModel quản lý giỏ hàng
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Xử lý chuyển trang sau khi đặt hàng thành công
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

        // ─── THÂN MÀN HÌNH (Hiển thị danh sách hoặc Báo trống) ───────────────
        if (uiState.items.isEmpty()) {
            // Khi giỏ hàng thực sự chưa có món nào
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Giỏ hàng trống", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            // Khi đã thêm sản phẩm thành công vào giỏ hàng
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

                // Phần nhập ghi chú đơn hàng của người dùng
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

                // Phần lựa chọn hình thức thanh toán
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
    // Tính tổng tiền cho item này dựa trên giá sản phẩm thực tế
    // Lưu ý: Nếu trong class CartItem của bạn biến chứa giá tên là 'unitPrice' hay 'basePrice' thì hãy đổi 'price' thành tên tương ứng nhé
    val tongTienTungMon = item.price * item.quantity

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
            // Hiển thị hình ảnh từ thuộc tính imageUrl thực tế của CartItem
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEBE9)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.productName, fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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