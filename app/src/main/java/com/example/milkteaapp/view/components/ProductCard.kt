package com.example.milkteaapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.milkteaapp.model.data.Product

private val MauNau    = Color(0xFF4E342E)
private val MauNauDam = Color(0xFF3E2723)
private val MauVang   = Color(0xFFF59E0B)

/**
 * Card sản phẩm dùng chung ở HomeScreen và MenuScreen.
 */
@Composable
fun ProductCard(
    sanPham: Product,
    onBấmVao: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBấmVao() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ảnh sản phẩm
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF4A7C59)),
                contentAlignment = Alignment.Center
            ) {
                Text("☕", fontSize = 28.sp)
            }

            // Thông tin sản phẩm
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Tên sản phẩm (🟢 ĐÃ CẮT BỎ ĐOẠN CHECK BESTSELLER GÂY LỖI)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(sanPham.name, fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 15.sp)
                }

                // Đánh giá + số lượng đã bán (Số cứng cho đẹp giao diện)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⭐", fontSize = 12.sp)
                    Text("4.9", fontSize = 12.sp, color = Color.Gray)
                    Text("·", color = Color.Gray)
                    Text("99+ đã bán", fontSize = 12.sp, color = Color.Gray)
                }

                // Giá
                Text(
                    "${"%,d".format(sanPham.basePrice)}đ",
                    fontWeight = FontWeight.Bold,
                    color = MauNau,
                    fontSize = 14.sp
                )
            }

            // Nút thêm vào giỏ
            Button(
                onClick = onBấmVao,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauNau)
            ) {
                Text("Thêm", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}