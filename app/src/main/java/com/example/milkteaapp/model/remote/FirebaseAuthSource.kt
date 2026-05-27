package com.example.milkteaapp.model.remote

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lớp nguồn dữ liệu xác thực – bao bọc Firebase Authentication.
 * Chỉ xử lý các tác vụ Auth thuần tuý, không biết gì về business logic.
 */
@Singleton
class FirebaseAuthSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    /** Người dùng đang đăng nhập, null nếu chưa đăng nhập */
    val currentUser: FirebaseUser? get() = auth.currentUser

    /** UID của người dùng hiện tại */
    val currentUid: String? get() = auth.currentUser?.uid

    // ── Đăng ký ──────────────────────────────────────────────────────────────

    /**
     * Tạo tài khoản mới bằng email + password.
     * @return [FirebaseUser] nếu thành công
     * @throws Exception nếu email đã tồn tại hoặc password yếu
     */
    suspend fun register(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Không thể tạo tài khoản.")
    }

    // ── Đăng ký tài khoản phụ (Admin tạo Staff/Admin mới) ────────────────────

    /**
     * Tạo tài khoản mới bằng FirebaseApp phụ để KHÔNG đăng xuất Admin hiện tại.
     *
     * Cách hoạt động:
     * 1. Tạo một FirebaseApp tạm thời với cùng config
     * 2. Dùng FirebaseAuth của app tạm đó để tạo tài khoản
     * 3. Đăng xuất + xoá app tạm ngay sau khi lấy được UID
     *
     * @return UID của tài khoản vừa tạo
     */
    suspend fun registerSecondary(email: String, password: String, context: android.content.Context): String {
        val primaryApp = FirebaseApp.getInstance()
        val options = primaryApp.options

        // Tên app tạm – dùng timestamp để tránh trùng
        val tempAppName = "temp_create_${System.currentTimeMillis()}"

        val tempApp = try {
            FirebaseApp.initializeApp(context, options, tempAppName)
                ?: throw Exception("Không thể khởi tạo Firebase app phụ.")
        } catch (e: Exception) {
            // Nếu app tên này đã tồn tại thì lấy lại
            FirebaseApp.getInstance(tempAppName)
        }

        return try {
            val tempAuth = FirebaseAuth.getInstance(tempApp)
            val result = tempAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Không thể tạo tài khoản.")
            tempAuth.signOut()
            uid
        } finally {
            tempApp.delete()
        }
    }

    // ── Đăng nhập ────────────────────────────────────────────────────────────

    /**
     * Đăng nhập bằng email + password.
     * @return [FirebaseUser] nếu thành công
     * @throws Exception nếu sai thông tin
     */
    suspend fun login(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Đăng nhập thất bại.")
    }

    // ── Đăng xuất ────────────────────────────────────────────────────────────

    fun logout() = auth.signOut()

    // ── Đặt lại mật khẩu ─────────────────────────────────────────────────────

    /**
     * Gửi email đặt lại mật khẩu.
     * @throws Exception nếu email không tồn tại
     */
    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Xoá tài khoản (Admin) ────────────────────────────────────────────────

    /**
     * Xoá tài khoản Auth của người dùng hiện tại.
     * Chỉ dùng khi Admin muốn xoá chính mình,
     * hoặc kết hợp với Admin SDK phía server.
     */
    suspend fun deleteCurrentUser() {
        auth.currentUser?.delete()?.await()
            ?: throw Exception("Không có người dùng nào đang đăng nhập.")
    }
}