package com.example.milkteaapp.view.customer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val MauNau     = Color(0xFF4E342E)
private val MauNauDam  = Color(0xFF3E2723)
private val MauNauNhat = Color(0xFFF5F0EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentName: String,
    currentEmail: String,
    currentAvatarUrl: String? = null,
    onBack: () -> Unit,
    // Hàm Save giờ hứng thêm cả mật khẩu cũ và mới (nếu có nhập)
    onSave: (String, Uri?, String, String) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // State cho khu vực Đổi mật khẩu
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ & Bảo mật", fontWeight = FontWeight.Bold, color = MauNauDam, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MauNauDam)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MauNauNhat)
            )
        },
        containerColor = MauNauNhat
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= PHẦN 1: THÔNG TIN CÁ NHÂN =================
            Text("THÔNG TIN CÁ NHÂN", fontWeight = FontWeight.Bold, color = MauNau, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, MauNau, CircleShape)
                    .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(selectedImageUri).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else if (!currentAvatarUrl.isNullOrEmpty()) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(currentAvatarUrl).crossfade(true).build(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Text(text = name.take(1).uppercase(), color = MauNau, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(32.dp).clip(CircleShape).background(MauNauDam).padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email (khóa cứng)
            OutlinedTextField(
                value = currentEmail, onValueChange = {}, label = { Text("Email đăng nhập") }, readOnly = true, enabled = false,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tên
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Họ và tên") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MauNau, focusedLabelColor = MauNau)
            )

            Spacer(modifier = Modifier.height(30.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // ================= PHẦN 2: ĐỔI MẬT KHẨU =================
            Text("ĐỔI MẬT KHẨU (Bỏ qua nếu không đổi)", fontWeight = FontWeight.Bold, color = MauNau, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))

            // Mật khẩu hiện tại
            OutlinedTextField(
                value = oldPassword, onValueChange = { oldPassword = it }, label = { Text("Mật khẩu hiện tại") }, singleLine = true,
                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) { Icon(if (oldPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = MauNau) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MauNau, focusedLabelColor = MauNau)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mật khẩu mới
            OutlinedTextField(
                value = newPassword, onValueChange = { newPassword = it }, label = { Text("Mật khẩu mới (ít nhất 6 ký tự)") }, singleLine = true,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) { Icon(if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = MauNau) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MauNau, focusedLabelColor = MauNau)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nhập lại mật khẩu mới
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Nhập lại mật khẩu mới") }, singleLine = true,
                isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = MauNau) } },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MauNau, focusedLabelColor = MauNau)
            )
            if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                Text("Mật khẩu xác nhận không khớp", color = Color.Red, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ================= LOGIC NÚT LƯU =================
            // Nếu có gõ vào 1 trong 3 ô password thì bắt buộc phải gõ đúng hết mới cho lưu
            val isPasswordSectionUsed = oldPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()
            val isPasswordValid = if (isPasswordSectionUsed) {
                oldPassword.isNotEmpty() && newPassword.length >= 6 && newPassword == confirmPassword
            } else {
                true // Bỏ qua nếu khách không có nhu cầu đổi pass
            }
            val isFormValid = name.isNotBlank() && isPasswordValid

            Button(
                onClick = { onSave(name, selectedImageUri, oldPassword, newPassword) },
                enabled = isFormValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauNau)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Lưu thay đổi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}