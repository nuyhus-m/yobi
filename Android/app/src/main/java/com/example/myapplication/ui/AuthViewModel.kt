package com.example.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.SharedPreferencesUtil
import com.example.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sharedPreferencesUtil: SharedPreferencesUtil

) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun login(employeeNumber: Int, password: String) {
        viewModelScope.launch {
            runCatching {
                authRepository.login(employeeNumber, password)
            }.onSuccess { response ->
                val body = response.body()
                val isValid = response.isSuccessful && body != null

                if (isValid) {
                    body?.let {
                        sharedPreferencesUtil.saveTokens(it.accessToken, it.refreshToken)
                        _loginSuccess.value = true
                    }
                } else {
                    Log.d("login", "${response}")
                    _loginSuccess.value = false
                }

            }.onFailure {
                _loginSuccess.value = false
            }
        }
    }
}