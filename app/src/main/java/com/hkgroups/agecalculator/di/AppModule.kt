package com.hkgroups.agecalculator.di

import android.content.Context
import com.hkgroups.agecalculator.data.local.ZodiacDao
import com.hkgroups.agecalculator.data.local.ZodiacDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideZodiacDatabase(@ApplicationContext context: Context): ZodiacDatabase =
        ZodiacDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideZodiacDao(database: ZodiacDatabase): ZodiacDao = database.zodiacDao()
}
