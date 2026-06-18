package com.example.milkteaapp.view.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.viewmodel.admin.AdminCategoryViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.model.data.DrinkSize


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(
    onNavigateToEditCategory: (String?) -> Unit,
    viewModel: AdminCategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 🟢 ĐÃ FIX: Gọi chính xác qua uiState và hứng đúng tên tiếng Việt của ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = uiState.danhSachDanhMuc
    val productsByExpandedCategory = uiState.sanPhamTheoDanhMuc
    val expandedCategoryId = uiState.expandedCategoryId

    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý danh mục", fontWeight = FontWeight.Bold, color = Color(0xFF4E342E)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F0EB))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditCategory(null) },
                containerColor = Color(0xFF4E342E),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục")
            }
        },
        containerColor = Color(0xFFF9F5F0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có danh mục nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories, key = { cat: Category -> cat.id }) { category: Category ->
                        val isExpanded = expandedCategoryId == category.id
                        val products = productsByExpandedCategory[category.id] ?: emptyList()

                        CategoryCardItem(
                            category = category,
                            isExpanded = isExpanded,
                            products = products,
                            // 🟢 ĐÃ FIX: Gọi đúng tên hàm tiếng Việt trong ViewModel
                            onToggleExpand = { viewModel.toggleDanhMuc(category) },
                            onEditClick = { onNavigateToEditCategory(category.id) },
                            onDeleteClick = { showDeleteDialog = category }
                        )
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa danh mục
    showDeleteDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Xoá danh mục?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xoá danh mục \"${category.name}\"? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 🟢 ĐÃ FIX: Gọi đúng tên hàm xóa danh mục
                        viewModel.xoaDanhMuc(category.id)
                        showDeleteDialog = null
                        Toast.makeText(context, "Đã xoá danh mục", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Xoá")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Huỷ", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun CategoryCardItem(
    category: Category,
    isExpanded: Boolean,
    products: List<Product>,
    onToggleExpand: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(if (isExpanded) 90f else 0f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                    Text(
                        text = "Mã icon: ${category.iconName.ifBlank { "Không có" }}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFFFFB300))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = Color.Red)
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F0EB).copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    if (products.isEmpty()) {
                        Text(
                            text = "Không có sản phẩm nào thuộc danh mục này.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Danh sách món thuộc nhóm (${products.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8D6E63),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        products.forEach { sanPham ->
                            ProductMiniRow(sanPham = sanPham)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductMiniRow(sanPham: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = sanPham.imageUrl,
            contentDescription = sanPham.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sanPham.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4E342E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (sanPham.isAvailable) Color(0xFF4A7C59) else Color.Red)
                )
                Text(
                    if (sanPham.isAvailable) "Đang bán" else "Tạm hết",
                    fontSize = 11.sp,
                    color = if (sanPham.isAvailable) Color(0xFF4A7C59) else Color.Red,
                    fontWeight = FontWeight.SemiBold
                )
                Text("·", color = Color.Gray, fontSize = 11.sp)

                val sizes = sanPham.sizePrices.keys
                    .toList()
                    .sortedBy { (it as DrinkSize).ordinal }
                    .joinToString(separator = "/") { (it as DrinkSize).label }

                Text(text = if (sizes.isBlank()) "M" else sizes, fontSize = 11.sp, color = Color.Gray)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "%,dđ".format(sanPham.basePrice),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4E342E)
            )
        }
    }
}