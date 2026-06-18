package com.example.milkteaapp.viewmodel.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.repository.AuthRepository
import com.example.milkteaapp.model.source.CloudinarySource // 🟢 THÊM IMPORT NÀY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed class AuthDestination {
    data object CustomerHome : AuthDestination()
    data object StaffDashboard : AuthDestination()
    data object AdminDashboard : AuthDestination()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storageSource: CloudinarySource, // 🟢 Đưa Cloudinary vào đây
    @ApplicationContext private val context: Context // 🟢 Đưa Context vào để đọc ảnh
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _destination = MutableStateFlow<AuthDestination?>(null)
    val destination: StateFlow<AuthDestination?> = _destination.asStateFlow()

    init { checkSession() }

    fun checkSession() {
        if (!authRepository.isLoggedIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    _destination.value = user.role.toDestination()
                },
                onFailure = { _uiState.update { it.copy(isLoading = false) } }
            )
        }
    }

    // ... [Các hàm login, register, sendPasswordReset, logout cũ của má giữ nguyên] ...

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(email.trim(), password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    _destination.value = user.role.toDestination()
                },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            )
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String, phoneNumber: String? = null) {
        if (!validateRegisterInput(fullName, email, password, confirmPassword)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(fullName.trim(), email.trim(), password, phoneNumber?.trim()).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    _destination.value = AuthDestination.CustomerHome
                },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) { _uiState.update { it.copy(errorMessage = "Vui lòng nhập email.") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.sendPasswordReset(email.trim()).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, successMessage = "Email đặt lại mật khẩu đã được gửi.") } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
        _destination.value = null
    }

    // 🟢 MỚI THÊM: Đổi ảnh từ Uri sang File Path để Cloudinary đọc
    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val filePath = context.cacheDir.absolutePath + "/temp_avatar.jpg"
            val file = java.io.File(filePath)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
            }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    // 🟢 MỚI THÊM: Hàm siêu cấp gánh team Lưu hồ sơ
    fun updateUserProfile(newName: String, imageUri: Uri?, oldPass: String, newPass: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            // 1. Xử lý đổi mật khẩu (nếu khách có nhập vào ô)
            if (oldPass.isNotEmpty() && newPass.isNotEmpty()) {
                val passResult = authRepository.changePassword(currentUser.email, oldPass, newPass)
                if (passResult.isFailure) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = passResult.exceptionOrNull()?.message) }
                    return@launch // Lỗi phát là dừng luôn không làm tiếp
                }
            }

            // 2. Upload ảnh lên Cloudinary (nếu khách có chọn ảnh mới)
            var uploadedAvatarUrl: String? = null
            if (imageUri != null) {
                val path = getFilePathFromUri(imageUri)
                if (path != null) {
                    runCatching {
                        uploadedAvatarUrl = storageSource.uploadImage(path)
                    }.onFailure {
                        _uiState.update { state -> state.copy(isLoading = false, errorMessage = "Lỗi tải ảnh lên!") }
                        return@launch
                    }
                }
            }

            // 3. Cập nhật Tên và Link ảnh lên Firestore
            val profileResult = authRepository.updateProfile(currentUser.uid, newName, uploadedAvatarUrl)

            if (profileResult.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Cập nhật thông tin thành công!") }
                checkSession() // F5 tải lại thông tin mới nhất gắn lên màn hình
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = profileResult.exceptionOrNull()?.message) }
            }
        }
    }

    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }
    fun onDestinationHandled() { _destination.value = null }

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank()    -> { _uiState.update { it.copy(errorMessage = "Vui lòng nhập email.") };    false }
            password.isBlank() -> { _uiState.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu.") }; false }
            else               -> true
        }
    }

    private fun validateRegisterInput(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            fullName.isBlank()              -> { error("Vui lòng nhập họ tên."); false }
            email.isBlank()                 -> { error("Vui lòng nhập email."); false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { error("Email không hợp lệ."); false }
            password.length < 6             -> { error("Mật khẩu tối thiểu 6 ký tự."); false }
            password != confirmPassword     -> { error("Mật khẩu xác nhận không khớp."); false }
            else                            -> true
        }
    }
    private fun error(msg: String) = _uiState.update { it.copy(errorMessage = msg) }
    private fun UserRole.toDestination(): AuthDestination = when (this) {
        UserRole.CUSTOMER -> AuthDestination.CustomerHome
        UserRole.STAFF    -> AuthDestination.StaffDashboard
        UserRole.ADMIN    -> AuthDestination.AdminDashboard
    }
}