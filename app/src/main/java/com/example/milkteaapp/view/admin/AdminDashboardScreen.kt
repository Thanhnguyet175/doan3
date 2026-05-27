package com.example.milkteaapp.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import kotlinx.coroutines.launch

private val MauNauDam  = Color(0xFF3E2723)
private val MauNau     = Color(0xFF4E342E)
private val MauNauMid  = Color(0xFF795548)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauKem  = Color(0xFFD7CCC8)
private val MauDo      = Color(0xFFEF4444)
private val CardWhite  = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToProducts: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogout: () -> Unit,
    productsScreenContent: @Composable () -> Unit,
    categoriesScreenContent: @Composable () -> Unit,
    usersScreenContent: @Composable () -> Unit,
    statsScreenContent: @Composable () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tabTitles = listOf("Thống Kê Doanh Thu", "Quản Lý Sản Phẩm", "Quản Lý Danh Mục", "Quản Lý Người Dùng", "Hồ Sơ & Cài Đặt")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MauNauNhat,
                modifier = Modifier.width(285.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MauNauDam)
                        .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp)
                ) {
                    Column {
                        Text("HỆ THỐNG QUẢN LÝ", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f), letterSpacing = 1.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Trà Sữa NL Admin", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MauNauKem.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("TÀI KHOẢN", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp, color = MauNauMid,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                NavigationDrawerItem(
                    label = { Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold) },
                    selected = false,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MauDo) },
                    onClick = { scope.launch { drawerState.close() }; onLogout() },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color(0xFFFFEBEE),
                        unselectedTextColor = MauDo, unselectedIconColor = MauDo),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MauNauNhat,
            topBar = {
                TopAppBar(
                    title = {
                        Text(tabTitles.getOrElse(selectedTab) { "Admin" },
                            fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MauNauDam)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Mở menu", tint = MauNauDam)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier.padding(end = 14.dp).size(36.dp)
                                .clip(CircleShape).background(MauNau),
                            contentAlignment = Alignment.Center
                        ) { Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MauNauNhat)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = CardWhite, tonalElevation = 0.dp) {
                    NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                        label = { Text("Thống kê", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.BarChart, null, modifier = Modifier.size(22.dp)) },
                        colors = adminNavColors())
                    NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                        label = { Text("Sản phẩm", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.Coffee, null, modifier = Modifier.size(22.dp)) },
                        colors = adminNavColors())
                    NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                        label = { Text("Danh mục", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.GridView, null, modifier = Modifier.size(22.dp)) },
                        colors = adminNavColors())
                    NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 },
                        label = { Text("Người dùng", fontSize = 10.sp) },
                        icon = { Icon(Icons.Default.People, null, modifier = Modifier.size(22.dp)) },
                        colors = adminNavColors())

                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (selectedTab) {
                    0 -> statsScreenContent()
                    1 -> productsScreenContent()
                    2 -> categoriesScreenContent()
                    3 -> usersScreenContent()

                }
            }
        }
    }
}

@Composable
private fun adminNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = Color(0xFF4E342E),
    selectedTextColor   = Color(0xFF4E342E),
    unselectedIconColor = Color(0xFF9E9E9E),
    unselectedTextColor = Color(0xFF9E9E9E),
    indicatorColor      = Color(0xFFF5F0EB)
)