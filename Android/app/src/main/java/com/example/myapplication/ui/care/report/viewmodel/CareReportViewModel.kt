package com.example.myapplication.ui.care.report.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.care.report.data.ReportDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CareReportViewModel @Inject constructor() : ViewModel() {

    private val _dates = MutableLiveData<List<ReportDate>>()
    val dates: MutableLiveData<List<ReportDate>> = _dates

    init {
        _dates.value = listOf(
            ReportDate("2025/04/22 - 2025/04/28"),
            ReportDate("2025/04/15 - 2025/04/21"),
            ReportDate("2025/04/08 - 2025/04/14")
        )
    }
}