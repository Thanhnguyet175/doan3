package com.example.milkteaapp.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.viewmodel.admin.AdminUserViewModel

// ── Màu chủ đạo ──────────────────────────────────────────────────────────────
private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauXanh    = Color(0xFF4A7C59)
private val MauXam     = Color(0xFF9E9E9E)
private val MauDo      = Color(0xFFEF4444)
private val MauCam     = Color(0xFFF59E0B)
private val MauTim     = Color(0xFF7B1FA2)

// ── Màn hình quản lý người dùng ───────────────────────────────────────────────

@Composable
fun AdminUserScreen(
    onBack: () -> Unit,
    viewModel: AdminUserViewModel = hiltViewModel()
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
        floatingActionButton = {
            // ── FAB: Tạo tài khoản mới ────────────────────────────────────
            FloatingActionButton(
                onClick = { viewModel.moDialogTaoTaiKhoan() },
                containerColor = MauNau,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Tạo tài khoản", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MauNauNhat)
                .padding(padding)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MauNauDam)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Text(
                    "Quản lý người dùng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.taiDanhSachNguoiDung() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Làm mới", tint = Color.White)
                }
            }

            // ── Ô tìm kiếm ───────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.tuKhoaTim,
                onValueChange = { viewModel.timKiem(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Tìm theo tên hoặc email…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MauXam)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MauNau,
                    unfocusedBorderColor = Color(0xFFDDD6CF),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            // ── Bộ lọc vai trò ───────────────────────────────────────────────
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ChipRole(
                        nhan = "Tất cả",
                        duocChon = uiState.roleDangChon == null,
                        onClick = { viewModel.locTheoRole(null) }
                    )
                }
                items(UserRole.values()) { role ->
                    ChipRole(
                        nhan = tenRole(role),
                        duocChon = uiState.roleDangChon == role,
                        onClick = { viewModel.locTheoRole(role) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${uiState.danhSachHienThi.size} người dùng",
                fontSize = 13.sp,
                color = MauXam,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ── Danh sách / Loading / Rỗng ───────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MauNau)
                            Spacer(Modifier.height(12.dp))
                            Text("Đang tải danh sách…", color = MauXam)
                        }
                    }
                }

                uiState.danhSachHienThi.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy người dùng nào", color = MauXam, textAlign = TextAlign.Center)
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 88.dp   // padding để FAB không che item cuối
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.danhSachHienThi, key = { it.uid }) { nguoiDung ->
                            TheNguoiDung(
                                nguoiDung = nguoiDung,
                                onClick = { viewModel.xemNguoiDung(nguoiDung) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialog chi tiết người dùng ───────────────────────────────────────────
    uiState.nguoiDungDangXem?.let { nd ->
        DialogChiTietNguoiDung(
            nguoiDung = nd,
            onDismiss = { viewModel.dongChiTiet() },
            onDoiRole = { roleMoi -> viewModel.doiVaiTro(nd.uid, nd.role, roleMoi) },
            onKhoa    = { viewModel.khoaTaiKhoan(nd.uid) },
            onMoKhoa  = { viewModel.moKhoaTaiKhoan(nd.uid) }
        )
    }

    // ── Dialog tạo tài khoản mới ─────────────────────────────────────────────
    if (uiState.hienDialogTaoTaiKhoan) {
        DialogTaoTaiKhoan(
            dangTai  = uiState.dangTaoTaiKhoan,
            onDismiss = { viewModel.dongDialogTaoTaiKhoan() },
            onXacNhan = { hoTen, email, matKhau, xacNhan, role, sdt ->
                viewModel.taoTaiKhoan(hoTen, email, matKhau, xacNhan, role, sdt)
            }
        )
    }
}

// ── Thẻ người dùng trong danh sách ───────────────────────────────────────────

@Composable
private fun TheNguoiDung(nguoiDung: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (nguoiDung.isActive) Color.White else Color(0xFFFFF3F3)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(mauNenRole(nguoiDung.role)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    nguoiDung.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        nguoiDung.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MauNauDam,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    BadgeRole(role = nguoiDung.role)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    nguoiDung.email,
                    fontSize = 12.sp,
                    color = MauXam,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!nguoiDung.isActive) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "🔒 Tài khoản bị khoá",
                        fontSize = 11.sp,
                        color = MauDo,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Dialog tạo tài khoản mới (Staff / Admin) ─────────────────────────────────

@Composable
private fun DialogTaoTaiKhoan(
    dangTai: Boolean,
    onDismiss: () -> Unit,
    onXacNhan: (hoTen: String, email: String, matKhau: String, xacNhan: String, role: UserRole, sdt: String) -> Unit
) {
    var hoTen        by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var sdt          by remember { mutableStateOf("") }
    var matKhau      by remember { mutableStateOf("") }
    var xacNhanMk    by remember { mutableStateOf("") }
    var roleChon     by remember { mutableStateOf(UserRole.STAFF) }
    var hienMk       by remember { mutableStateOf(false) }
    var hienXacNhanMk by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!dangTai) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Tiêu đề ──────────────────────────────────────────────────
                Text(
                    "➕  Tạo tài khoản mới",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MauNauDam
                )
                Text(
                    "Chỉ tạo được tài khoản Nhân viên hoặc Admin.\nKhách hàng tự đăng ký qua app.",
                    fontSize = 12.sp,
                    color = MauXam,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // ── Chọn vai trò ─────────────────────────────────────────────
                Text("Vai trò *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MauNauDam)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(UserRole.STAFF, UserRole.ADMIN).forEach { role ->
                        val duocChon = roleChon == role
                        val mauNen   = if (duocChon) mauNenRole(role) else Color(0xFFF0EBE8)
                        val mauChu   = if (duocChon) Color.White else MauNauDam

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(mauNen)
                                .border(
                                    width = if (duocChon) 0.dp else 1.dp,
                                    color = Color(0xFFDDD6CF),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { roleChon = role }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (role == UserRole.STAFF) "👨‍💼" else "👑",
                                    fontSize = 22.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    tenRole(role),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = mauChu
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEDE7E1))
                Spacer(Modifier.height(16.dp))

                // ── Họ tên ───────────────────────────────────────────────────
                TruongNhap(
                    nhan      = "Họ và tên *",
                    giaTri    = hoTen,
                    onChange  = { hoTen = it },
                    giaY      = "Nguyễn Văn A",
                    loaiBanPhim = KeyboardType.Text
                )

                Spacer(Modifier.height(10.dp))

                // ── Email ─────────────────────────────────────────────────────
                TruongNhap(
                    nhan      = "Email *",
                    giaTri    = email,
                    onChange  = { email = it },
                    giaY      = "email@example.com",
                    loaiBanPhim = KeyboardType.Email
                )

                Spacer(Modifier.height(10.dp))

                // ── Số điện thoại ─────────────────────────────────────────────
                TruongNhap(
                    nhan      = "Số điện thoại",
                    giaTri    = sdt,
                    onChange  = { sdt = it },
                    giaY      = "0901234567 (tuỳ chọn)",
                    loaiBanPhim = KeyboardType.Phone
                )

                Spacer(Modifier.height(10.dp))

                // ── Mật khẩu ─────────────────────────────────────────────────
                OutlinedTextField(
                    value           = matKhau,
                    onValueChange   = { matKhau = it },
                    label           = { Text("Mật khẩu *") },
                    placeholder     = { Text("Tối thiểu 6 ký tự") },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    visualTransformation = if (hienMk) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon    = {
                        IconButton(onClick = { hienMk = !hienMk }) {
                            Icon(
                                if (hienMk) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MauXam
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors          = truongNhapColors()
                )

                Spacer(Modifier.height(10.dp))

                // ── Xác nhận mật khẩu ────────────────────────────────────────
                OutlinedTextField(
                    value           = xacNhanMk,
                    onValueChange   = { xacNhanMk = it },
                    label           = { Text("Xác nhận mật khẩu *") },
                    placeholder     = { Text("Nhập lại mật khẩu") },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(10.dp),
                    visualTransformation = if (hienXacNhanMk) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon    = {
                        IconButton(onClick = { hienXacNhanMk = !hienXacNhanMk }) {
                            Icon(
                                if (hienXacNhanMk) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MauXam
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors          = truongNhapColors(),
                    isError         = xacNhanMk.isNotEmpty() && xacNhanMk != matKhau,
                    supportingText  = {
                        if (xacNhanMk.isNotEmpty() && xacNhanMk != matKhau) {
                            Text("Mật khẩu không khớp", color = MauDo, fontSize = 11.sp)
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // ── Nút hành động ─────────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick   = { if (!dangTai) onDismiss() },
                        modifier  = Modifier.weight(1f),
                        shape     = RoundedCornerShape(10.dp),
                        enabled   = !dangTai
                    ) {
                        Text("Huỷ")
                    }

                    Button(
                        onClick = {
                            onXacNhan(hoTen, email, matKhau, xacNhanMk, roleChon, sdt)
                        },
                        modifier  = Modifier.weight(1f),
                        shape     = RoundedCornerShape(10.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = mauNenRole(roleChon)),
                        enabled   = !dangTai
                    ) {
                        if (dangTai) {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(16.dp),
                                color     = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tạo tài khoản", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ── Dialog chi tiết + hành động ──────────────────────────────────────────────

@Composable
private fun DialogChiTietNguoiDung(
    nguoiDung: User,
    onDismiss: () -> Unit,
    onDoiRole: (UserRole) -> Unit,
    onKhoa: () -> Unit,
    onMoKhoa: () -> Unit
) {
    var hienThiXacNhanKhoa by remember { mutableStateOf(false) }
    var roleDangChon by remember(nguoiDung.uid) { mutableStateOf(nguoiDung.role) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // ── Avatar + tên ─────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(mauNenRole(nguoiDung.role)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nguoiDung.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(nguoiDung.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                        Text(nguoiDung.email, fontSize = 12.sp, color = MauXam)
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEDE7E1))
                Spacer(Modifier.height(12.dp))

                DongThongTin("📱", "Điện thoại", nguoiDung.phoneNumber ?: "Chưa cập nhật")
                DongThongTin("📍", "Địa chỉ", nguoiDung.address ?: "Chưa cập nhật")
                DongThongTin(
                    "🔑", "Trạng thái",
                    if (nguoiDung.isActive) "Đang hoạt động" else "Bị khoá",
                    if (nguoiDung.isActive) MauXanh else MauDo
                )

                Spacer(Modifier.height(16.dp))

                // ── Đổi vai trò ───────────────────────────────────────────────
                if (nguoiDung.role != UserRole.ADMIN) {
                    Text("Vai trò:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MauNauDam)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(UserRole.CUSTOMER, UserRole.STAFF).forEach { role ->
                            FilterChip(
                                selected = roleDangChon == role,
                                onClick = {
                                    roleDangChon = role
                                    onDoiRole(role)
                                },
                                label = { Text(tenRole(role), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = mauNenRole(role),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Vai trò:", fontSize = 13.sp, color = MauNauDam)
                        Spacer(Modifier.width(8.dp))
                        BadgeRole(role = UserRole.ADMIN)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                HorizontalDivider(color = Color(0xFFEDE7E1))
                Spacer(Modifier.height(12.dp))

                // ── Khoá / Mở khoá ───────────────────────────────────────────
                if (nguoiDung.role != UserRole.ADMIN) {
                    if (nguoiDung.isActive) {
                        if (hienThiXacNhanKhoa) {
                            Text(
                                "Bạn chắc muốn khoá tài khoản này?",
                                fontSize = 13.sp,
                                color = MauDo,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { hienThiXacNhanKhoa = false },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Huỷ") }
                                Button(
                                    onClick = {
                                        onKhoa()
                                        hienThiXacNhanKhoa = false
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MauDo)
                                ) { Text("Khoá") }
                            }
                        } else {
                            Button(
                                onClick = { hienThiXacNhanKhoa = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MauDo),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔒  Khoá tài khoản", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onMoKhoa(); onDismiss() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MauXanh),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔓  Mở khoá tài khoản", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Đóng") }
            }
        }
    }
}

// ── Chip lọc vai trò ─────────────────────────────────────────────────────────

@Composable
private fun ChipRole(nhan: String, duocChon: Boolean, onClick: () -> Unit) {
    val mauNen = if (duocChon) MauNau else Color.White
    val mauChu = if (duocChon) Color.White else MauNauDam
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(mauNen)
            .border(1.dp, if (duocChon) MauNau else Color(0xFFDDD6CF), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(nhan, fontSize = 13.sp, color = mauChu, fontWeight = if (duocChon) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── Badge vai trò ─────────────────────────────────────────────────────────────

@Composable
private fun BadgeRole(role: UserRole) {
    val (nhan, mauNen) = when (role) {
        UserRole.ADMIN    -> "Admin" to MauTim
        UserRole.STAFF    -> "Nhân viên" to MauXanh
        UserRole.CUSTOMER -> "Khách hàng" to MauCam
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(mauNen.copy(alpha = 0.15f))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(nhan, fontSize = 10.sp, color = mauNen, fontWeight = FontWeight.Bold)
    }
}

// ── Dòng thông tin ────────────────────────────────────────────────────────────

@Composable
private fun DongThongTin(icon: String, nhan: String, giaTri: String, mauGiaTri: Color = MauNauDam) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text("$nhan: ", fontSize = 13.sp, color = MauXam, modifier = Modifier.width(80.dp))
        Text(giaTri, fontSize = 13.sp, color = mauGiaTri, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

// ── Trường nhập text tái sử dụng ─────────────────────────────────────────────

@Composable
private fun TruongNhap(
    nhan: String,
    giaTri: String,
    onChange: (String) -> Unit,
    giaY: String = "",
    loaiBanPhim: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value           = giaTri,
        onValueChange   = onChange,
        label           = { Text(nhan) },
        placeholder     = { Text(giaY) },
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = loaiBanPhim),
        colors          = truongNhapColors()
    )
}

@Composable
private fun truongNhapColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = MauNau,
    unfocusedBorderColor    = Color(0xFFDDD6CF),
    focusedContainerColor   = Color.White,
    unfocusedContainerColor = Color.White,
    focusedLabelColor       = MauNau
)

// ── Hàm phụ ──────────────────────────────────────────────────────────────────

private fun tenRole(role: UserRole): String = when (role) {
    UserRole.ADMIN    -> "Admin"
    UserRole.STAFF    -> "Nhân viên"
    UserRole.CUSTOMER -> "Khách hàng"
}

private fun mauNenRole(role: UserRole): Color = when (role) {
    UserRole.ADMIN    -> MauTim
    UserRole.STAFF    -> MauXanh
    UserRole.CUSTOMER -> MauCam
}