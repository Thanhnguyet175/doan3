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
import coil.request.ImageRequest
import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.viewmodel.admin.AdminCategoryViewModel

private val MauNauDam  = Color(0xFF3E2723)
private val MauNau     = Color(0xFF4E342E)
private val MauNauMid  = Color(0xFF795548)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauKem  = Color(0xFFD7CCC8)
private val MauXanh    = Color(0xFF4A7C59)
private val MauDo      = Color(0xFFEF4444)
private val MauTextSub = Color(0xFF7F7571)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(
    onBack: () -> Unit,
    viewModel: AdminCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context  = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<Category?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.thongBaoThanhCong) {
        uiState.thongBaoThanhCong?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.xoaThongBao()
        }
    }
    LaunchedEffect(uiState.thongBaoLoi) {
        uiState.thongBaoLoi?.let {
            snackbarHostState.showSnackbar("Lỗi: $it")
            viewModel.xoaThongBao()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick      = { textInput = ""; viewModel.moFormThem() },
                containerColor = MauNau,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Thêm danh mục", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MauNauNhat)
                .padding(padding)
        ) {
            when {
                // ── Đang tải danh sách danh mục lần đầu ─────────────────────
                uiState.isLoadingCategories && uiState.danhSachDanhMuc.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MauNau, strokeWidth = 2.dp)
                    }
                }

                uiState.danhSachDanhMuc.isEmpty() -> {
                    // ── Chưa có danh mục nào ──────────────────────────────────
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📂", fontSize = 44.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Chưa có danh mục nào", color = MauTextSub, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Nhấn nút + để tạo danh mục đầu tiên", color = MauNauKem, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                else -> {
                    // ── Danh sách accordion ───────────────────────────────────
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 14.dp, end = 14.dp, top = 12.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.danhSachDanhMuc,
                            key   = { it.id }
                        ) { danhMuc ->
                            val isExpanded  = uiState.expandedCategoryId == danhMuc.id
                            val isLoading   = uiState.isLoadingProducts[danhMuc.id] == true
                            val sanPhamList = uiState.sanPhamTheoDanhMuc[danhMuc.id] ?: emptyList()

                            DanhMucAccordionCard(
                                danhMuc     = danhMuc,
                                isExpanded  = isExpanded,
                                isLoading   = isLoading,
                                sanPhamList = sanPhamList,
                                onToggle    = { viewModel.toggleDanhMuc(danhMuc) },
                                onEdit      = { textInput = danhMuc.name; viewModel.moFormSua(danhMuc) },
                                onDelete    = { showDeleteConfirm = danhMuc }
                            )
                        }
                    }

                    // ── Loading overlay nhẹ khi đang refresh danh mục ────────
                    if (uiState.isLoadingCategories) {
                        LinearProgressIndicator(
                            color = MauNau,
                            trackColor = MauNauKem.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    // ── Dialog thêm / sửa danh mục ───────────────────────────────────────────
    if (uiState.dangMoForm) {
        AlertDialog(
            onDismissRequest = { viewModel.dongForm() },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(16.dp),
            title = {
                Text(
                    if (uiState.danhMucDangSua == null) "Thêm danh mục mới" else "Cập nhật danh mục",
                    fontWeight = FontWeight.ExtraBold, color = MauNauDam, fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tên danh mục", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = MauNauMid, letterSpacing = 0.5.sp)
                    OutlinedTextField(
                        value         = textInput,
                        onValueChange = { textInput = it },
                        placeholder   = { Text("Ví dụ: Trà Sữa, Cà Phê...", color = MauNauKem) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = MauNau,
                            unfocusedBorderColor    = MauNauKem,
                            focusedContainerColor   = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.luuDanhMuc(textInput) },
                    colors   = ButtonDefaults.buttonColors(containerColor = MauNau),
                    shape    = RoundedCornerShape(10.dp),
                    enabled  = textInput.isNotBlank()
                ) { Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dongForm() }) {
                    Text("Hủy", color = MauTextSub)
                }
            }
        )
    }

    // ── Dialog xác nhận xóa ──────────────────────────────────────────────────
    showDeleteConfirm?.let { dm ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor   = Color.White,
            shape            = RoundedCornerShape(16.dp),
            title = { Text("Xóa danh mục?", fontWeight = FontWeight.ExtraBold, color = MauNauDam) },
            text  = {
                Text(
                    "Danh mục \"${dm.name}\" sẽ bị xóa vĩnh viễn. Sản phẩm trong danh mục sẽ không bị xóa.",
                    color = MauTextSub, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.xoaDanhMuc(dm.id); showDeleteConfirm = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MauDo),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Hủy", color = MauTextSub)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Accordion Card: Header danh mục + AnimatedVisibility cho sản phẩm bên trong
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DanhMucAccordionCard(
    danhMuc: Category,
    isExpanded: Boolean,
    isLoading: Boolean,
    sanPhamList: List<Product>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column {
            // ── Header hàng danh mục ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon / ảnh danh mục
                DanhMucIcon(iconUrl = danhMuc.iconUrl, name = danhMuc.name)

                // Tên + số sản phẩm (hiện khi đã tải)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        danhMuc.name,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MauNauDam,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (isExpanded && !isLoading) {
                        Text(
                            "${sanPhamList.size} sản phẩm",
                            fontSize = 12.sp,
                            color    = MauTextSub,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Nút Edit
                IconButton(
                    onClick  = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MauNauNhat)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa",
                        tint = MauNauMid, modifier = Modifier.size(16.dp))
                }

                // Nút Delete
                IconButton(
                    onClick  = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa",
                        tint = MauDo, modifier = Modifier.size(16.dp))
                }

                // Mũi tên xoay
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint     = MauNauKem,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isExpanded) 90f else 0f)
                )
            }

            // ── Nội dung mở rộng: danh sách sản phẩm ────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(color = MauNauKem.copy(alpha = 0.4f))

                    when {
                        isLoading -> {
                            // Loading sản phẩm
                            Box(
                                modifier            = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment    = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(18.dp),
                                        color       = MauNau,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Đang tải sản phẩm...", color = MauTextSub, fontSize = 13.sp)
                                }
                            }
                        }

                        sanPhamList.isEmpty() -> {
                            // Không có sản phẩm
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Chưa có sản phẩm trong danh mục này",
                                    color    = MauTextSub,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        else -> {
                            // Danh sách sản phẩm thực
                            Column(
                                modifier = Modifier.padding(
                                    start = 14.dp, end = 14.dp,
                                    top   = 6.dp,  bottom = 10.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sanPhamList.forEach { sanPham ->
                                    SanPhamRow(sanPham = sanPham)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Icon danh mục: load từ URL hoặc fallback chữ cái ─────────────────────────
@Composable
private fun DanhMucIcon(iconUrl: String?, name: String) {
    val context = LocalContext.current
    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MauNauNhat),
        contentAlignment = Alignment.Center
    ) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
            )
        } else {
            // Fallback: chữ cái đầu của tên danh mục
            Text(
                text       = name.take(1).uppercase(),
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MauNauMid
            )
        }
    }
}

