package com.example.myapplication.ui.schedule.schedule.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.model.ScheduleItemModel
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
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
    private val colorPool = listOf(
        "#00B383", "#735BF2", "#0095FF", "#EA9A86", "#FF5E5E",
        "#FF6AD5", "#946FCF", "#39FF14", "#D2691E", "#FFF200"
    )

    var clientColorMap: Map<Int, String> = emptyMap()
        private set

    fun setClientColors(clients: List<ClientDetailResponse>) {
        val map = mutableMapOf<Int, String>()
        clients.forEachIndexed { index, client ->
            map[client.clientId] = colorPool[index % colorPool.size]
        }
        clientColorMap = map
    }

    fun getPeriodSchedule(start: Long, end: Long) {
        val startDate = start.toLocalDate()
        val endDate = end.toLocalDate()

        viewModelScope.launch {
            kotlin.runCatching {
                scheduleRepository.getPeriodSchedule(start, end)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()

                    // 1. 서버 응답을 날짜별로 clientId 리스트로 변환
                    val mapped = body.groupBy { it.visitedDate.toLocalDate() }
                        .mapValues { entry -> entry.value.map { it.clientId } }

                    // 2. 기존 dotMap을 복사
                    val currentMap = _dotMap.value.orEmpty().toMutableMap()

                    // 3. 이번 범위의 날짜들을 모두 제거 (삭제된 도트 반영을 위함)
                    val updatedDates = (startDate..endDate).toList()
                    updatedDates.forEach { currentMap.remove(it) }

                    // 4. 새 데이터로 덮어쓰기
                    currentMap.putAll(mapped)

                    // 5. 최종 dotMap 반영
                    _dotMap.value = currentMap
//                    val body = response.body() ?: emptyList()
//                    val mapped = body.groupBy { it.visitedDate.toLocalDate() }
//                        .mapValues { entry -> entry.value.map { it.clientId } }
//
//                    val currentMap = _dotMap.value.orEmpty().toMutableMap()
//                    currentMap.putAll(mapped)
//                    _dotMap.value = currentMap

                    loadedRanges.add(startDate to endDate)
                }
                Log.d("getPeriodSchedule", "${response.body()}")

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

    fun reloadCurrentDate() {
        selectedDate.value?.let { date ->
            getDaySchedule(date.toEpochMillis())
        }
    }

}

operator fun LocalDate.rangeTo(other: LocalDate): Iterable<LocalDate> =
    object : Iterable<LocalDate> {
        override fun iterator(): Iterator<LocalDate> =
            generateSequence(this@rangeTo) { date ->
                if (date < other) date.plusDays(1) else null
            }.takeWhile { it <= other }.iterator()
    }
