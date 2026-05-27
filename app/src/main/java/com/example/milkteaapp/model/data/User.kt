package com.example.milkteaapp.model.data

import com.google.firebase.Timestamp

/**
 * Enu định nghĩa các vai trò người dùng trong hệ thống.
 * - CUSTOMER : Khách hàng đặt món
 * - STAFF    : Nhân viên pha chế / xử lý đơn
 * - ADMIN    : Quản trị viên toàn quyền
 */
enum class UserRole {
    CUSTOMER,
    STAFF,
    ADMIN
}

/**
 * Data class đại diện cho một người dùng trong hệ thống.
 *
 * @param uid          ID duy nhất do Firebase Auth sinh ra
 * @param fullName     Họ và tên đầy đủ
 * @param email        Địa chỉ email (dùng để đăng nhập)
 * @param phoneNumber  Số điện thoại liên lạc (tuỳ chọn)
 * @param avatarUrl    URL ảnh đại diện lưu trên Firebase Storage (tuỳ chọn)
 * @param role         Vai trò trong hệ thống, mặc định là CUSTOMER
 * @param address      Địa chỉ giao hàng mặc định (tuỳ chọn)
 * @param createdAt    Thời điểm tạo tài khoản
 * @param isActive     Trạng thái hoạt động của tài khoản (Admin có thể khoá)
 */
data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val address: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
) {
    /**
     * Chuyển đổi sang Map để lưu lên Firestore.
     * UserRole được lưu dưới dạng String để dễ đọc trên console Firebase.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "uid"         to uid,
        "fullName"    to fullName,
        "email"       to email,
        "phoneNumber" to phoneNumber,
        "avatarUrl"   to avatarUrl,
        "role"        to role.name,          // lưu "CUSTOMER" / "STAFF" / "ADMIN"
        "address"     to address,
        "createdAt"   to createdAt,
        "isActive"    to isActive
    )

    companion object {
        /**
         * Khôi phục User từ Map Firestore snapshot.
         * Nếu role không hợp lệ sẽ fallback về CUSTOMER.
         */
        fun fromMap(map: Map<String, Any?>): User = User(
            uid         = map["uid"] as? String ?: "",
            fullName    = map["fullName"] as? String ?: "",
            email       = map["email"] as? String ?: "",
            phoneNumber = map["phoneNumber"] as? String,
            avatarUrl   = map["avatarUrl"] as? String,
            role        = runCatching {
                UserRole.valueOf(map["role"] as? String ?: "")
            }.getOrDefault(UserRole.CUSTOMER),
            address     = map["address"] as? String,
            createdAt   = map["createdAt"] as? Timestamp ?: Timestamp.now(),
            isActive    = map["isActive"] as? Boolean ?: true
        )
    }
}