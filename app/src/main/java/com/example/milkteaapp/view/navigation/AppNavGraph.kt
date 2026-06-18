package com.example.milkteaapp.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.milkteaapp.view.admin.AdminCategoryScreen
import com.example.milkteaapp.view.admin.AdminDashboardScreen
import com.example.milkteaapp.view.admin.AdminProductScreen
import com.example.milkteaapp.view.admin.AdminStatsScreen
import com.example.milkteaapp.view.admin.AdminUserScreen
import com.example.milkteaapp.view.auth.LoginScreen
import com.example.milkteaapp.view.auth.RegisterScreen
import com.example.milkteaapp.view.customer.CartScreen
import com.example.milkteaapp.view.customer.EditProfileScreen
import com.example.milkteaapp.view.customer.ProductDetailScreen
import com.example.milkteaapp.view.staff.StaffOrderManagementScreen
import com.example.milkteaapp.viewmodel.auth.AuthDestination
import com.example.milkteaapp.viewmodel.auth.AuthViewModel
import com.example.milkteaapp.viewmodel.customer.CartViewModel

object Route {
    const val LOGIN    = "login"
    const val REGISTER = "register"

    const val CUSTOMER_HOME  = "customer_home"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val CUSTOMER_CART  = "customer_cart"

    // Staff
    const val STAFF_DASHBOARD = "staff_dashboard"

    // Admin
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_PRODUCT   = "admin_product"
    const val ADMIN_CATEGORY  = "admin_category"
    const val ADMIN_USER      = "admin_user"
    const val ADMIN_STATS     = "admin_stats"

    const val EDIT_PROFILE = "edit_profile"
    fun productDetail(productId: String) = "product_detail/$productId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val destination by authViewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            is AuthDestination.CustomerHome -> {
                navController.navigate(Route.CUSTOMER_HOME) { popUpTo(Route.LOGIN) { inclusive = true } }
            }
            is AuthDestination.StaffDashboard -> {
                navController.navigate(Route.STAFF_DASHBOARD) { popUpTo(Route.LOGIN) { inclusive = true } }
            }
            is AuthDestination.AdminDashboard -> {
                navController.navigate(Route.ADMIN_DASHBOARD) { popUpTo(Route.LOGIN) { inclusive = true } }
            }
            null -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.LOGIN
    ) {
        // ── AUTH ─────────────────────────────────────────────────────────────
        composable(Route.LOGIN) {
            LoginScreen(
                onNavigateToCustomerHome    = { navController.navigate(Route.CUSTOMER_HOME) { popUpTo(Route.LOGIN) { inclusive = true } } },
                onNavigateToStaffDashboard  = { navController.navigate(Route.STAFF_DASHBOARD) { popUpTo(Route.LOGIN) { inclusive = true } } },
                onNavigateToAdminDashboard  = { navController.navigate(Route.ADMIN_DASHBOARD) { popUpTo(Route.LOGIN) { inclusive = true } } },
                onNavigateToRegister        = { navController.navigate(Route.REGISTER) }
            )
        }

        composable(Route.REGISTER) {
            RegisterScreen(
                onNavigateToHome  = { navController.navigate(Route.CUSTOMER_HOME) { popUpTo(Route.LOGIN) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── CUSTOMER ─────────────────────────────────────────────────────────
        composable(Route.CUSTOMER_HOME) {
            // Gọi trực tiếp hàm CustomerScaffold bên file BottomNavBar.kt
            CustomerScaffold(navController = navController, cartViewModel = cartViewModel)
        }

        composable(Route.PRODUCT_DETAIL) { backStack ->
            val productId = backStack.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(onBack = { navController.popBackStack() }, cartViewModel = cartViewModel)
        }

        // Màn hình giỏ hàng độc lập
        composable(Route.CUSTOMER_CART) {
            CartScreen(
                onBack             = { navController.popBackStack() },
                onDatHangThanhCong = { navController.navigate(Route.CUSTOMER_HOME) { popUpTo(Route.CUSTOMER_HOME) { inclusive = true } } },
                viewModel          = cartViewModel
            )
        }

        // ── EDIT PROFILE (ĐÃ SỬA LỖI ONSAVE) ─────────────────────────────────
        composable(Route.EDIT_PROFILE) {
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()

            EditProfileScreen(
                currentName = authState.user?.fullName ?: "",
                currentEmail = authState.user?.email ?: "",
                currentAvatarUrl = null,
                onBack = { navController.popBackStack() },
                onSave = { newName, newImageUri, oldPass, newPass ->
                    // Gọi hàm cập nhật
                    authViewModel.updateUserProfile(newName, newImageUri, oldPass, newPass)

                    // Hiện thông báo và lùi về trang trước
                    android.widget.Toast.makeText(navController.context, "Đã lưu toàn bộ thông tin!", android.widget.Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }

        // ── STAFF ────────────────────────────────────────────────────────────
        composable(Route.STAFF_DASHBOARD) {
            StaffOrderManagementScreen(onBack = {
                authViewModel.logout()
                navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }

        // ── ADMIN ────────────────────────────────────────────────────────────
        composable(Route.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToProducts = { navController.navigate(Route.ADMIN_PRODUCT) },
                onNavigateToCategories = { navController.navigate(Route.ADMIN_CATEGORY) },
                onNavigateToUsers = { navController.navigate(Route.ADMIN_USER) },
                onNavigateToStats = { navController.navigate(Route.ADMIN_STATS) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Route.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                productsScreenContent = { AdminProductScreen(onBack = { }) },
                categoriesScreenContent = { AdminCategoryScreen(onNavigateToEditCategory = { }) },
                usersScreenContent = { AdminUserScreen(onBack = {}) },
                statsScreenContent = { AdminStatsScreen(onBack = {}) }
            )
        }

        composable(Route.ADMIN_PRODUCT) { AdminProductScreen(onBack = { navController.popBackStack() }) }
        composable(Route.ADMIN_CATEGORY) { AdminCategoryScreen(onNavigateToEditCategory = { }) }
        composable(Route.ADMIN_USER) { AdminUserScreen(onBack = { navController.popBackStack() }) }
        composable(Route.ADMIN_STATS) { AdminStatsScreen(onBack = { navController.popBackStack() }) }
    }
}