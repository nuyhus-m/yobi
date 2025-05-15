package com.example.myapplication.ui.schedule.manualschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
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

}