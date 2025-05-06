package com.example.myapplication.ui.visitlog.visitloglist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VisitLogViewModel @Inject constructor() : ViewModel() {


    // 위에 이름 선택하는 부분
    private val _filterItems = MutableLiveData<List<FilterItem>>()
    val filterItems: LiveData<List<FilterItem>> = _filterItems
    
    
    // 방문 기록들
    private val _allLogs = listOf(
        VisitLog("박진현", "2025/04/28"),
        VisitLog("민수현", "2025/04/28"),
        VisitLog("민수현", "2025/04/27"),
        VisitLog("박진현", "2025/04/27"),
    )

    private val _filteredLogs = MutableLiveData<List<VisitLog>>()
    val filteredLogs: LiveData<List<VisitLog>> = _filteredLogs

    init {
        val initialFilters = listOf("전체", "박진현", "민수현", "차현우", "이호정")
            .mapIndexed { index, name -> FilterItem(name, index == 0) }
        _filterItems.value = initialFilters
        filterLogs("전체")
    }

    fun selectFilter(selectedName: String) {
        _filterItems.value = _filterItems.value?.map {
            it.copy(isSelected = it.name == selectedName)
        }
        filterLogs(selectedName)
    }

    fun filterLogs(name: String) {
        _filteredLogs.value =
            if (name == "전체") {
                _allLogs
            } else {
                _allLogs.filter { it.name == name }
            }
    }
}

data class VisitLog(
    val name: String,
    val date: String
)

data class FilterItem(
    val name: String,
    val isSelected: Boolean = false
)
