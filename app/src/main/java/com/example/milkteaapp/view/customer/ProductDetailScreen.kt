package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.milkteaapp.model.data.DrinkSize
import com.example.milkteaapp.model.data.IceLevel
import com.example.milkteaapp.model.data.SugarLevel
import com.example.milkteaapp.model.data.Topping
import com.example.milkteaapp.viewmodel.customer.CartViewModel
import com.example.milkteaapp.viewmodel.customer.DetailViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauXanh    = Color(0xFF4A7C59)

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    // CartViewModel dùng chung toàn app (không tạo mới)
    cartViewModel: CartViewModel,
    detailViewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

    // Khi addedToCart = true → báo CartViewModel thêm item
    LaunchedEffect(uiState.addedToCart) {
        if (uiState.addedToCart) {
            val item = detailViewModel.buildCartItem()
            if (item != null) cartViewModel.addItem(item)
            detailViewModel.onAddedToCartHandled()
        }
    }

    if (uiState.isLoading || uiState.product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MauNau)
        }
        return
    }

    val sanPham = uiState.product!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
    ) {
        // ── Nút quay lại ─────────────────────────────────────────────────────
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
            Text(sanPham.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // ── Nội dung cuộn được ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Ảnh sản phẩm (placeholder màu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MauNau),
                contentAlignment = Alignment.Center
            ) {
                Text("☕", fontSize = 64.sp)
            }

            // Tên + giá
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(sanPham.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                    Text(sanPham.description, fontSize = 13.sp, color = Color.Gray)
                }
                Text(
                    text = "${"%,d".format(uiState.currentUnitPrice)}đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MauNau
                )
            }

            // ── Chọn kích cỡ ─────────────────────────────────────────────────
            NhomLuaChon(tieuDe = "Kích cỡ") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sanPham.sizePrices.keys.forEach { size ->
                        NutLuaChon(
                            nhan     = size.label,
                            dangChon = uiState.selectedSize == size,
                            onClick  = { detailViewModel.selectSize(size) }
                        )
                    }
                }
            }

            // ── Chọn mức đường ───────────────────────────────────────────────
            NhomLuaChon(tieuDe = "Mức đường") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sanPham.sugarOptions.forEach { sugar ->
                        NutLuaChon(
                            nhan     = sugar.label,
                            dangChon = uiState.selectedSugar == sugar,
                            onClick  = { detailViewModel.selectSugar(sugar) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Chọn mức đá ─────────────────────────────────────────────────
            NhomLuaChon(tieuDe = "Mức đá") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sanPham.iceOptions.forEach { ice ->
                        NutLuaChon(
                            nhan     = ice.label,
                            dangChon = uiState.selectedIce == ice,
                            onClick  = { detailViewModel.selectIce(ice) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Chọn topping ─────────────────────────────────────────────────
            if (uiState.availableToppings.isNotEmpty()) {
                NhomLuaChon(tieuDe = "Topping") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.availableToppings.forEach { topping ->
                            HangTopping(
                                topping  = topping,
                                dangChon = uiState.selectedToppings.any { it.id == topping.id },
                                onClick  = { detailViewModel.toggleTopping(topping) }
                            )
                        }
                    }
                }
            }

            // ── Ghi chú ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { detailViewModel.onNoteChange(it) },
                label = { Text("Ghi chú (tuỳ chọn)") },
                placeholder = { Text("VD: Ít ngọt hơn, không đá...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )
        }

        // ── Thanh đặt hàng phía dưới ─────────────────────────────────────────
        Surface(shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút điều chỉnh số lượng: [ - ] 1 [ + ]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { detailViewModel.decreaseQty() },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) { Text("-", fontSize = 18.sp) }

                    Text(
                        "${uiState.quantity}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MauNauDam
                    )

                    FilledTonalButton(
                        onClick = { detailViewModel.increaseQty() },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) { Text("+", fontSize = 18.sp) }
                }

                // Nút thêm vào giỏ
                Button(
                    onClick = { detailViewModel.onAddedToCart() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MauNau)
                ) {
                    Text(
                        "Thêm vào giỏ • ${"%.0f".format(uiState.totalPrice / 1000.0)}k",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Composable tái sử dụng ───────────────────────────────────────────────────

// Khung nhóm lựa chọn có tiêu đề
@Composable
private fun NhomLuaChon(tieuDe: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tieuDe, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
        content()
    }
}

// Nút lựa chọn (size / đường / đá)
@Composable
private fun NutLuaChon(
    nhan: String,
    dangChon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (dangChon) MauNau else Color.White)
            .border(1.dp, if (dangChon) MauNau else Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = nhan,
            color      = if (dangChon) Color.White else MauNauDam,
            fontSize   = 13.sp,
            fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Hàng topping có checkbox
@Composable
private fun HangTopping(topping: Topping, dangChon: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (dangChon) Color(0xFFEFEBE9) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(topping.name, fontSize = 14.sp, color = MauNauDam)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("+${"%.0f".format(topping.price / 1000.0)}k", fontSize = 13.sp, color = Color.Gray)
            Checkbox(
                checked = dangChon,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(checkedColor = MauNau)
            )
        }
    }
}