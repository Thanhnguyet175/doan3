package com.example.milkteaapp.model.repository

import android.content.Context
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.remote.FirebaseAuthSource
import com.example.milkteaapp.model.remote.FirestoreSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource,
    @ApplicationContext private val context: Context
) {
    /** UID người dùng hiện tại, null nếu chưa đăng nhập */
    val currentUid: String? get() = authSource.currentUid

    /** Trả về true nếu đang có session đăng nhập */
    val isLoggedIn: Boolean get() = authSource.currentUser != null

    // ── Đăng ký (Khách hàng tự đăng ký) ─────────────────────────────────────

    /**
     * Đăng ký tài khoản mới – luôn tạo với role = CUSTOMER.
     * Dùng cho màn hình Register của khách hàng.
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        phoneNumber: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseUser = authSource.register(email, password)
            val user = User(
                uid         = firebaseUser.uid,
                fullName    = fullName,
                email       = email,
                phoneNumber = phoneNumber,
                role        = UserRole.CUSTOMER
            )
            firestoreSource.saveUser(user)
            user
        }
    }

    // ── Tạo tài khoản có phân quyền (Admin tạo Staff/Admin) ──────────────────

    /**
     * Admin tạo tài khoản Staff hoặc Admin mới mà KHÔNG bị đăng xuất.
     *
     * Dùng Firebase App phụ (secondary app) để tạo auth riêng biệt,
     * sau đó lưu profile với role chỉ định vào Firestore.
     *
     * @param fullName   Họ tên đầy đủ
     * @param email      Email đăng nhập
     * @param password   Mật khẩu (tối thiểu 6 ký tự)
     * @param role       STAFF hoặc ADMIN (không cho phép CUSTOMER ở đây)
     * @param phoneNumber Số điện thoại tuỳ chọn
     */
    suspend fun registerWithRole(
        fullName: String,
        email: String,
        password: String,
        role: UserRole,
        phoneNumber: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            if (role == UserRole.CUSTOMER) {
                throw Exception("Dùng hàm register() thông thường cho khách hàng.")
            }

            // Tạo tài khoản Auth bằng app phụ → Admin không bị đăng xuất
            val newUid = authSource.registerSecondary(email, password, context)

            val user = User(
                uid         = newUid,
                fullName    = fullName,
                email       = email,
                phoneNumber = phoneNumber,
                role        = role
            )
            firestoreSource.saveUser(user)
            user
        }
    }

    // ── Đăng nhập ────────────────────────────────────────────────────────────

    /**
     * Đăng nhập và lấy profile từ Firestore.
     * @return [Result.success] chứa [User] nếu thành công
     */
    suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                val firebaseUser = authSource.login(email, password)
                val user = firestoreSource.getUser(firebaseUser.uid)
                    ?: throw Exception("Không tìm thấy thông tin người dùng.")

                if (!user.isActive) throw Exception("Tài khoản đã bị khoá. Vui lòng liên hệ quản trị.")
                user
            }
        }

    // ── Lấy profile hiện tại ─────────────────────────────────────────────────

    /**
     * Lấy profile của người dùng đang đăng nhập từ Firestore.
     * Dùng khi khởi động app (session còn hiệu lực).
     */
    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = authSource.currentUid
                ?: throw Exception("Chưa đăng nhập.")
            firestoreSource.getUser(uid)
                ?: throw Exception("Không tìm thấy profile người dùng.")
        }
    }

    // ── Đặt lại mật khẩu ─────────────────────────────────────────────────────

    suspend fun sendPasswordReset(email: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { authSource.sendPasswordReset(email) }
        }

    // ── Đăng xuất ────────────────────────────────────────────────────────────

    fun logout() = authSource.logout()
}