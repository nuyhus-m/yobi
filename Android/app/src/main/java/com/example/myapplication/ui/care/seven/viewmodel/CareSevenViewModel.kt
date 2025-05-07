package com.example.myapplication.ui.care.seven.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.care.seven.data.DailyMetric
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CareSevenViewModel @Inject constructor() : ViewModel(){

    private val _metrics = MutableLiveData<List<DailyMetric>>()
    val metrics : LiveData<List<DailyMetric>> = _metrics

    init {
        val dateLabels = listOf("4/24", "4/25", "4/26", "4/27", "4/28", "4/29", "4/30")
        fun dummy(name: String) = DailyMetric(
            title = name,
            dates = dateLabels,
            values = List(dateLabels.size) { (50..100).random().toFloat() }
        )

        _metrics.value = listOf(
            dummy("체지방률"), dummy("기초대사량"), dummy("체내 수분"),
            dummy("단백질량"), dummy("수축기 혈압"), dummy("이완기 혈압"),
            dummy("스트레스 지수")
        )
    }
}