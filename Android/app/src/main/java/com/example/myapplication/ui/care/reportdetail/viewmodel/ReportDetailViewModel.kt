package com.example.myapplication.ui.care.reportdetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.ReportDetailDto
import com.example.myapplication.data.repository.CareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {

    private val _report = MutableLiveData<ReportDetailDto>()
    val report: LiveData<ReportDetailDto> = _report

    fun fetchReportDetail(reportId: Int) {
        viewModelScope.launch {
            val response = careRepository.getReportDetail(reportId)
            if (response.isSuccessful) {
                val result = response.body()
                result?.let { _report.value = it }
            }
        }
    }
}
