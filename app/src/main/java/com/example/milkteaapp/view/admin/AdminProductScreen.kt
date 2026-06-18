package com.example.milkteaapp.view.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.milkteaapp.model.data.Product
import com.example.milkteaapp.viewmodel.admin.AdminProductViewModel

import com.example.milkteaapp.model.data.Category
import com.example.milkteaapp.model.data.DrinkSize // 🟢 ĐÃ THÊM: Import thư viện DrinkSize

// ── Bảng màu (đồng bộ thiết kế) ──────────────────────────────────────────────
private val Cream      = Color(0xFFF5F0EB)   // nền tổng thể
private val Brown      = Color(0xFF5C3D2E)   // màu chính (nâu ấm)
private val BrownDark  = Color(0xFF3E2723)   // header / text đậm
private val BrownLight = Color(0xFFD7CCC8)   // viền / divider
private val Green      = Color(0xFF4A7C59)   // badge "INSTOCK" / toggle on
private val CardWhite  = Color(0xFFFFFFFF)   // thân card

@Composable
fun AdminProductScreen(
    onBack: () -> Unit,
    viewModel: AdminProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.thongBaoThanhCong) {
        uiState.thongBaoThanhCong?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.xoaThongBaoThanhCong()
        }
    }
    LaunchedEffect(uiState.thongBaoLoi) {
        uiState.thongBaoLoi?.let {
            snackbarHostState.showSnackbar("Lỗi: $it")
            viewModel.xoaThongBaoLoi()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Cream
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(
                    "Tea Collection",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrownDark,
                    fontStyle = FontStyle.Italic
                )
                Text(
                    "Curate and manage your artisanal tea blends.\nAdjust availability and pricing for the season.",
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63),
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Brown),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrownLight),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Filter", fontSize = 13.sp)
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { viewModel.batDauThemMoi() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brown),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("+ New Product", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Brown)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.danhSachHienThi, key = { it.id }) { sanPham ->
                        SanPhamCard(
                            sanPham          = sanPham,
                            onSua            = { viewModel.batDauSua(sanPham) },
                            onDoiTrangThai   = {
                                viewModel.doiTrangThaiBan(sanPham.id, !sanPham.isAvailable)
                            },
                            onXoa            = { viewModel.xoaSanPham(sanPham) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (uiState.dangMoForm) {
            DialogFormSanPham(
                sanPhamGoc       = uiState.sanPhamDangSua,
                selectedImageUri = uiState.selectedImageUri,
                isUploading      = uiState.isUploadingImage,
                danhSachDanhMucObj = uiState.danhSachDanhMuc ?: emptyList(),
                onChonAnh        = { uri -> viewModel.chonAnh(uri) },
                onLuu            = { viewModel.luuSanPham(it) },
                onHuy            = { viewModel.dongForm() }
            )
        }
    }
}

@Composable
private fun SanPhamCard(
    sanPham        : Product,
    onSua          : () -> Unit,
    onDoiTrangThai : () -> Unit,
    onXoa          : () -> Unit
) {
    var hienDialogXoa by remember { mutableStateOf(false) }

    val imgBg = when {
        sanPham.categoryId.lowercase().contains("matcha") -> Color(0xFF4A7C59)
        sanPham.categoryId.lowercase().contains("oolong") -> Color(0xFF8D6E63)
        sanPham.categoryId.lowercase().contains("sua")    -> Color(0xFFB08068)
        else -> Color(0xFF6D8B74)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = imgBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!sanPham.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(sanPham.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = sanPham.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🧋", fontSize = 56.sp)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "ID: ${sanPham.categoryId.take(8)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        sanPham.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrownDark,
                        modifier = Modifier.weight(1f)
                    )
                    // Hiển thị basePrice (mặc định lấy theo Size M) cho thẻ sản phẩm
                    Text(
                        "${"${sanPham.basePrice / 1000}".replace(",", ".")}k VND",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Brown
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    sanPham.description.ifBlank { "Chưa có mô tả." },
                    fontSize = 12.sp,
                    color = Color(0xFF8D6E63),
                    lineHeight = 16.sp,
                    maxLines = 2
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = sanPham.isAvailable,
                        onCheckedChange = { onDoiTrangThai() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor      = Color.White,
                            checkedTrackColor      = Green,
                            uncheckedThumbColor    = Color.White,
                            uncheckedTrackColor    = Color.Gray.copy(0.4f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (sanPham.isAvailable) "INSTOCK" else "HIDDEN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sanPham.isAvailable) Green else Color.Gray
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = onSua,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BrownLight.copy(0.4f))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa",
                            tint = Brown, modifier = Modifier.size(16.dp))
                    }

                    Spacer(Modifier.width(8.dp))

                    IconButton(
                        onClick = { hienDialogXoa = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Xoá",
                            tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    if (hienDialogXoa) {
        AlertDialog(
            onDismissRequest = { hienDialogXoa = false },
            title   = { Text("Xác nhận xoá", fontWeight = FontWeight.Bold) },
            text    = { Text("Bạn có chắc muốn xoá \"${sanPham.name}\" không?") },
            confirmButton = {
                Button(
                    onClick = { hienDialogXoa = false; onXoa() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) { Text("Xoá", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { hienDialogXoa = false }) { Text("Huỷ", color = Brown) }
            },
            containerColor = CardWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogFormSanPham(
    sanPhamGoc        : Product?,
    selectedImageUri  : Uri?,
    isUploading       : Boolean,
    danhSachDanhMucObj: List<Category>,
    onChonAnh         : (Uri?) -> Unit,
    onLuu             : (Product) -> Unit,
    onHuy             : () -> Unit
) {
    var ten              by remember { mutableStateOf(sanPhamGoc?.name ?: "") }
    var moTa             by remember { mutableStateOf(sanPhamGoc?.description ?: "") }

    // 🟢 ĐÃ FIX: Chẻ đôi thuộc tính Giá và tự động móc dữ liệu cũ lên form (nếu có)

// 🟢 Sửa giaM: Ép kiểu Map tường minh
    var giaM by remember {
        val map = sanPhamGoc?.sizePrices ?: emptyMap<DrinkSize, Long>()
        mutableStateOf(map[DrinkSize.MEDIUM]?.toString() ?: "")
    }

    var giaL by remember {
        val map = sanPhamGoc?.sizePrices ?: emptyMap<DrinkSize, Long>()
        mutableStateOf(map[DrinkSize.LARGE]?.toString() ?: "")
    }

    var danhMucId        by remember { mutableStateOf(sanPhamGoc?.categoryId ?: "") }
    var expandedDropdown by remember { mutableStateOf(false) }
    val isThemMoi         = sanPhamGoc == null || sanPhamGoc.id.isBlank()

    val tenDanhMucHienTai = danhSachDanhMucObj.find { it.id == danhMucId }?.name ?: "Chọn danh mục"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onChonAnh(it) } }

    Dialog(onDismissRequest = onHuy) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    if (isThemMoi) "Thêm sản phẩm mới" else "Sửa sản phẩm",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BrownDark
                )

                // Chọn ảnh từ máy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Cream)
                        .border(1.dp, BrownLight, RoundedCornerShape(12.dp))
                        .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(selectedImageUri).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        )
                    } else if (!sanPhamGoc?.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(sanPhamGoc?.imageUrl).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Brown.copy(0.5f), modifier = Modifier.size(40.dp))
                            Text("Nhấn để chọn ảnh từ máy", fontSize = 13.sp, color = Brown.copy(0.6f))
                        }
                    }
                }

                OutlinedTextField(
                    value = ten, onValueChange = { ten = it }, label = { Text("Tên sản phẩm *") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brown, focusedLabelColor = Brown)
                )

                OutlinedTextField(
                    value = moTa, onValueChange = { moTa = it }, label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brown, focusedLabelColor = Brown)
                )

                // 🟢 ĐÃ FIX: Chẻ đôi ô giá thành 2 cột ngang hàng nhau
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = giaM, onValueChange = { giaM = it }, label = { Text("Giá Size M (VNĐ) *") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brown, focusedLabelColor = Brown)
                    )
                    OutlinedTextField(
                        value = giaL, onValueChange = { giaL = it }, label = { Text("Giá Size L (VNĐ) *") },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brown, focusedLabelColor = Brown)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tenDanhMucHienTai,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục sản phẩm *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brown, focusedLabelColor = Brown)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(CardWhite)
                    ) {
                        if (danhSachDanhMucObj.isEmpty()) {
                            DropdownMenuItem(text = { Text("Trống", color = Color.Gray) }, onClick = {})
                        } else {
                            danhSachDanhMucObj.forEach { dm ->
                                DropdownMenuItem(
                                    text = { Text(dm.name, color = BrownDark) },
                                    onClick = {
                                        danhMucId = dm.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onHuy, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Huỷ") }
                    Button(
                        onClick = {
                            // 🟢 ĐÃ FIX: Nhồi 2 giá mới vào Map để lưu chung một cục
                            val priceM = giaM.toLongOrNull() ?: 0L
                            val priceL = giaL.toLongOrNull() ?: 0L

                            val sp = (sanPhamGoc ?: Product()).copy(
                                name        = ten.trim(),
                                description = moTa.trim(),
                                basePrice   = priceM, // Giữ basePrice bằng Size M cho các chỗ hiển thị mặc định
                                sizePrices = mapOf(
                                    "MEDIUM" to (giaM.toLongOrNull() ?: 0L),
                                    "LARGE" to (giaL.toLongOrNull() ?: 0L)
                                ),
                                categoryId  = danhMucId.trim()
                            )
                            onLuu(sp)
                        },
                        // Điều kiện mở nút LƯU
                        enabled = ten.isNotBlank() && danhMucId.isNotBlank() && !isUploading,
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brown)
                    ) {
                        if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        else Text("Lưu", color = Color.White)
                    }
                }
            }
        }
    }
}