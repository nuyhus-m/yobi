package com.example.myapplication.ui.measure.measureresult.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.measure.HealthDataResultResponse
import com.example.myapplication.data.repository.MeasureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MeasureResultViewModel"

@HiltViewModel
class MeasureResultViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {

    private val _result = MutableLiveData<HealthDataResultResponse>()
    val result: LiveData<HealthDataResultResponse> = _result

    fun getBodyCompResult(bodyId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getBodyCompResult(bodyId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = it
                    }
                    Log.d(TAG, "getBodyCompResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getBodyCompResult: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getBodyCompResult: ${it.message}", it)
            }
        }
    }

    fun getBloodPressureResult(bloodId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getBloodPressureResult(bloodId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = it
                    }
                    Log.d(TAG, "getBloodPressureResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getBloodPressureResult: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getBloodPressureResult: ${it.message}", it)
            }
        }
    }

    fun getHeartRateResult(heartRateId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getHeartRateResult(heartRateId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = it
                    }
                    Log.d(TAG, "getHeartRateResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getHeartRateResult: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getHeartRateResult: ${it.message}", it)
            }
        }
    }

    fun getStressResult(stressId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getStressResult(stressId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = it
                    }
                    Log.d(TAG, "getStressResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getStressResult: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getStressResult: ${it.message}", it)
            }
        }
    }

    fun getTemperatureResult(temperatureId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getTemperatureResult(temperatureId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _result.value = it
                    }
                    Log.d(TAG, "getTemperatureResult: ${response.body()}")
                } else {
                    Log.d(TAG, "getTemperatureResult: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getTemperatureResult: ${it.message}", it)
            }
        }
    }
}