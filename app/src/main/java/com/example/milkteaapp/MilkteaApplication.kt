package com.example.milkteaapp

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkteaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Điền chính xác thông tin từ Dashboard của bạn vào đây
        val config = mapOf(
            "cloud_name" to "dp44trruy",
            "api_key"    to "543936814248327",
            "api_secret" to "Vrj2YAaOrX8So8_uMlnO0TjFtfE"
        )

        MediaManager.init(this, config)
    }
}