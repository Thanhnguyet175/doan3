package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage // 🟢 Thêm import Coil để load ảnh thật từ Firebase
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.viewmodel.customer.MenuViewModel

private val MauNau     = Color(0xFF5D4037)
private val MauNauNhat = Color(0xFFFBF8F4)
private val MauNauDam  = Color(0xFF231F20)

@Composable
fun MenuScreen(
    onNavigateToProductDetail: (String) -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 10.dp)
        ) {
            Text("Thực đơn", fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, color = MauNauDam)
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text("Tìm món uống bạn thích...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MauNau) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    TextButton(onClick = { viewModel.clearSearch() }) {
                        Text("✕", color = MauNau, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = MauNau,
                unfocusedBorderColor = Color.Transparent
            )
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            item {
                TabDanhMuc(
                    ten        = "Tất cả",
                    dangChon   = uiState.selectedCategoryId == null,
                    onClick    = { viewModel.selectCategory(null) }
                )
            }
            // Chỉ hiện các danh mục được cấu hình cho phép hiển thị
            items(uiState.categories.filter { it.isVisible }) { danhMuc ->
                TabDanhMuc(
                    ten      = danhMuc.name,
                    dangChon = uiState.selectedCategoryId == danhMuc.id,
                    onClick  = { viewModel.selectCategory(danhMuc.id) }
                )
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MauNau)
            }
        } else if (uiState.filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy món nào thích hợp 😅", color = Color.Gray, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            }
        } else {
            // 🟢 Đã sửa lỗi ép kiểu: Sử dụng đúng hàm items dành riêng cho LazyVerticalGrid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredProducts) { sanPham ->
                    ProductGridCard(
                        sanPham  = sanPham,
                        onBấmVao = { onNavigateToProductDetail(sanPham.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabDanhMuc(ten: String, dangChon: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (dangChon) MauNau else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text  = ten,
            color = if (dangChon) Color.White else MauNauDam,
            fontSize = 13.sp,
            fontWeight = if (dangChon) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.SemiBold
        )
    }
}

@Composable
fun ProductGridCard(
    sanPham: Product,
    onBấmVao: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBấmVao() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEBE9)),
                contentAlignment = Alignment.Center
            ) {
                // 🟢 Hiển thị ảnh sản phẩm thật từ Firebase Storage
                if (!sanPham.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = sanPham.imageUrl,
                        contentDescription = sanPham.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "🍹", fontSize = 40.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = sanPham.name,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 15.sp,
                color = MauNauDam,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%,dđ", sanPham.basePrice),
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MauNau
            )
        }
    }
}