package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.GET

interface UserService {

    @GET("users/")
    suspend fun getMyInfo(): Response<UserResponse>

//    // 사용자 정보를 추가한다.
//    @POST("rest/user")
//    suspend fun insert(@Body body: User): Boolean

}