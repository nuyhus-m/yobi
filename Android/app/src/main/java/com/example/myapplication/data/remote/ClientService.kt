package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.ClientResponse
import retrofit2.Response
import retrofit2.http.GET

interface ClientService {

    @GET("clients/list")
    suspend fun getClientList() : Response<List<ClientResponse>>
}