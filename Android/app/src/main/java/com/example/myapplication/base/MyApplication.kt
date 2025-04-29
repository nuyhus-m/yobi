package com.example.myapplication.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        const val BASE_URL = "https://api.example.com/"
    }
}