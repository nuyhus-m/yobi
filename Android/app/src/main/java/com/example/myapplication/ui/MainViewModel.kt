package com.example.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.ClientResponse
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
}