// ── Row một sản phẩm bên trong accordion ─────────────────────────────────────
@Composable
private fun SanPhamRow(sanPham: Product) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MauNauNhat)
            .padding(10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Ảnh sản phẩm từ Firestore / Cloudinary
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MauNauKem.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (!sanPham.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(sanPham.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = sanPham.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp))
                )
            } else {
                // Placeholder chữ cái
                Text(
                    sanPham.name.take(1).uppercase(),
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MauNauMid
                )
            }
        }

        // Thông tin sản phẩm
        Column(modifier = Modifier.weight(1f)) {
            Text(
                sanPham.name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = MauNauDam,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier              = Modifier.padding(top = 3.dp)
            ) {
                // Chấm trạng thái
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (sanPham.isAvailable) MauXanh else MauDo)
                )
                Text(
                    if (sanPham.isAvailable) "Đang bán" else "Tạm hết",
                    fontSize   = 11.sp,
                    color      = if (sanPham.isAvailable) MauXanh else MauDo,
                    fontWeight = FontWeight.SemiBold
                )
                // Divider nhỏ
                Text("·", color = MauNauKem, fontSize = 11.sp)
                // Size
                val sizes = sanPham.sizePrices.keys
                    .sortedBy { it.ordinal }
                    .joinToString("/") { it.label }
                Text(sizes.ifEmpty { "M" }, fontSize = 11.sp, color = MauTextSub)
            }
        }

        // Giá
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "%,dđ".format(sanPham.basePrice),
                fontSize   = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MauNau
            )
            if (sanPham.isBestSeller) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("HOT 🔥", fontSize = 9.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}