package com.example.milkteaapp.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _destination = MutableStateFlow<AuthDestination?>(null)
    val destination: StateFlow<AuthDestination?> = _destination.asStateFlow()


    fun checkSession() {
        if (!authRepository.isLoggedIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    _destination.value = user.role.toDestination()
                },
                onFailure = {
                    // Session hết hạn hoặc user bị xoá → không điều hướng
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }

    // ── Đăng nhập ────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(email.trim(), password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    _destination.value = user.role.toDestination()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
            )
        }
    }


    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String? = null
    ) {
        if (!validateRegisterInput(fullName, email, password, confirmPassword)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(fullName.trim(), email.trim(), password, phoneNumber?.trim())
                .fold(
                    onSuccess = { user ->
                        _uiState.update { it.copy(isLoading = false, user = user) }
                        _destination.value = AuthDestination.CustomerHome
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = e.message)
                        }
                    }
                )
        }
    }


    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.sendPasswordReset(email.trim()).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Email đặt lại mật khẩu đã được gửi."
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
            )
        }
    }


    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
        _destination.value = null
    }

    // ── Reset messages ────────────────────────────────────────────────────────

    fun clearError()   = _uiState.update { it.copy(errorMessage = null) }
    fun clearSuccess() = _uiState.update { it.copy(successMessage = null) }

    /** Gọi sau khi Navigation đã xử lý destination */
    fun onDestinationHandled() { _destination.value = null }

    // ── Validation ───────────────────────────────────────────────────────────

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank()    -> { _uiState.update { it.copy(errorMessage = "Vui lòng nhập email.") };    false }
            password.isBlank() -> { _uiState.update { it.copy(errorMessage = "Vui lòng nhập mật khẩu.") }; false }
            else               -> true
        }
    }

    private fun validateRegisterInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            fullName.isBlank()              -> { error("Vui lòng nhập họ tên."); false }
            email.isBlank()                 -> { error("Vui lòng nhập email."); false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                -> { error("Email không hợp lệ."); false }
            password.length < 6             -> { error("Mật khẩu tối thiểu 6 ký tự."); false }
            password != confirmPassword     -> { error("Mật khẩu xác nhận không khớp."); false }
            else                            -> true
        }
    }

    private fun error(msg: String) = _uiState.update { it.copy(errorMessage = msg) }

    // ── Extension ────────────────────────────────────────────────────────────

    private fun UserRole.toDestination(): AuthDestination = when (this) {
        UserRole.CUSTOMER -> AuthDestination.CustomerHome
        UserRole.STAFF    -> AuthDestination.StaffDashboard
        UserRole.ADMIN    -> AuthDestination.AdminDashboard
    }
}