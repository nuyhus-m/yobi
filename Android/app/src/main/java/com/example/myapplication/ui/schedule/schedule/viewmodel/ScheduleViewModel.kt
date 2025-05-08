package com.example.myapplication.ui.schedule.schedule.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.time.LocalDate

@HiltViewModel
class ScheduleViewModel @Inject constructor(
//    private val userRepository: UserRepository
) : ViewModel() {

    private val _scheduleList = MutableLiveData<List<ScheduleItem>>()
    val scheduleList: LiveData<List<ScheduleItem>> = _scheduleList

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _scheduleList.value = listOf(
            ScheduleItem(101, 1, "박진현", "2025-05-04", "10:00:00", "10:50:00"),
            ScheduleItem(102, 2, "민수현", "2025-05-04", "11:00:00", "11:30:00")
        )
    }


}

data class ScheduleItem(
    val scheduleId: Int,
    val clientId: Int,
    val clientName: String,
    val visitedDate: String,
    val startAt: String,
    val endAt: String
)
