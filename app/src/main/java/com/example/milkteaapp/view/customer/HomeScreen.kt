package com.example.milkteaapp.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import coil.compose.AsyncImage
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    customerName: String = "Khách hàng",
    categories: List<Category> = emptyList(),
    bestSellers: List<Product> = emptyList(),
    soItemGio: Int = 0,
    onNavigateToMenu: (String?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val MauNauDam = Color(0xFF4E3629)
    val MauNenKem = Color(0xFFF9F5F0)
    val MauXanhLa = Color(0xFF4F7E63)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNenKem)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            // ─── HEADER (GIỎ HÀNG Ở GÓC TRÁI) ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MauNauDam)
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // 🟢 Đẩy 2 cụm ra 2 đầu lề trái/phải
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🟢 GÓC TRÁI: Tên quán và lời chào khách hàng
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "NL Tea",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Xin chào, ${customerName.ifBlank { "Khách hàng" }} 👋",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }

                        // 🟢 GÓC PHẢI: Nút Giỏ hàng độc lập kèm Badge đỏ thông báo số lượng
                        IconButton(onClick = onNavigateToCart) {
                            if (soItemGio > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color.Red) {
                                            Text(text = if (soItemGio > 99) "99+" else "$soItemGio", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = Color.White)
                                }
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Giỏ hàng", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Khung Banner Thưởng thức giữ nguyên
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF5A3E32))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(text = "Thưởng thức", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            Text(
                                text = "từng khoảnh khắc",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ─── SEARCH BAR ──────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm món uống bạn thích...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = MauNauDam,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // ─── DANH MỤC ───────────────────────────────────────────────────
            Text(
                text = "Danh mục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MauNauDam,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(categories.filter { it.isVisible }) { cat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToMenu(cat.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFEFEBE9), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val emoji = when (cat.name.trim()) {
                                "Trà Sữa"      -> "🧋"
                                "Trà Trái Cây" -> "🍹"
                                "Cà Phê"       -> "☕"
                                "Ăn Vặt", "Thức ăn nhanh" -> "🍿"
                                else           -> "✨"
                            }
                            Text(text = emoji, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            color = MauNauDam,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ─── KHUYẾN MÃI ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Khuyến Mãi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                Text(text = "Xem tất cả", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.clickable { onNavigateToMenu(null) })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF3E2723))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "SUMMER DEAL", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text(text = "Mua 1 Tặng 1\nTrà Trái Cây", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MauXanhLa) // 🟢 ĐÃ FIX: Viết liền không lỗi chính tả
                        .padding(16.dp)
                ) {
                    Column {
                        Text(text = "SUMMER DEAL", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text(text = "Giảm 20%\nCà Phê", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // ─── BÁN CHẠY NHẤT ───────────────────────────────────────────────
            Text(
                text = "Bán Chạy Nhất",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MauNauDam,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 10.dp)
            )

            if (bestSellers.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Đang tính toán món bán chạy nhất... ✨", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                bestSellers.forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onNavigateToProductDetail(product.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F0EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!product.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(text = "🍹", fontSize = 32.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⭐ 4.9", fontSize = 12.sp, color = Color(0xFFFFB300))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Bán chạy nhất", fontSize = 11.sp, color = MauXanhLa, fontWeight = FontWeight.Medium)
                                }
                                Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MauNauDam)
                                Text(text = String.format("%,dđ", product.basePrice), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8D6E63))
                            }
                            Button(
                                onClick = { onNavigateToProductDetail(product.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = MauNauDam),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Thêm ngay", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}