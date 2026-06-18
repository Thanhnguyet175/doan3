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
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 🟢 Đã thêm icon Quay lại
import androidx.compose.material.icons.filled.Search
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
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.viewmodel.customer.MenuViewModel

private val MauNau     = Color(0xFF5D4037)
private val MauNauNhat = Color(0xFFFBF8F4)
private val MauNauDam  = Color(0xFF231F20)

@Composable
fun MenuScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onBack: () -> Unit, // 🟢 THÊM YÊU CẦU TRUYỀN HÀM QUAY LẠI VÀO ĐÂY
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
    ) {
        // ─── HEADER THỰC ĐƠN MÀU NÂU (GIỐNG 100% LỊCH SỬ ĐƠN HÀNG) ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3E2723)) // Đúng mã màu MauNauDam bên Lịch sử đơn hàng
                .padding(horizontal = 8.dp, vertical = 12.dp), // 🟢 Trả lề trái về 8.dp vì đã có nút
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🟢 THÊM NÚT QUAY LẠI VÀO ĐÂY
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
            Text(
                text = "Thực đơn",
                fontSize = 20.sp, // Bằng đúng size chữ bên Lịch sử
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text("Tìm món uống bạn thích...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MauNau) },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    TextButton(onClick = { viewModel.clearSearch() }) {
                        Text("✕", color = MauNau, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Text("Không tìm thấy món nào thích hợp 😅", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        } else {
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
            fontWeight = if (dangChon) FontWeight.Bold else FontWeight.SemiBold
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
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MauNauDam,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%,dđ", sanPham.basePrice),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MauNau
            )
        }
    }
}