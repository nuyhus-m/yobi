package com.example.myapplication.ui.mypage.changePassword.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.request.mypage.ChangePasswordRequest
import com.example.myapplication.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository
): ViewModel() {
    private val _changePasswordSuccess = MutableLiveData<Boolean>()
    val changePasswordSuccess: LiveData<Boolean> = _changePasswordSuccess

    fun changePassword(request: ChangePasswordRequest) {
        viewModelScope.launch {
            runCatching {
                myPageRepository.changePassword(request)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    _changePasswordSuccess.value = true
                } else {
                    _changePasswordSuccess.value = false
                    Log.d("changePassword", "${response}")
                }
            }.onFailure {
                _changePasswordSuccess.value = false
                Log.d("changePassword", "${it}")
                }
            }
    }

}