package com.example.myapplication.ui.care.seven.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.care.HealthResponse
import com.example.myapplication.data.dto.response.care.MetricData
import com.example.myapplication.data.repository.CareRepository
import com.example.myapplication.ui.care.seven.data.DailyMetric
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CareSevenViewModel"

@HiltViewModel
class CareSevenViewModel @Inject constructor(
    private val careRepository: CareRepository
) : ViewModel() {

    private val _metrics = MutableLiveData<List<DailyMetric>>()
    val metrics: LiveData<List<DailyMetric>> = _metrics

    fun fetchMetrics(clientId: Int, userId: Int, size: Int, cursorDate: String? = null) {
        viewModelScope.launch {
            try {
                val response = careRepository.getTotalHealth(clientId, userId, size, cursorDate)
                if (response.isSuccessful){
                    val body = response.body()
                    if (body != null){
                        _metrics.value = convertToDailyMetrics(body)
                    }
                    Log.d(TAG, "fetchMetrics: ${response.body()}")
                }else{
                    // 실패처리
                    Log.d(TAG, "fetchMetrics: 실패")
                }
            } catch (e: Exception) {
                // 예외 처리
                Log.e(TAG, "fetchMetrics: ${e.message}", e)
            }
        }
    }

    private fun convertToDailyMetrics(response: HealthResponse): List<DailyMetric> {
        fun convert(title: String, data: List<MetricData>): DailyMetric {
            val formatter = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
            val dates = data.map {
                formatter.format(java.util.Date(it.date))
            }
            val values = data.map { it.value }
            return DailyMetric(title, dates, values)
        }
        return listOf(
            convert("체지방률", response.bodyComposition.bfp),
            convert("기초대사량", response.bodyComposition.bmr),
            convert("체내 수분", response.bodyComposition.ecf),
            convert("단백질량", response.bodyComposition.protein),
            convert("수축기 혈압", response.bloodPressure.sbp),
            convert("이완기 혈압", response.bloodPressure.dbp),
            convert("스트레스 지수", response.stress.stressValue)
        )

    }

}