package com.example.milkteaapp.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
private val MauNauNhat = Color(0xFFF5F0EB)

@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,     // sau khi đăng ký → vào Home
    onNavigateToLogin: () -> Unit,    // quay lại Login
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    // Biến cục bộ cho các ô nhập liệu
    var hoTen       by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var soDienThoai by remember { mutableStateOf("") }
    var matKhau     by remember { mutableStateOf("") }
    var xacNhanMK   by remember { mutableStateOf("") }
    var hienMK      by remember { mutableStateOf(false) }

    // Điều hướng sau khi đăng ký thành công
    LaunchedEffect(destination) {
        if (destination is AuthDestination.CustomerHome) {
            onNavigateToHome()
            viewModel.onDestinationHandled()
        }
    }

    // Dùng verticalScroll để màn hình không bị tràn khi bàn phím hiện lên
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MauNauNhat)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Tạo tài khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MauNau)
        Text("Điền thông tin để đăng ký", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(4.dp))

        // Ô họ tên
        OutlinedTextField(
            value = hoTen,
            onValueChange = { hoTen = it },
            label = { Text("Họ và tên") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Ô email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Ô số điện thoại (tuỳ chọn)
        OutlinedTextField(
            value = soDienThoai,
            onValueChange = { soDienThoai = it },
            label = { Text("Số điện thoại (tuỳ chọn)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Ô mật khẩu
        OutlinedTextField(
            value = matKhau,
            onValueChange = { matKhau = it },
            label = { Text("Mật khẩu") },
            singleLine = true,
            visualTransformation = if (hienMK) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { hienMK = !hienMK }) {
                    Text(if (hienMK) "Ẩn" else "Hiện", color = MauNau)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Ô xác nhận mật khẩu
        OutlinedTextField(
            value = xacNhanMK,
            onValueChange = { xacNhanMK = it },
            label = { Text("Xác nhận mật khẩu") },
            singleLine = true,
            visualTransformation = if (hienMK) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            // Đổi màu viền đỏ nếu 2 mật khẩu không khớp
            isError = xacNhanMK.isNotBlank() && matKhau != xacNhanMK,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        if (xacNhanMK.isNotBlank() && matKhau != xacNhanMK) {
            Text("Mật khẩu không khớp", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        // Hiển thị lỗi từ ViewModel (vd: email đã tồn tại)
        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        // Nút đăng ký
        Button(
            onClick = {
                viewModel.register(
                    fullName        = hoTen,
                    email           = email,
                    password        = matKhau,
                    confirmPassword = xacNhanMK,
                    phoneNumber     = soDienThoai.ifBlank { null }
                )
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MauNau)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Đăng ký", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Link quay lại Login
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Đã có tài khoản?", color = Color.Gray, fontSize = 13.sp)
            TextButton(onClick = onNavigateToLogin) {
                Text("Đăng nhập", color = MauNau, fontWeight = FontWeight.Bold)
            }
        }
    }
}