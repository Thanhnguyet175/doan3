package com.example.milkteaapp.view.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.model.data.Order
import com.example.milkteaapp.model.data.OrderStatus
import com.example.milkteaapp.viewmodel.admin.AdminOrderViewModel
import com.example.milkteaapp.viewmodel.admin.TabDonHang
import kotlinx.coroutines.launch

private val MauNau     = Color(0xFF4E342E)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauDam  = Color(0xFF3E2723)
private val MauNauKem  = Color(0xFFD7CCC8)
private val MauNauMid  = Color(0xFF795548)
private val MauXanh    = Color(0xFF4A7C59)
private val MauCam     = Color(0xFFF97316)
private val MauDo      = Color(0xFFEF4444)
private val MauTextSub = Color(0xFF7F7571)

// ── Enum cho 2 mục trong drawer ──────────────────────────────────────────────
private enum class StaffSection { ORDERS, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffOrderManagementScreen(
    onBack: () -> Unit,
    viewModel: AdminOrderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentSection by remember { mutableStateOf(StaffSection.ORDERS) }

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

    // ── Modal Navigation Drawer ──────────────────────────────────────────────
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MauNauNhat,
                modifier = Modifier.width(265.dp)
            ) {
                // ── Drawer Header ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MauNauDam)
                        .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MauNauMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("NV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Nhân viên", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("NL Tea – Chi nhánh chính", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "CHỨC NĂNG",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = MauNauMid,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // ── Mục: Hồ sơ cá nhân ───────────────────────────────────────
                NavigationDrawerItem(
                    label = { Text("Hồ sơ cá nhân", fontWeight = FontWeight.Medium) },
                    selected = currentSection == StaffSection.PROFILE,
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    onClick = {
                        currentSection = StaffSection.PROFILE
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MauNauKem.copy(alpha = 0.5f),
                        selectedTextColor = MauNau,
                        selectedIconColor = MauNau,
                        unselectedTextColor = MauNauDam,
                        unselectedIconColor = MauNauMid
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Mục: Quản lý đơn hàng ────────────────────────────────────
                NavigationDrawerItem(
                    label = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Medium) },
                    selected = currentSection == StaffSection.ORDERS,
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                    onClick = {
                        currentSection = StaffSection.ORDERS
                        scope.launch { drawerState.close() }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MauNauKem.copy(alpha = 0.5f),
                        selectedTextColor = MauNau,
                        selectedIconColor = MauNau,
                        unselectedTextColor = MauNauDam,
                        unselectedIconColor = MauNauMid
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MauNauKem.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // ── Đăng xuất ─────────────────────────────────────────────────
                NavigationDrawerItem(
                    label = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MauDo) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onBack()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color(0xFFFFEBEE),
                        unselectedTextColor = MauDo,
                        unselectedIconColor = MauDo
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // ── TopBar chung ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MauNauDam)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ☰ Hamburger
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Mở menu", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentSection == StaffSection.ORDERS) "Quản lý đơn hàng"
                                else "Hồ sơ cá nhân",
                                fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                            Text(
                                "Nhân viên – NL Tea",
                                fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        if (currentSection == StaffSection.ORDERS) {
                            IconButton(onClick = { viewModel.taiTatCaDonHang() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = Color.White)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            when (currentSection) {
                StaffSection.ORDERS -> NoiDungDonHang(
                    padding = padding,
                    uiState = uiState,
                    viewModel = viewModel
                )
                StaffSection.PROFILE -> NoiDungHoSo(padding = padding, onLogout = {
                    onBack()
                })
            }
        }
    }

    // ── Dialog chi tiết ───────────────────────────────────────────────────────
    if (uiState.donDangXem != null) {
        DialogStaffChiTietDon(
            don    = uiState.donDangXem!!,
            onDong = { viewModel.dongChiTiet() },
            onCapNhatTrangThai = { trangThaiMoi ->
                viewModel.capNhatTrangThai(uiState.donDangXem!!.id, trangThaiMoi)
                viewModel.dongChiTiet()
            }
        )
    }
}

// ── Nội dung tab Đơn hàng ────────────────────────────────────────────────────
@Composable
private fun NoiDungDonHang(
    padding: PaddingValues,
    uiState: com.example.milkteaapp.viewmodel.admin.AdminOrderUiState,
    viewModel: AdminOrderViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
            .padding(padding)
    ) {
        // ── Tab row ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabDonHang.entries.forEach { tab ->
                NutTab(
                    nhan     = tab.nhanHien,
                    dangChon = uiState.tabDangChon == tab,
                    onClick  = { viewModel.chonTab(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Tổng số đơn ──────────────────────────────────────────────────────
        Text(
            "Tổng số: ${uiState.danhSachHienThi.size} đơn hàng",
            fontSize = 12.sp, color = MauTextSub,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ── Danh sách ────────────────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MauNau)
            }
        } else if (uiState.danhSachHienThi.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Không có đơn hàng nào", color = MauTextSub, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.danhSachHienThi, key = { it.id }) { don ->
                    TheDonHangStaffMoi(
                        don             = don,
                        dangCapNhat     = uiState.idDonDangCapNhat == don.id,
                        onBamVao        = { viewModel.xemChiTiet(don) },
                        onCapNhatTrangThai = { trangThaiMoi ->
                            viewModel.capNhatTrangThai(don.id, trangThaiMoi)
                        }
                    )
                }
            }
        }
    }
}

// ── Nội dung tab Hồ sơ ───────────────────────────────────────────────────────
@Composable
private fun NoiDungHoSo(padding: PaddingValues, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
            .padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Avatar lớn
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(MauNau),
            contentAlignment = Alignment.Center
        ) {
            Text("NV", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Nhân viên", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MauNauDam)
        Text("Pha chế – NL Tea", fontSize = 13.sp, color = MauTextSub, modifier = Modifier.padding(top = 2.dp))
        Text("nhanvien@nlteashop.vn", fontSize = 13.sp, color = MauTextSub)

        Spacer(modifier = Modifier.height(24.dp))

        // Menu items
        ProfileMucChon(
            icon = Icons.Default.Person,
            nhan = "Thông tin cá nhân",
            onClick = {}
        )
        ProfileMucChon(
            icon = Icons.Default.Lock,
            nhan = "Đổi mật khẩu",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Đăng xuất
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFEBEE))
                .clickable { onLogout() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MauDo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đăng xuất", fontSize = 14.sp, color = MauDo, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileMucChon(icon: ImageVector, nhan: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MauNauMid, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(nhan, fontSize = 14.sp, color = MauNauDam, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = MauTextSub,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Card đơn hàng ────────────────────────────────────────────────────────────
@Composable
private fun TheDonHangStaffMoi(
    don: Order,
    dangCapNhat: Boolean,
    onBamVao: () -> Unit,
    onCapNhatTrangThai: (OrderStatus) -> Unit
) {
    val mauTrangThai = Color(android.graphics.Color.parseColor(don.status.colorHex))
    var hienDropdown by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBamVao() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // ── Dòng 1: Mã + Tên khách + Badge ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("#${don.id}", fontWeight = FontWeight.ExtraBold, color = MauNauDam, fontSize = 14.sp)
                    Text(don.customerName, fontSize = 13.sp, color = MauTextSub)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(mauTrangThai.copy(alpha = 0.13f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(don.status.label, fontSize = 11.sp, color = mauTrangThai, fontWeight = FontWeight.Bold)
                }
            }

            // ── Danh sách món ────────────────────────────────────────────────
            Text(
                don.items.joinToString(", ") { "• ${it.productName} ×${it.quantity}" },
                fontSize = 12.sp, color = MauTextSub, maxLines = 2
            )

            HorizontalDivider(color = MauNauKem.copy(alpha = 0.5f))

            // ── Tổng + phương thức ───────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("%,dđ".format(don.finalAmount), fontWeight = FontWeight.Bold, color = MauNau, fontSize = 15.sp)
                Text(don.paymentMethod, fontSize = 12.sp, color = MauTextSub)
            }

            // ── Nút cập nhật trạng thái ──────────────────────────────────────
            if (dangCapNhat) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MauNau, strokeWidth = 2.dp)
                }
            } else {
                Box {
                    OutlinedButton(
                        onClick = { hienDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Text("Cập nhật trạng thái ▾", color = MauNau, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    DropdownMenu(
                        expanded = hienDropdown,
                        onDismissRequest = { hienDropdown = false }
                    ) {
                        OrderStatus.entries
                            .filter { it != don.status }
                            .forEach { trangThai ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            trangThai.label,
                                            color = Color(android.graphics.Color.parseColor(trangThai.colorHex))
                                        )
                                    },
                                    onClick = {
                                        hienDropdown = false
                                        onCapNhatTrangThai(trangThai)
                                    }
                                )
                            }
                    }
                }
            }
        }
    }
}

