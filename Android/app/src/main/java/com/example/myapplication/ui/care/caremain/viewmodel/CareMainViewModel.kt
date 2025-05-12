package com.example.myapplication.ui.care.caremain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.data.repository.CareRepository
import com.example.myapplication.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareMainViewModel @Inject constructor(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _clientDetail = MutableLiveData<ClientDetailResponse>()
    val clientDetail: LiveData<ClientDetailResponse> = _clientDetail

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchClientDetail(clientId: Int) {
        viewModelScope.launch {
            try {
                val response = clientRepository.getClientDetail(clientId)
                if (response.isSuccessful) {
                    _clientDetail.value = response.body()
                } else {
                    _error.value = "서버 오류 : ${response.body()}"
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류 :${e.message}"
            }
        }
    }
}