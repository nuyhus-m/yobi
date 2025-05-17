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
import com.example.myapplication.util.TimeUtils.toEpochMillisFromMMDD
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

    private var currentList = mutableListOf<DailyMetric>()
    private var isLoading = false
    private var currentClientId = -1          // ← 이후 loadMore()에서 사용
    private val pageSize = 30                 // 고정

    /** 최초 호출·추가 호출 모두 여기서 처리 */
    fun fetchMetrics(
        clientId: Int,
        cursorDate: Long? = null      // null → 최신부터
    ) {
        if (isLoading) return
        isLoading = true
        currentClientId = clientId
        Log.d(TAG, "fetchMetrics: $currentClientId")

        viewModelScope.launch {
            try {
                val res = careRepository.getTotalHealth(clientId, pageSize, cursorDate)
                if (res.isSuccessful) {
                    res.body()?.let { body ->
                        val newMetrics = convertToDailyMetrics(body)

                        currentList = if (cursorDate == null) {
                            // 초기 로드
                            newMetrics.toMutableList()
                        } else {
                            // 과거 데이터 prepend
                            currentList.zip(newMetrics).map { (old, new) ->
                                old.copy(
                                    dates = new.dates + old.dates,
                                    values = new.values + old.values
                                )
                            }.toMutableList()
                        }
                        _metrics.postValue(currentList)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchMetrics error", e)
            } finally {
                isLoading = false
            }
        }
    }

    /** 차트가 왼쪽 끝에 닿을 때 호출 */
    fun loadMore() {
        val currentCnt = currentList.firstOrNull()?.dates?.size ?: 0
        if (currentCnt >= pageSize) return

        // 현재 리스트에서 가장 오래된 날짜 추출 (MM/dd 문자열)
        val oldestDateStr = currentList.firstOrNull()?.dates?.firstOrNull() ?: return
        // util 함수로 epoch millis 변환
        val oldestEpoch = oldestDateStr.toEpochMillisFromMMDD()   // ← Long
        fetchMetrics(currentClientId, cursorDate = oldestEpoch)
    }

    /* ---------- 변환 로직 그대로 ---------- */
    private fun convertToDailyMetrics(r: HealthResponse): List<DailyMetric> {
        fun cv(title: String, data: List<MetricData>): DailyMetric {
            val fmt = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
            val dates = data.map { fmt.format(java.util.Date(it.date)) }
            return DailyMetric(title, dates, data.map { it.value })
        }
        return listOf(
            cv("체지방률", r.bodyComposition.bfp),
            cv("기초대사량", r.bodyComposition.bmr),
            cv("체내 수분", r.bodyComposition.ecf),
            cv("단백질량", r.bodyComposition.protein),
            cv("수축기 혈압", r.bloodPressure.sbp),
            cv("이완기 혈압", r.bloodPressure.dbp),
            cv("스트레스 지수", r.stress.stressValue)
        )
    }
}
