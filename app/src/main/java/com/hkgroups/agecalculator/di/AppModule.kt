package com.hkgroups.agecalculator.di

import android.content.Context
import com.hkgroups.agecalculator.data.local.ZodiacDao
import com.hkgroups.agecalculator.data.local.ZodiacDatabase
import com.hkgroups.agecalculator.data.remote.MockApiInterceptor
import com.hkgroups.agecalculator.data.remote.ZodiacApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- OkHttpClient with Mock Interceptor ---
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(MockApiInterceptor()) // Add mock interceptor to simulate real API
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // --- Retrofit Instance with OkHttpClient ---
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.zodiac.com/") // The base URL (intercepted by MockApiInterceptor)
            .client(okHttpClient) // Use OkHttpClient with mock interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- Zodiac API Service ---
    @Provides
    @Singleton
    fun provideZodiacApiService(retrofit: Retrofit): ZodiacApiService {
        return retrofit.create(ZodiacApiService::class.java)
    }

    // --- Room Database (with automatic seeding via callback) ---
    @Provides
    @Singleton
    fun provideZodiacDatabase(@ApplicationContext context: Context): ZodiacDatabase {
        return ZodiacDatabase.getInstance(context)
    }

    // --- Zodiac DAO ---
    @Provides
    @Singleton
    fun provideZodiacDao(database: ZodiacDatabase): ZodiacDao {
        return database.zodiacDao()
    }
}