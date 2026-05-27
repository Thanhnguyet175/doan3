package com.example.milkteaapp.model.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.utils.ObjectUtils

class CloudinarySource {

    /** Hàm upload ảnh lên Cloudinary nhận vào đường dẫn Path dạng String */
    suspend fun uploadImage(filePath: String): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(filePath)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    if (continuation.isActive) continuation.resume(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    if (continuation.isActive) continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    /** * SỬA LỖI: Thêm hàm xoá ảnh từ URL Cloudinary
     * Hàm này bóc tách publicId từ link (Ví dụ: từ .../upload/v123456/image_name.jpg thành image_name)
     */
    suspend fun deleteImageByUrl(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val publicId = imageUrl.substringAfterLast("/").substringBeforeLast(".")
            if (publicId.isNotBlank()) {
                // Gọi API hủy/xóa tài nguyên của Cloudinary
                MediaManager.get().cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}