package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repository quản lý người dùng – chủ yếu dùng bởi Admin.
 * Các thao tác của chính người dùng (login/register) nằm ở [AuthRepository].
 */
@Singleton
class UserRepository @Inject constructor(
    private val firestoreSource: FirestoreSource
) {
    // ── Xem ──────────────────────────────────────────────────────────────────

    /** Lấy profile của một người dùng bất kỳ theo UID */
    suspend fun getUserById(uid: String): Result<User> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getUser(uid)
                    ?: throw Exception("Không tìm thấy người dùng.")
            }
        }

    /** Lấy danh sách toàn bộ người dùng (Admin) - Lấy từ Firestore collection "users" */
    suspend fun getAllUsers(): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                suspendCancellableCoroutine { continuation ->
                    db.collection("users").get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val users = task.result?.documents?.mapNotNull { doc ->
                                    User.fromMap(doc.data ?: emptyMap())
                                } ?: emptyList()
                                continuation.resume(users)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Get all users failed"))
                            }
                        }
                }
            }
        }

    /** Lấy danh sách người dùng theo role (Admin) */
    suspend fun getUsersByRole(role: UserRole): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val allUsersResult = getAllUsers().getOrThrow()
                allUsersResult.filter { it.role == role }
            }
        }

    // ── Sửa ──────────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin profile (họ tên, sđt, địa chỉ...) của người dùng.
     * FIX: buildMap<String, Any> thay vì <String, Any?> để tránh crash Firestore
     */
    suspend fun updateProfile(
        uid: String,
        fullName: String? = null,
        phoneNumber: String? = null,
        address: String? = null,
        avatarUrl: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val fields = buildMap<String, Any> {
                fullName?.let    { put("fullName", it) }
                phoneNumber?.let { put("phoneNumber", it) }
                address?.let     { put("address", it) }
                avatarUrl?.let   { put("avatarUrl", it) }
            }
            if (fields.isEmpty()) return@runCatching

            suspendCancellableCoroutine { continuation ->
                db.collection("users").document(uid).update(fields)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Unit)
                        } else {
                            continuation.resumeWithException(task.exception ?: Exception("Update profile failed"))
                        }
                    }
            }
        }
    }

    // ── Quản lý (Admin) ───────────────────────────────────────────────────────

    /**
     * Thay đổi vai trò người dùng (Admin).
     * Dùng để nâng cấp khách thành nhân viên hoặc hạ cấp.
     */
    suspend fun changeUserRole(uid: String, newRole: UserRole): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                suspendCancellableCoroutine { continuation ->
                    db.collection("users").document(uid).update("role", newRole.name)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Change role failed"))
                            }
                        }
                }
            }
        }

    /** Khóa tài khoản người dùng */
    suspend fun lockUser(uid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                suspendCancellableCoroutine { continuation ->
                    db.collection("users").document(uid).update("isActive", false)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Lock user failed"))
                            }
                        }
                }
            }
        }

    /**
     * Mở khoá tài khoản người dùng (Admin).
     */
    suspend fun unlockUser(uid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                suspendCancellableCoroutine { continuation ->
                    db.collection("users").document(uid).update("isActive", true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Unlock user failed"))
                            }
                        }
                }
            }
        }
}