package com.example.myapplication.ui.visitlog.visitwrite.stt

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.IOException

/**
 * Google Cloud API 인증을 위한 헬퍼 클래스
 */
object CredentialsHelper {
    private const val TAG = "CredentialsHelper"
    private const val CREDENTIALS_FILE = "service_stt.json"

    /**
     * assets 폴더에서 Google Cloud 서비스 계정 자격증명을 로드
     */
    fun fromAssets(context: Context): GoogleCredentials {
        return try {
            context.assets.open(CREDENTIALS_FILE).use { inputStream ->
                GoogleCredentials.fromStream(inputStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load credentials from assets", e)
            throw IllegalStateException("Google Cloud 자격증명을 로드할 수 없습니다. 파일이 assets에 존재하는지 확인하세요.", e)
        }
    }

    fun checkCredentialsExist(context: Context): Boolean {
        return try {
            context.assets.open(CREDENTIALS_FILE).use { true }
        } catch (e: IOException) {
            Log.e(TAG, "Credentials file not found in assets", e)
            false
        }
    }
}