// ── Dialog chi tiết ──────────────────────────────────────────────────────────
@Composable
private fun DialogStaffChiTietDon(
    don: Order,
    onDong: () -> Unit,
    onCapNhatTrangThai: (OrderStatus) -> Unit
) {
    var trangThaiChon by remember { mutableStateOf(don.status) }

    AlertDialog(
        onDismissRequest = onDong,
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Text("Đơn #${don.id}", fontWeight = FontWeight.ExtraBold, color = MauNauDam)
                Text(
                    "KH: ${don.customerName} • SĐT: ${don.customerPhone}",
                    fontSize = 13.sp, color = MauTextSub
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                don.items.forEach { item ->
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text("${item.productName} ×${item.quantity}", fontWeight = FontWeight.Medium)
                        Text(
                            "${item.size.label} | Đường ${item.sugarLevel.label} | ${item.iceLevel.label}",
                            fontSize = 12.sp, color = MauTextSub
                        )
                        if (item.toppings.isNotEmpty()) {
                            Text("+ ${item.toppings.joinToString { it.name }}", fontSize = 12.sp, color = MauNauMid)
                        }
                    }
                }

                HorizontalDivider(color = MauNauKem.copy(alpha = 0.5f))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng cộng", fontWeight = FontWeight.Bold)
                    Text("%,dđ".format(don.finalAmount), fontWeight = FontWeight.ExtraBold, color = MauNau)
                }

                if (don.note.isNotBlank()) {
                    Text("📝 ${don.note}", fontSize = 13.sp, color = MauTextSub)
                }

                HorizontalDivider(color = MauNauKem.copy(alpha = 0.5f))

                Text("Chuyển trạng thái:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MauNauDam)
                OrderStatus.entries.forEach { trangThai ->
                    val mauTT = Color(android.graphics.Color.parseColor(trangThai.colorHex))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { trangThaiChon = trangThai }
                            .background(if (trangThaiChon == trangThai) mauTT.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        RadioButton(
                            selected  = trangThaiChon == trangThai,
                            onClick   = { trangThaiChon = trangThai },
                            colors    = RadioButtonDefaults.colors(selectedColor = MauNau)
                        )
                        Text(trangThai.label, fontSize = 14.sp, color = if (trangThaiChon == trangThai) mauTT else MauNauDam)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCapNhatTrangThai(trangThaiChon) },
                colors  = ButtonDefaults.buttonColors(containerColor = MauNau),
                shape   = RoundedCornerShape(10.dp),
                enabled = trangThaiChon != don.status
            ) { Text("Lưu thay đổi", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDong) {
                Text("Đóng", color = MauTextSub)
            }
        }
    )
}

// ── Nút Tab ──────────────────────────────────────────────────────────────────
@Composable
private fun NutTab(nhan: String, dangChon: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dangChon) MauNau else MauNauKem.copy(alpha = 0.5f),
            contentColor   = if (dangChon) Color.White else MauNauDam
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(nhan, fontSize = 12.sp, fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Normal)
    }
}