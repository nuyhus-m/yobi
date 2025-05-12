package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.ClientResponse
import com.example.myapplication.data.remote.ClientService
import retrofit2.Response
import javax.inject.Inject

class ClientRepository @Inject constructor(
    private val clientService: ClientService
) {

    suspend fun getClientList(): Response<List<ClientResponse>> {
        return clientService.getClientList()
    }
}