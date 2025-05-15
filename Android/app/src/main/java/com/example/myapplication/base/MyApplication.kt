package com.example.myapplication.base

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 다크모드 비활성화
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    companion object {
        const val BASE_URL = "http://k12s209.p.ssafy.io:8081/api/"
//        const val BASE_URL = "http://192.168.0.52:8080/api/"
    }
}