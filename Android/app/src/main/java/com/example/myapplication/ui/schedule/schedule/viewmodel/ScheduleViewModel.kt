package com.example.myapplication.ui.schedule.schedule.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.model.ScheduleItemModel
import com.example.myapplication.data.repository.ScheduleRepository
import com.example.myapplication.util.TimeUtils.toEpochMillis
import com.example.myapplication.util.TimeUtils.toLocalDate
import com.example.myapplication.util.TimeUtils.toTimeText
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    private val _scheduleList = MutableLiveData<List<ScheduleItemModel>>()
    val scheduleList: LiveData<List<ScheduleItemModel>> = _scheduleList

    private val _dotMap = MutableLiveData<Map<LocalDate, List<Int>>>()
    val dotMap: LiveData<Map<LocalDate, List<Int>>> = _dotMap

    // 캐싱하는 데이터
    private val loadedRanges = mutableListOf<Pair<LocalDate, LocalDate>>()

    // 클라이언트 아이디별 도트 색 맵핑
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
        selectDate(LocalDate.now())
    }

    fun getPeriodSchedule(start: Long, end: Long) {
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        if (loadedRanges.any { it.contains(startDate, endDate) }) return

        viewModelScope.launch {
            kotlin.runCatching {
                scheduleRepository.getPeriodSchedule(start, end)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    val mapped = body.groupBy { it.visitedDate.toLocalDate() }
                        .mapValues { entry -> entry.value.map { it.clientId } }

                    val currentMap = _dotMap.value.orEmpty().toMutableMap()
                    currentMap.putAll(mapped)
                    _dotMap.value = currentMap

                    loadedRanges.add(startDate to endDate)
                }

            }.onFailure {
                Log.d("getPeriodSchedule", "${it}")
            }
        }
    }

    fun getDaySchedule(date: Long) {
        viewModelScope.launch {
            kotlin.runCatching {
                scheduleRepository.getDaySchedule(date)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val list = response.body()?.map {
                        ScheduleItemModel(
                            it.scheduleId,
                            it.clientId,
                            it.clientName,
                            it.visitedDate,
                            it.visitedDate.toLocalDate().toString(),
                            "${it.startAt.toTimeText()} ~ ${it.endAt.toTimeText()}",
                            it.hasLogContent
                        )
                    } ?: emptyList()
                    _scheduleList.value = list
                }
            }.onFailure {
                Log.d("getDaySchedule", "${it}")
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        getDaySchedule(date.toEpochMillis())
    }

    private fun Pair<LocalDate, LocalDate>.contains(start: LocalDate, end: LocalDate): Boolean {
        return this.first <= start && this.second >= end
    }

    fun updateDotMap(date: LocalDate, clientIds: List<Int>) {
        val currentMap = _dotMap.value.orEmpty().toMutableMap()
        if (clientIds.isEmpty()) {
            currentMap.remove(date) // 일정이 없어지면 도트 제거
        } else {
            currentMap[date] = clientIds // 일정이 있으면 도트 추가/갱신
        }
        _dotMap.value = currentMap
    }

    fun refreshDotMapForRange(startDate: LocalDate, endDate: LocalDate, newData: Map<LocalDate, List<Int>>) {
        val currentMap = _dotMap.value.orEmpty().toMutableMap()

        // 해당 범위의 기존 데이터 제거
        currentMap.keys.toList().forEach { date ->
            if (date in startDate..endDate) {
                currentMap.remove(date)
            }
        }

        // 새 데이터 추가
        currentMap.putAll(newData)
        _dotMap.value = currentMap
    }

}

