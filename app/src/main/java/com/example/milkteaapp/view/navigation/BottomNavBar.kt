package com.example.milkteaapp.view.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.milkteaapp.view.customer.HomeScreen
import com.example.milkteaapp.view.customer.MenuScreen
import com.example.milkteaapp.view.customer.OrderHistoryScreen
import com.example.milkteaapp.viewmodel.auth.AuthViewModel
import com.example.milkteaapp.viewmodel.customer.CartViewModel
import com.example.milkteaapp.viewmodel.customer.MenuViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauDam  = Color(0xFF3E2723)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauKem  = Color(0xFFD7CCC8)
private val MauNauMid  = Color(0xFF795548)
private val MauTextSub = Color(0xFF7F7571)
private val MauXam     = Color(0xFF9E9E9E)
private val MauDo      = Color(0xFFEF4444)
private val MauVang    = Color(0xFFFBC02D)
private val MauXanhLa  = Color(0xFF4F7E63)

// ─────────────────────────────────────────────────────────────────────────────
// TAB ENUM
// ─────────────────────────────────────────────────────────────────────────────
enum class CustomerTab(val icon: String, val nhan: String) {
    HOME("🏠", "Trang chủ"),
    MENU("🧋", "Thực đơn"),
    HISTORY("📋", "Đơn hàng"),
    PROFILE("👤", "Tài khoản")
}

@Composable
fun CustomerScaffold(
    navController: NavHostController,
    cartViewModel: CartViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    startTab: CustomerTab = CustomerTab.HOME
) {
    var tabHienTai by remember { mutableStateOf(startTab) }
    val cartState by cartViewModel.uiState.collectAsState()
    val soItemGio = cartState.items.sumOf { it.quantity }.toInt()

    // Lấy state của người dùng hiện tại từ Firebase
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            BottomNavBar(
                tabHienTai = tabHienTai,
                onChonTab  = { tab -> tabHienTai = tab }
            )
        },
        containerColor = MauNauNhat
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            val menuViewModel: MenuViewModel = hiltViewModel()
            val menuUiState by menuViewModel.uiState.collectAsStateWithLifecycle()

            when (tabHienTai) {
                CustomerTab.HOME -> {
                    HomeScreen(
                        customerName = authState.user?.fullName ?: "Khách hàng",
                        categories   = menuUiState.categories,
                        bestSellers  = menuUiState.allProducts.take(3),
                        soItemGio    = soItemGio,
                        onNavigateToMenu = { categoryId ->
                            menuViewModel.selectCategory(categoryId)
                            tabHienTai = CustomerTab.MENU
                        },
                        onNavigateToCart          = { navController.navigate(Route.CUSTOMER_CART) },
                        onNavigateToProductDetail = { productId ->
                            navController.navigate(Route.productDetail(productId))
                        }
                    )
                }
                CustomerTab.MENU -> {
                    MenuScreen(
                        onNavigateToProductDetail = { productId ->
                            navController.navigate(Route.productDetail(productId))
                        },
                        onBack = {
                            navController.navigate(Route.CUSTOMER_HOME) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                CustomerTab.HISTORY -> OrderHistoryScreen(
                    onBack = { tabHienTai = CustomerTab.HOME }
                )
                // 🟢 SỬA LẠI ĐOẠN NÀY
                CustomerTab.PROFILE -> {
                    TrangTaiKhoanKhachHang(
                        customerName = authState.user?.fullName ?: "Khách hàng",
                        customerEmail = authState.user?.email ?: "Chưa có email",
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
                        },
                        onNavigateToEditProfile = {
                            // 🟢 Thay Toast bằng lệnh phóng xe sang trang Edit Profile!
                            navController.navigate(Route.EDIT_PROFILE)
                        }
                    )
                }
            }
        }
    }
}

// ── Trang tài khoản khách hàng ───────────────────────────────────
@Composable
fun TrangTaiKhoanKhachHang(
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    customerName: String,
    customerEmail: String,
    memberTier: String = "Thành viên Đồng"
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MauNauDam)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tài khoản của tôi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── THÔNG TIN CÁ NHÂN KÈM HẠNG THÀNH VIÊN ───────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar: Tạm lấy chữ cái đầu của Tên (nếu sau này có ảnh thì thay bằng AsyncImage)
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MauNau),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customerName.take(1).uppercase(),
                                color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(customerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                            Text(customerEmail, fontSize = 14.sp, color = MauTextSub)
                            Spacer(modifier = Modifier.height(4.dp))
                            // Thẻ hạng thành viên
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MauVang.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Stars, contentDescription = null, tint = MauVang, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(memberTier, fontSize = 11.sp, color = MauNauDam, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Nút Edit Avatar/Tên góc phải
                        IconButton(onClick = onNavigateToEditProfile) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Chỉnh sửa", tint = MauNau)
                        }
                    }
                    // Đã dọn dẹp sạch sẽ phần Điểm thưởng ở đây!
                }
            }

            // ─── DANH MỤC TÀI KHOẢN VÀ BẢO MẬT ──────────────────────────────
            MenuSection(title = "Tài khoản & Bảo mật") {
                ProfileMenuItem(
                    icon = Icons.Rounded.AccountCircle,
                    title = "Thông tin cá nhân",
                    subtitle = "Xem và sửa tên, ảnh đại diện...",
                    onClick = onNavigateToEditProfile // Bấm vào đây cũng gọi hàm chuyển trang
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.LocationOn,
                    title = "Địa chỉ giao hàng",
                    onClick = { android.widget.Toast.makeText(context, "Chức năng đang cập nhật", android.widget.Toast.LENGTH_SHORT).show() }
                )

            }

            // ─── CÀI ĐẶT & HỖ TRỢ ───────────────────────────────────────────
            MenuSection(title = "Cài đặt ứng dụng") {
                ProfileMenuItem(
                    icon = Icons.Rounded.Notifications,
                    title = "Cài đặt thông báo",
                    onClick = { android.widget.Toast.makeText(context, "Chức năng đang cập nhật", android.widget.Toast.LENGTH_SHORT).show() }
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.HeadsetMic,
                    title = "Trung tâm hỗ trợ",
                    onClick = { android.widget.Toast.makeText(context, "Hotline: 1900 1234", android.widget.Toast.LENGTH_LONG).show() }
                )
            }

            // ─── NÚT ĐĂNG XUẤT ──────────────────────────────────────────────
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = MauDo
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun BottomNavBar(
    tabHienTai: CustomerTab,
    onChonTab: (CustomerTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        CustomerTab.entries.forEach { tab ->
            NavigationBarItem(
                selected  = tabHienTai == tab,
                onClick   = { onChonTab(tab) },
                icon      = {
                    Text(
                        tab.icon,
                        fontSize = if (tabHienTai == tab) 24.sp else 22.sp
                    )
                },
                label     = {
                    Text(
                        tab.nhan,
                        fontSize = 11.sp,
                        fontWeight = if (tabHienTai == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors    = NavigationBarItemDefaults.colors(
                    selectedTextColor   = MauNau,
                    unselectedTextColor = MauXam,
                    indicatorColor      = MauNau.copy(alpha = 0.10f)
                )
            )
        }
    }
}


@Composable
private fun MenuSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MauTextSub,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MauNauNhat),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MauNau)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MauNauDam)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = MauTextSub)
            }
        }

        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Chi tiết",
            tint = Color.LightGray
        )
    }
}