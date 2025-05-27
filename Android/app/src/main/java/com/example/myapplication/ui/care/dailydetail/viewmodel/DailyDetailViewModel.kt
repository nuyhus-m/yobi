package com.example.myapplication.ui.care.dailydetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.TodayDetailResponse
import com.example.myapplication.data.repository.CareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyDetailViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {
    private val _todayDetailData = MutableLiveData<TodayDetailResponse>()
    val todayDetailData: LiveData<TodayDetailResponse> = _todayDetailData

    fun fetchTodayDetailData(clientId: Int) {
        viewModelScope.launch {
            val response = careRepository.getTodayDetailData(clientId)
            if (response.isSuccessful) {
                _todayDetailData.value = response.body()

            } else {

            }
        }
    }

}
