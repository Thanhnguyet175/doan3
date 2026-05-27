package com.example.milkteaapp.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.milkteaapp.view.customer.HomeScreen
import com.example.milkteaapp.view.customer.MenuScreen
import com.example.milkteaapp.view.customer.OrderHistoryScreen
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
    startTab: CustomerTab = CustomerTab.HOME
) {
    var tabHienTai by remember { mutableStateOf(startTab) }
    val cartState by cartViewModel.uiState.collectAsState()
    val soItemGio = cartState.items.sumOf { it.quantity }.toInt()

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
                        customerName = "Khách hàng",
                        categories   = menuUiState.categories,
                        bestSellers  = menuUiState.allProducts.filter { it.isBestSeller },
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
                        viewModel = menuViewModel
                    )
                }
                CustomerTab.HISTORY -> OrderHistoryScreen(
                    onBack = { tabHienTai = CustomerTab.HOME }
                )
                CustomerTab.PROFILE -> {
                    TrangTaiKhoanKhachHang(
                        onLogout = {
                            navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
            }
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

// ── Trang tài khoản khách hàng (thay thế GiaoDienTaiKhoanTam) ────────────────
@Composable
fun TrangTaiKhoanKhachHang(onLogout: () -> Unit) {
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
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Text("Tài khoản", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(20.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MauNau),
                contentAlignment = Alignment.Center
            ) {
                Text("KH", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.size(12.dp))
            Text("Khách hàng", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MauNauDam)
            Text("khachhang@email.com", fontSize = 13.sp, color = MauTextSub)
            Spacer(modifier = Modifier.size(24.dp))

            // Menu items
            ListItem(
                headlineContent = { Text("Thông tin cá nhân") },
                leadingContent  = { Icon(Icons.Default.AccountCircle, null, tint = MauNauMid) },
                trailingContent = { Text("›", color = MauTextSub, fontSize = 18.sp) },
                colors = ListItemDefaults.colors(containerColor = Color.White),
                modifier = Modifier.clip(MaterialTheme.shapes.medium).padding(bottom = 2.dp)
            )
            ListItem(
                headlineContent = { Text("Thông báo") },
                leadingContent  = { Icon(Icons.Default.Notifications, null, tint = MauNauMid) },
                trailingContent = { Text("›", color = MauTextSub, fontSize = 18.sp) },
                colors = ListItemDefaults.colors(containerColor = Color.White),
                modifier = Modifier.clip(MaterialTheme.shapes.medium).padding(bottom = 2.dp)
            )
            ListItem(
                headlineContent = { Text("Đổi mật khẩu") },
                leadingContent  = { Icon(Icons.Default.Lock, null, tint = MauNauMid) },
                trailingContent = { Text("›", color = MauTextSub, fontSize = 18.sp) },
                colors = ListItemDefaults.colors(containerColor = Color.White),
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                shape  = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MauDo)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Đăng xuất", color = MauDo, fontWeight = FontWeight.Bold)
            }
        }
    }
}