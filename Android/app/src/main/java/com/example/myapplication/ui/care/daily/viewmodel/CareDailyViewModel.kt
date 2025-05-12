package com.example.myapplication.ui.care.daily.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.TodayResponse
import com.example.myapplication.data.repository.CareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareDailyViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {

    private val _todayData = MutableLiveData<TodayResponse>()
    val todayData: LiveData<TodayResponse> = _todayData

    fun fetchTodayData(clientId: Int) {
        viewModelScope.launch {
            val response = careRepository.getTodayData(clientId)
            if (response.isSuccessful) {
                _todayData.value = response.body()
            } else {

            }
        }
    }
}