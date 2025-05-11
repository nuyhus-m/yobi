package com.example.myapplication.di

import com.example.myapplication.data.remote.MeasureService
import com.example.myapplication.data.remote.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideMeasureService(retrofit: Retrofit): MeasureService {
        return retrofit.create(MeasureService::class.java)
    }

}