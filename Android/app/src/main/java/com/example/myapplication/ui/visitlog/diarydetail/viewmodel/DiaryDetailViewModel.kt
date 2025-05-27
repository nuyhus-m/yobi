package com.example.myapplication.ui.visitlog.diarydetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.repository.DailyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val repository: DailyRepository
) : ViewModel() {

    private val _dailyLog = MutableLiveData<DailyLogResponse>()
    val dailyLog: LiveData<DailyLogResponse> = _dailyLog

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _deleted = MutableLiveData<Boolean>()
    val deleted: LiveData<Boolean> = _deleted

    fun loadDailyLog(scheduleId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getDailyLogs(scheduleId)
                if (response.isSuccessful) {
                    _dailyLog.value = response.body()
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"

            }
        }
    }

    fun deleteDailyLog(scheduleId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.patchDailyLogsDelete(scheduleId)
                if (response.isSuccessful) {
                    _deleted.value = true
                } else {
                    _error.value = "삭제 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }
}