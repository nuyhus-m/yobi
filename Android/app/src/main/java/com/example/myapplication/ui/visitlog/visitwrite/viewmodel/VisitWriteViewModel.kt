package com.example.myapplication.ui.visitlog.visitwrite.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.DailyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "VisitWriteViewModel"

@HiltViewModel
class VisitWriteViewModel @Inject constructor(
    private val dailyRepository: DailyRepository
) : ViewModel() {

    fun saveDailyLog(
        scheduleId: Int,
        content: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val response = dailyRepository.updateDailyLog(scheduleId, content)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    Log.d(TAG, "saveDailyLog: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "saveDailyLog: ${e.message}")
            }
        }
    }

    fun loadDailyLog(
        scheduleId: Int,
        onSuccess: (clientName: String, visitedDate: Long, logContent: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = dailyRepository.getDailyLogs(scheduleId)
                if (res.isSuccessful) {
                    res.body()?.let {
                        Log.d(TAG, "loadDailyLog: ${it}")
                        onSuccess(it.clientName, it.visitedDate, it.logContent)
                    }
                } else Log.d(TAG, "loadDailyLog: ${res.code()}")
            } catch (e: Exception) {
                Log.d(TAG, "loadDailyLog: ${e.message}")
            }
        }
    }

}