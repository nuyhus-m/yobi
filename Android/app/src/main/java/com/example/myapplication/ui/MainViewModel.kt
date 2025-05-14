package com.example.myapplication.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.ClientResponse
import com.example.myapplication.data.dto.response.UserResponse
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _clientList = MutableLiveData<List<ClientResponse>>()
    val clientList : LiveData<List<ClientResponse>> = _clientList

    private val _userInfo = MutableLiveData<UserResponse>()
    val userInfo: LiveData<UserResponse> = _userInfo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchClients(){
        viewModelScope.launch {
            try{
                val response = clientRepository.getClientList()
                if (response.isSuccessful){
                    _clientList.value = response.body()
                }else{
                    _error.value = "서버 오류: ${response.code()}"
                }
            }catch (e:Exception){
                _error.value = "네트워크 오류 : ${e.message}"
            }
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            runCatching {
                userRepository.getMyInfo()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _userInfo.value = it
                    }
                } else {
                    Log.d("MainViewModel", "loadUserInfo: ${response}")
                }
            }.onFailure { e ->
                Log.d("MainViewModel", "loadUserInfo: ${e.message}")
            }

        }
    }
}