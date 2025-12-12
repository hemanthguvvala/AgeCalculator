package com.hkgroups.agecalculator.di

import com.hkgroups.agecalculator.data.remote.ZodiacApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    // --- NEW: Provide the Retrofit instance ---
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.zodiac.com/") // The base URL for our imaginary API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- NEW: Provide the ZodiacApiService ---
    @Provides
    @Singleton
    fun provideZodiacApiService(retrofit: Retrofit): ZodiacApiService {
        return retrofit.create(ZodiacApiService::class.java)
    }
}