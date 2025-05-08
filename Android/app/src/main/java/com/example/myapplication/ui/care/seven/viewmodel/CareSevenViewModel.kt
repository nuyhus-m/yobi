package com.example.myapplication.ui.care.seven.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.care.seven.data.DailyMetric
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CareSevenViewModel @Inject constructor() : ViewModel() {

    private val _metrics = MutableLiveData<List<DailyMetric>>()
    val metrics: LiveData<List<DailyMetric>> = _metrics

    init {
        val dateLabels = listOf(
            "4/1", "4/2", "4/3", "4/4", "4/5", "4/6", "4/7",
            "4/8", "4/9", "4/10", "4/11", "4/12", "4/13", "4/14",
            "4/15", "4/16", "4/17", "4/18", "4/19", "4/20", "4/21",
            "4/22", "4/23", "4/24", "4/25", "4/26", "4/27", "4/28", "4/29", "4/30"
        )

        // 그래프 흐름도 표현하기 위해서 만든 함수
        fun generateDateWithTrend(size: Int, baseValue: Float, maxVariation: Float): List<Float> {
            val result = mutableListOf<Float>()
            var currentValue = baseValue

            for (i in 0 until size) {
                val trend = (-10..10).random().toFloat() / 10f

                // 변동값 (-maxVariation ~ maxVariation 사이의 값)
                val variation =
                    (-maxVariation.toInt()..maxVariation.toInt()).random().toFloat() / 10f

                currentValue += trend + variation
                currentValue = currentValue.coerceIn(
                    baseValue - maxVariation * 3,
                    baseValue + maxVariation * 3
                )
                result.add(currentValue)
            }
            return result
        }
        // 하드코딩용
        fun createMetric(name: String, baseValue: Float): DailyMetric {
            return DailyMetric(
                title = name,
                dates = dateLabels,
                values = generateDateWithTrend(dateLabels.size, baseValue, 10f)
            )
        }

        _metrics.value = listOf(
            createMetric("체지방률", 25f),
            createMetric("기초대사량", 1500f),
            createMetric("체내 수분", 60f),
            createMetric("단백질량", 70f),
            createMetric("수축기 혈압", 120f),
            createMetric("이완기 혈압", 80f),
            createMetric("스트레스 지수", 50f)
        )
    }
}