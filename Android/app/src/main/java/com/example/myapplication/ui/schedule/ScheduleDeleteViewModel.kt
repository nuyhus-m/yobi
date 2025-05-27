package com.example.myapplication.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleDeleteViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

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