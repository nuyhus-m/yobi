package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.request.mypage.ChangePasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH

interface MyPageService {
    @PATCH("users/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>
}