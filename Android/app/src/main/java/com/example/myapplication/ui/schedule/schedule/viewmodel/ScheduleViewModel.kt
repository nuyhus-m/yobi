package com.example.myapplication.ui.schedule.schedule.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.time.LocalDate

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _scheduleList = MutableLiveData<List<ScheduleItem>>()
    val scheduleList: LiveData<List<ScheduleItem>> = _scheduleList

    val clientColorMap = mapOf(
        1 to "#00B383",
        2 to "#735BF2",
        3 to "#0095FF",
        4 to "#EA9A86",
        5 to "#FF5E5E",
        6 to "#FF6AD5",
        7 to "#946FCF",
        8 to "#39FF14",
        9 to "#D2691E",
        10 to "#FFF200"
    )

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _scheduleList.value = listOf(
            ScheduleItem(101, 1, "박진현", "2025-05-04", "10:00:00", "10:50:00"),
            ScheduleItem(102, 2, "민수현", "2025-05-04", "11:00:00", "11:30:00"),
            ScheduleItem(103, 3, "이서현", "2025-05-04", "12:00:00", "13:30:00"),
            ScheduleItem(104, 4, "이호정", "2025-05-04", "13:00:00", "14:30:00"),
            ScheduleItem(105, 5, "이문경", "2025-05-04", "14:00:00", "15:30:00"),
            ScheduleItem(106, 6, "차현우", "2025-05-04", "15:00:00", "16:30:00")
        )
    }

    private val dummySchedules = listOf(
        ScheduleDotItem(101, 1, LocalDate.of(2025, 5, 2), "10:00", "10:50"),
        ScheduleDotItem(102, 2, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 3, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 4, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 5, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 6, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 7, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 8, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 9, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(102, 10, LocalDate.of(2025, 5, 2), "11:00", "11:30"),
        ScheduleDotItem(101, 1, LocalDate.of(2025, 5, 1), "10:00", "10:50"),
        ScheduleDotItem(102, 2, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 3, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 4, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 5, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 6, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 7, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 8, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 9, LocalDate.of(2025, 5, 1), "11:00", "11:30"),
        ScheduleDotItem(102, 10, LocalDate.of(2025, 5, 1), "11:00", "11:30")
    )

    // 날짜 별 도트 정보 맵핑
    val dotMap: Map<LocalDate, List<Int>> = dummySchedules
        .groupBy { it.visitedDate }
        .mapValues { entry -> entry.value.map { it.clientId } }


}

data class ScheduleItem(
    val scheduleId: Int,
    val clientId: Int,
    val clientName: String,
    val visitedDate: String,
    val startAt: String,
    val endAt: String
)

data class ScheduleDotItem(
    val scheduleId: Int,
    val clientId: Int,
    val visitedDate: LocalDate,
    val startAt: String,
    val endAt: String
)

