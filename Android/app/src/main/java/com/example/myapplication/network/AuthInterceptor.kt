package com.example.myapplication.network

import com.example.myapplication.data.local.SharedPreferencesUtil
import jakarta.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val sharedPreferencesUtil: SharedPreferencesUtil
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val accessToken = sharedPreferencesUtil.getAccessToken()

        if (request.url.encodedPath.contains("/user/login")) {
            return chain.proceed(request)
        }

        return if (accessToken != null) {
            val newRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(request)
        }
    }


}