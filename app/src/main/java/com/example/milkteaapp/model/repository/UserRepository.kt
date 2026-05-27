package com.example.milkteaapp.model.repository

import com.example.milkteaapp.model.data.User
import com.example.milkteaapp.model.data.UserRole
import com.example.milkteaapp.model.remote.FirestoreSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

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

    /** Lấy danh sách toàn bộ người dùng (Admin) */
    suspend fun getAllUsers(): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.getAllUsers() }
        }

    /** Lấy danh sách người dùng theo role (Admin) */
    suspend fun getUsersByRole(role: UserRole): Result<List<User>> =
        withContext(Dispatchers.IO) {
            runCatching {
                firestoreSource.getAllUsers().filter { it.role == role }
            }
        }

    // ── Cập nhật ─────────────────────────────────────────────────────────────

    /**
     * Cập nhật thông tin cá nhân của người dùng.
     * Chỉ cho phép sửa các field an toàn – role và uid không được thay đổi ở đây.
     */
    suspend fun updateProfile(
        uid: String,
        fullName: String? = null,
        phoneNumber: String? = null,
        address: String? = null,
        avatarUrl: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val fields = buildMap<String, Any?> {
                fullName?.let    { put("fullName", it) }
                phoneNumber?.let { put("phoneNumber", it) }
                address?.let     { put("address", it) }
                avatarUrl?.let   { put("avatarUrl", it) }
            }
            if (fields.isEmpty()) return@runCatching
            firestoreSource.updateUser(uid, fields)
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
                firestoreSource.updateUser(uid, mapOf("role" to newRole.name))
            }
        }


    suspend fun lockUser(uid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.setUserActive(uid, false) }
        }

    /**
     * Mở khoá tài khoản người dùng (Admin).
     */
    suspend fun unlockUser(uid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { firestoreSource.setUserActive(uid, true) }
        }
}