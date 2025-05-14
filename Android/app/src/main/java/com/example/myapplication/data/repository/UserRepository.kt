package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.UserResponse
import com.example.myapplication.data.remote.UserService
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userService: UserService
) {
    suspend fun getMyInfo(): Response<UserResponse> {
        return userService.getMyInfo()
    }
}