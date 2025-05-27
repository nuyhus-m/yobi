package com.example.myapplication.ui.care.report.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.ReportDto
import com.example.myapplication.data.repository.CareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CareReportViewModel"

@HiltViewModel
class CareReportViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {

    private val _reports = MutableLiveData<List<ReportDto>>()
    val reports: LiveData<List<ReportDto>> = _reports

    fun fetchReports(clientId: Int) {
        viewModelScope.launch {
            val response = careRepository.getWeeklyReportList(clientId)
            Log.d("CareReportVM", "response raw: ${response.raw()}")
            Log.d("CareReportVM", "response body: ${response.body()}")

            if (response.isSuccessful) {
                val reportList = response.body()?.reports
                Log.d("CareReportVM", "reports: $reportList")
                _reports.value = reportList ?: emptyList()
            } else {
                // 에러 처리 로직 필요 시 여기에 추가
            }
        }
    }
}
