package com.example.myapplication.ui.schedule.manualschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
import com.example.myapplication.data.dto.response.schedule.ScheduleResponse
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ManualScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel(){

    fun registerSchedule(request: ScheduleRequest, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.registerSchedule(request)
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

    fun editSchedule(scheduleId: Int, request: ScheduleRequest, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                scheduleRepository.editSchedule(scheduleId, request)
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
}