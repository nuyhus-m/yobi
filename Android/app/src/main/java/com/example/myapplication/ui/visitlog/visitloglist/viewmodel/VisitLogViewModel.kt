package com.example.myapplication.ui.visitlog.visitloglist.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.visitlog.DailyHumanDTO
import com.example.myapplication.data.repository.DailyRepository
import com.example.myapplication.ui.visitlog.visitloglist.viewmodel.data.FilterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VisitLogViewModel"
@HiltViewModel
class VisitLogViewModel @Inject constructor(
    private val repository: DailyRepository
) : ViewModel() {


    // 위에 이름 선택하는 부분
    private val _filterItems = MutableLiveData<List<FilterItem>>()
    val filterItems: LiveData<List<FilterItem>> = _filterItems


    // API로부터 가져온 모든 방문 기록들
    private val _allLogs = MutableLiveData<List<DailyHumanDTO>>()

    // 방문 기록들
    private val _filteredLogs = MutableLiveData<List<DailyHumanDTO>>()
    val filteredLogs: LiveData<List<DailyHumanDTO>> = _filteredLogs

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchDailyHumanList()
    }

    private fun fetchDailyHumanList() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = repository.getDailyHumanList()
                if (response.isSuccessful) {
                    val dailyHumans = response.body() ?: emptyList()
                    _allLogs.value = dailyHumans
                    Log.d(TAG, "fetchDailyHumanList: $dailyHumans")
                    // 필터
                    val names = dailyHumans.map { it.clientName }.distinct()
                    val filterItems = mutableListOf(FilterItem("전체", true))
                    filterItems.addAll(names.map { FilterItem(it, false) })
                    _filterItems.value = filterItems

                    filterLogs("전체")
                } else {
                    Log.d(TAG, "fetchDailyHumanList: ")
                    _errorMessage.value = "데이터 못불러왔어요 : ${response.message()}"
                }
            } catch (e: Exception) {
                Log.d(TAG, "fetchDailyHumanList: ${e.message}")
                _errorMessage.value = "데이터 못불러왔어요 catch : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // 필터 선택 시 호출되는 함수
    fun selectFilter(selectedName: String) {
        _filterItems.value = _filterItems.value?.map {
            it.copy(isSelected = it.name == selectedName)
        }
        filterLogs(selectedName)
    }

    private fun filterLogs(name: String) {
        val allLogs = _allLogs.value ?: emptyList()
        _filteredLogs.value =
            if (name == "전체") {
                allLogs
            } else {
                allLogs.filter { it.clientName == name }
            }
    }

    // 새로고침 기능
    fun refresh() {
        fetchDailyHumanList()
    }
}