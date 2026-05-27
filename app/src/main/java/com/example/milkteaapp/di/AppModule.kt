package com.example.milkteaapp.di

import com.example.milkteaapp.model.source.CloudinarySource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // ĐẢM BẢO ĐÃ IMPORT DÒNG NÀY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. Cung cấp FirebaseAuth (Dành cho việc đăng nhập/đăng ký)
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // 2. SỬA LỖI TẠI ĐÂY: Cung cấp FirebaseFirestore để các Repository chạy được FirestoreSource
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // 3. Cung cấp CloudinarySource chúng ta vừa tạo ở các bước trước
    @Provides
    @Singleton
    fun provideCloudinarySource(): CloudinarySource {
        return CloudinarySource()
    }
}