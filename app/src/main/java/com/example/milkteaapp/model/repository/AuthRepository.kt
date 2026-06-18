package com.example.milkteaapp.model.repository

import android.content.Context
import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.remote.FirebaseAuthSource
import com.example.milkteaapp.model.remote.FirestoreSource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRepository @Inject constructor(
    private val authSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource,
    @ApplicationContext private val context: Context
) {
    val currentUid: String? get() = authSource.currentUid
    val isLoggedIn: Boolean get() = authSource.currentUser != null

    suspend fun register(fullName: String, email: String, password: String, phoneNumber: String? = null): Result<User> = withContext(Dispatchers.IO) {
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

    suspend fun registerWithRole(fullName: String, email: String, password: String, role: UserRole, phoneNumber: String? = null): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            if (role == UserRole.CUSTOMER) throw Exception("Dùng hàm register() thông thường cho khách hàng.")
            val newUid = authSource.registerSecondary(email, password, context)
            val user = User(uid = newUid, fullName = fullName, email = email, phoneNumber = phoneNumber, role = role)
            firestoreSource.saveUser(user)
            user
        }
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseUser = authSource.login(email, password)
            val user = firestoreSource.getUser(firebaseUser.uid) ?: throw Exception("Không tìm thấy thông tin người dùng.")
            if (!user.isActive) throw Exception("Tài khoản đã bị khoá. Vui lòng liên hệ quản trị.")
            user
        }
    }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        runCatching {
            val uid = authSource.currentUid ?: throw Exception("Chưa đăng nhập.")
            firestoreSource.getUser(uid) ?: throw Exception("Không tìm thấy profile người dùng.")
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { authSource.sendPasswordReset(email) }
    }

    fun logout() = authSource.logout()

    // 🟢 MỚI THÊM: Cập nhật Tên và Ảnh lên Firestore
    suspend fun updateProfile(uid: String, newName: String, newAvatarUrl: String?): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val db = FirebaseFirestore.getInstance()
            val updates = mutableMapOf<String, Any>()
            updates["fullName"] = newName
            updates["name"] = newName // Lưu cả 2 cho chắc cốp, tuỳ model gọi cái nào

            if (newAvatarUrl != null) {
                updates["avatarUrl"] = newAvatarUrl // Giả định có trường này trong DB
            }

            suspendCancellableCoroutine { continuation ->
                db.collection("users").document(uid).update(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(Unit)
                    else continuation.resumeWithException(task.exception ?: Exception("Cập nhật hồ sơ thất bại"))
                }
            }
        }
    }

    // 🟢 MỚI THÊM: Đổi mật khẩu cực an toàn với Re-Authenticate
    suspend fun changePassword(email: String, oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = FirebaseAuth.getInstance().currentUser ?: throw Exception("Chưa đăng nhập")
            val credential = EmailAuthProvider.getCredential(email, oldPass)

            suspendCancellableCoroutine { continuation ->
                // 1. Gõ cửa kiểm tra mật khẩu cũ
                user.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 2. Đúng mật khẩu thì cho phép đổi
                        user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) continuation.resume(Unit)
                            else continuation.resumeWithException(updateTask.exception ?: Exception("Đổi mật khẩu thất bại"))
                        }
                    } else {
                        continuation.resumeWithException(Exception("Mật khẩu hiện tại không đúng!"))
                    }
                }
            }
        }
    }
}