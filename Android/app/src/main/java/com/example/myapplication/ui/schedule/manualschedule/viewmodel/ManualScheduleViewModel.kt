package com.example.myapplication.ui.schedule.manualschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
import com.example.myapplication.data.dto.response.schedule.ScheduleResponse
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import org.json.JSONObject

@HiltViewModel
class ManualScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel(){

    fun registerSchedule(request: ScheduleRequest, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.registerSchedule(request)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorCode = extractErrorCode(response)
                    onError(errorCode)
                }
            }.onFailure {
                onError(null)
            }

        }
    }

    fun editSchedule(scheduleId: Int, request: ScheduleRequest, onSuccess: () -> Unit, onError: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.editSchedule(scheduleId, request)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorCode = extractErrorCode(response)
                    onError(errorCode)
                }
            }.onFailure {
                onError(null)
            }
        }
    }

    fun getSchedule(scheduleId: Int, onSuccess: (ScheduleResponse) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.getSchedule(scheduleId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onError()
                } else {
                    onError()
                }
            }.onFailure {
                onError()
            }
        }
    }

    fun deleteSchedule(scheduleId: Int, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.deleteSchedule(scheduleId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    onSuccess()
                    } else {
                    onError()
                }
            }.onFailure {
                onError()
            }
        }
    }

    private fun extractErrorCode(response: retrofit2.Response<*>): String? {
        val errorBody = response.errorBody()?.string()
        val json = JSONObject(errorBody ?: return null)
        return json.getString("code")
    }
}