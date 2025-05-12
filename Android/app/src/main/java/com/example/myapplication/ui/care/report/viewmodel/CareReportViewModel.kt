package com.example.myapplication.ui.care.report.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.ReportDto
import com.example.myapplication.data.repository.CareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareReportViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {

    private val _reports = MutableLiveData<List<ReportDto>>()
    val reports: LiveData<List<ReportDto>> = _reports

    fun fetchReports(clientId: Int) {
        viewModelScope.launch {
            val response = careRepository.getWeeklyReportList(clientId)
            if (response.isSuccessful) {
                _reports.value = response.body()?.data ?: emptyList()
            } else {
                // 에러 처리 로직 필요 시 여기에 추가
            }
        }
    }
}
