package com.example.myapplication.ui.measure.measureloading.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.request.measure.BloodPressureRequest
import com.example.myapplication.data.dto.request.measure.BodyCompositionRequest
import com.example.myapplication.data.dto.request.measure.HeartRateRequest
import com.example.myapplication.data.dto.request.measure.RequiredDataRequest
import com.example.myapplication.data.dto.request.measure.StressRequest
import com.example.myapplication.data.dto.request.measure.TemperatureRequest
import com.example.myapplication.data.dto.response.measure.HealthDataResponse
import com.example.myapplication.data.repository.MeasureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MeasureLoadingViewModel"

@HiltViewModel
class MeasureLoadingViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {

    private val _healthDataResponse = MutableSharedFlow<HealthDataResponse>()
    val healthDataResponse: SharedFlow<HealthDataResponse> = _healthDataResponse

    fun saveRequiredMeasureData(clientId: Int, body: RequiredDataRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveRequiredMeasureData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveRequiredMeasureData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveRequiredMeasureData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveRequiredMeasureData: ${it.message}", it)
            }
        }
    }

    fun saveBodyCompositionData(clientId: Int, body: BodyCompositionRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveBodyCompositionData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveBodyCompositionData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveBodyCompositionData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveBodyCompositionData: ${it.message}", it)
            }
        }
    }

    fun saveHeartRateData(clientId: Int, body: HeartRateRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveHeartRateData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveHeartRateData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveHeartRateData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveHeartRateData: ${it.message}", it)
            }
        }
    }

    fun saveBloodPressureData(clientId: Int, body: BloodPressureRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveBloodPressureData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveBloodPressureData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveBloodPressureData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveBloodPressureData: ${it.message}", it)
            }
        }
    }

    fun saveStressData(clientId: Int, body: StressRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveStressData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveStressData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveStressData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveStressData: ${it.message}", it)
            }
        }
    }

    fun saveTemperatureData(clientId: Int, body: TemperatureRequest) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.saveTemperatureData(clientId, body)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _healthDataResponse.emit(it)
                    }
                    Log.d(TAG, "saveTemperatureData: ${response.body()}")
                } else {
                    Log.d(TAG, "saveTemperatureData: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "saveTemperatureData: ${it.message}", it)
            }
        }
    }

}