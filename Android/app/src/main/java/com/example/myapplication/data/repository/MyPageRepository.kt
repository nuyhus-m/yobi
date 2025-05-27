package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.mypage.ChangePasswordRequest
import com.example.myapplication.data.remote.MyPageService
import retrofit2.Response
import javax.inject.Inject

class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService
) {
    suspend fun changePassword(body: ChangePasswordRequest): Response<Unit> {
        return myPageService.changePassword(body)
    }
}