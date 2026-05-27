package com.example.milkteaapp.view.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.milkteaapp.model.data.DrinkSize
import com.example.milkteaapp.model.data.IcePackOption
import com.example.milkteaapp.model.data.IceLevel
import com.example.milkteaapp.model.data.SugarLevel
import com.example.milkteaapp.model.data.Topping
import com.example.milkteaapp.viewmodel.customer.CartViewModel
import com.example.milkteaapp.viewmodel.customer.DetailViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauCam     = Color(0xFFFF9800)

@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    cartViewModel: CartViewModel,
    detailViewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.addedToCart) {
        if (uiState.addedToCart) {
            val item = detailViewModel.buildCartItem()
            if (item != null) cartViewModel.addItem(item)
            detailViewModel.onAddedToCartHandled()
            onBack()
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
        // TOOLBAR
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
            Text("Thông tin sản phẩm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ẢNH
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = sanPham.imageUrl,
                    contentDescription = sanPham.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Rounded.LocalDrink)
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sanPham.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MauNauDam)
                        Spacer(Modifier.height(4.dp))
                        Text(sanPham.description, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                    }
                    Text(
                        text = "${"%,d".format(uiState.currentUnitPrice)}đ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MauCam,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                // 1. KÍCH CỠ LY
                NhomLuaChon(tieuDe = "Kích cỡ ly") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        sanPham.sizePrices.keys.forEach { size ->
                            NutLuaChon(
                                nhan = size.label,
                                dangChon = uiState.selectedSize == size,
                                onClick = { detailViewModel.selectSize(size) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. MỨC ĐƯỜNG
                NhomLuaChon(tieuDe = "Mức đường") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SugarLevel.values().forEach { sugar ->
                            NutLuaChon(
                                nhan = sugar.label,
                                dangChon = uiState.selectedSugar == sugar,
                                onClick = { detailViewModel.selectSugar(sugar) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. CHẾ ĐỘ ĐÁ (ĐÁ RIÊNG / ĐÁ CHUNG)
                NhomLuaChon(tieuDe = "Hình thức đóng gói đá") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IcePackOption.values().forEach { option ->
                            NutLuaChon(
                                nhan = option.label,
                                dangChon = uiState.selectedIcePack == option,
                                onClick = { detailViewModel.selectIcePack(option) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 4. MỨC ĐỘ ĐÁ CỤ THỂ (ẨN HIỆN THEO ĐÁ CHUNG)
                AnimatedVisibility(
                    visible = uiState.selectedIcePack == IcePackOption.DA_CHUNG,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NhomLuaChon(tieuDe = "Mức độ đá tùy chọn") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IceLevel.values().forEach { level ->
                                NutLuaChon(
                                    nhan = level.label,
                                    dangChon = uiState.selectedIceLevel == level,
                                    onClick = { detailViewModel.selectIceLevel(level) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // TOPPING
                if (uiState.availableToppings.isNotEmpty()) {
                    NhomLuaChon(tieuDe = "Topping chọn thêm") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.availableToppings.forEach { topping ->
                                HangTopping(
                                    topping = topping,
                                    dangChon = uiState.selectedToppings.any { it.id == topping.id },
                                    onClick = { detailViewModel.toggleTopping(topping) }
                                )
                            }
                        }
                    }
                }

                // GHI CHÚ
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { detailViewModel.onNoteChange(it) },
                    label = { Text("Ghi chú tùy chọn cho món") },
                    placeholder = { Text("Ví dụ: Ít ngọt, lấy thêm thạch...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MauNau,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // BOTTOM BAR
        Surface(shadowElevation = 12.dp, color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MauNauNhat, RoundedCornerShape(24.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = { detailViewModel.decreaseQty() }) {
                        Text("-", fontSize = 22.sp, color = MauNauDam, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${uiState.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(onClick = { detailViewModel.increaseQty() }) {
                        Text("+", fontSize = 18.sp, color = MauNauDam, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { detailViewModel.onAddedToCart() },
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .padding(start = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MauNau)
                ) {
                    Text(
                        text = "Thêm • ${"%,d".format(uiState.totalPrice)}đ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NhomLuaChon(tieuDe: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tieuDe, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
        content()
    }
}

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
            .border(1.dp, if (dangChon) MauNau else Color(0xFFE0D7D3), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = nhan,
            color      = if (dangChon) Color.White else MauNauDam,
            fontSize   = 13.sp,
            fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun HangTopping(topping: Topping, dangChon: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (dangChon) Color(0xFFF0EDE9) else Color.White)
            .border(0.5.dp, if (dangChon) MauNau else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(topping.name, fontSize = 14.sp, color = MauNauDam, fontWeight = FontWeight.Medium)
            Text("+${"%,d".format(topping.price)}đ", fontSize = 12.sp, color = MauCam)
        }
        Checkbox(
            checked = dangChon,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(checkedColor = MauNau)
        )
    }
}