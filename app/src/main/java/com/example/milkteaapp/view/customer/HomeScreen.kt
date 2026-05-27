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
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.LocalBar
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
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
    categories: List<Category> = emptyList(),       // Nhận dữ liệu thật từ Admin truyền vào
    bestSellers: List<Product> = emptyList(),       // Nhận dữ liệu thật đã tính toán theo doanh thu
    soItemGio: Int = 0,
    onNavigateToMenu: (String?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Bảng màu chủ đạo giữ nguyên theo UI gốc
    val MauNauDam = Color(0xFF4E3629)
    val MauNenKem = Color(0xFFF9F5F0)
    val MauXanhLa = Color(0xFF4F7E63)
    val MauCam = Color(0xFFFF9800)

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
            // ─── HEADER (Xin chào & Giỏ hàng) ────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MauNauDam,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "NL Tea",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Xin chào, ${customerName.ifBlank { "Khách hàng" }} 👋",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Icon Giỏ hàng
                        IconButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(48.dp)
                        ) {
                            if (soItemGio > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MauCam) {
                                            Text(
                                                text = if (soItemGio > 99) "99+" else "$soItemGio",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Banner Thưởng thức
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF6D4C41))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = MauCam, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Dành riêng cho bạn", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Thưởng thức\ntừng khoảnh khắc",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }
            }

            // ─── THANH TÌM KIẾM ──────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                placeholder = { Text("Tìm món uống bạn thích...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MauNauDam) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = MauNauDam,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MauNauDam
                ),
                singleLine = true
            )

            // ─── DANH MỤC (Lấy trực tiếp từ Admin) ─────────────────────────────────
            Text(
                text = "Danh mục",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MauNauDam,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
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
                                .background(Color.White, shape = CircleShape)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val categoryName = cat.name.trim().lowercase()

                            val icon = when {
                                categoryName.contains("trà sữa") -> Icons.Rounded.LocalDrink
                                categoryName.contains("trà trái cây") || categoryName.contains("nước ép") -> Icons.Rounded.LocalBar
                                categoryName.contains("cà phê") || categoryName.contains("coffee") -> Icons.Rounded.Coffee
                                categoryName.contains("matcha") || categoryName.contains("latte") -> Icons.Rounded.LocalCafe
                                categoryName.contains("ăn vặt") || categoryName.contains("thức ăn") -> Icons.Rounded.Fastfood
                                else -> Icons.Rounded.Star
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = cat.name,
                                tint = MauNauDam,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.name,
                            fontSize = 13.sp,
                            color = MauNauDam,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ─── KHUYẾN MÃI ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = "Khuyến Mãi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                Text(
                    text = "Xem tất cả",
                    fontSize = 13.sp,
                    color = MauXanhLa,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateToMenu(null) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MauCam)
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(Icons.Rounded.LocalActivity, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "SUMMER DEAL", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Mua 1 Tặng 1\nTrà Trái Cây", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MauXanhLa)
                        .padding(16.dp)
                ) {
                    Column {
                        Icon(Icons.Rounded.LocalActivity, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "FLASH SALE", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Giảm 20%\nCà Phê", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // ─── BÁN CHẠY NHẤT (Lọc động từ Đơn hàng Hoàn thành) ─────────────────
            Text(
                text = "Bán Chạy Nhất",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MauNauDam,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 12.dp)
            )

            bestSellers.forEach { product ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable { onNavigateToProductDetail(product.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hình ảnh sản phẩm
                        Box(
                            modifier = Modifier
                                .size(80.dp)
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
                                Icon(Icons.Rounded.LocalDrink, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Thông tin sản phẩm
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "4.9", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "• Bán chạy", fontSize = 11.sp, color = MauXanhLa, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MauNauDam,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%,d đ", product.basePrice),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MauCam
                            )
                        }

                        // Nút Chọn mua nhanh
                        IconButton(
                            onClick = { onNavigateToProductDetail(product.id) },
                            modifier = Modifier
                                .background(MauNauDam, RoundedCornerShape(12.dp))
                                .size(40.dp)
                        ) {
                            Icon(Icons.Rounded.AddShoppingCart, contentDescription = "Thêm ngay", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}