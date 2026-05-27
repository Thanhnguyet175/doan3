package com.example.milkteaapp.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.milkteaapp.viewmodel.auth.AuthDestination
import com.example.milkteaapp.viewmodel.auth.AuthViewModel

private val MauNau     = Color(0xFF4E342E)
private val MauNauDam  = Color(0xFF3E2723)
private val MauNauNhat = Color(0xFFF5F0EB)
private val MauNauKem  = Color(0xFFD7CCC8)
private val MauXanh    = Color(0xFF4A7C59)
private val MauTextSub = Color(0xFF7F7571)

@Composable
fun LoginScreen(
    onNavigateToCustomerHome: () -> Unit,
    onNavigateToStaffDashboard: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var hienMatKhau by remember { mutableStateOf(false) }

    LaunchedEffect(destination) {
        when (destination) {
            is AuthDestination.CustomerHome   -> { onNavigateToCustomerHome();   viewModel.onDestinationHandled() }
            is AuthDestination.StaffDashboard -> { onNavigateToStaffDashboard(); viewModel.onDestinationHandled() }
            is AuthDestination.AdminDashboard -> { onNavigateToAdminDashboard(); viewModel.onDestinationHandled() }
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Logo / Icon ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MauNau),
                contentAlignment = Alignment.Center
            ) {
                Text("☕", fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NL Tea",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MauNauDam
            )
            Text(
                text = "Chào mừng bạn trở lại 👋",
                fontSize = 14.sp,
                color = MauTextSub,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // ── Email Field ──────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "EMAIL",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548), letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("your@email.com", color = MauNauKem) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MauNau,
                        unfocusedBorderColor = MauNauKem,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Password Field ───────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "MẬT KHẨU",
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548), letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = matKhau,
                    onValueChange = { matKhau = it },
                    placeholder = { Text("••••••••", color = MauNauKem) },
                    singleLine = true,
                    visualTransformation = if (hienMatKhau) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { hienMatKhau = !hienMatKhau }) {
                            Icon(
                                imageVector = if (hienMatKhau) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (hienMatKhau) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = MauTextSub
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MauNau,
                        unfocusedBorderColor = MauNauKem,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // ── Quên mật khẩu ────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { viewModel.sendPasswordReset(email) }) {
                    Text("Quên mật khẩu?", color = MauNau, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Thông báo lỗi / thành công ───────────────────────────────────
            if (uiState.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (uiState.successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = MauXanh,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ── Nút Đăng nhập ────────────────────────────────────────────────
            Button(
                onClick = { viewModel.login(email, matKhau) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MauNau,
                    disabledContainerColor = MauNauKem
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // ── Divider ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MauNauKem)
                Text("  hoặc  ", color = MauTextSub, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MauNauKem)
            }

            // ── Link Đăng ký ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản?", color = MauTextSub, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Đăng ký ngay", color = MauNau, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}