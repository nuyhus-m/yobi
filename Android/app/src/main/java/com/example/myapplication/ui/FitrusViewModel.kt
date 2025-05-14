package com.example.myapplication.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.base.HealthDataType
import com.example.myapplication.data.dto.model.BloodPressureResult
import com.example.myapplication.data.dto.model.BodyCompositionResult
import com.example.myapplication.data.dto.model.HeartRateResult
import com.example.myapplication.data.dto.model.MeasureResult
import com.example.myapplication.data.dto.model.StressResult
import com.example.myapplication.data.dto.model.TemperatureResult
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.data.repository.ClientRepository
import com.example.myapplication.data.repository.MeasureRepository
import com.example.myapplication.util.CommonUtils
import com.example.myapplication.util.CommonUtils.mapToDataClass
import com.onesoftdigm.fitrus.device.sdk.FitrusBleDelegate
import com.onesoftdigm.fitrus.device.sdk.FitrusDevice
import com.onesoftdigm.fitrus.device.sdk.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FitrusViewModel"

@HiltViewModel
class FitrusViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val measureRepository: MeasureRepository,
) : ViewModel(), FitrusBleDelegate {

    private var _client: ClientDetailResponse? = null
    val client: ClientDetailResponse get() = _client!!

    private var _isMeasured = false
    val isMeasured: Boolean get() = _isMeasured

    private var _measureType = HealthDataType.BODY_COMPOSITION
    val measureType: HealthDataType get() = _measureType

    private val _isInfoSuccess = MutableSharedFlow<Boolean>()
    val isInfoSuccess: SharedFlow<Boolean> = _isInfoSuccess

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

    private val _measureResult = MutableSharedFlow<MeasureResult>()
    val measureResult: SharedFlow<MeasureResult> = _measureResult

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var connectJob: Job? = null

    private lateinit var manager: FitrusDevice

    fun initFitrusDevice(device: FitrusDevice) {
        manager = device
    }

    fun setMeasureType(type: HealthDataType) {
        _measureType = type
    }

    fun getClientDetail(clientId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                clientRepository.getClientDetail(clientId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _client = it
                        getMeasureStatus(clientId)
                    }
                    Log.d(TAG, "getClientDetail: ${response.body()}")
                } else {
                    _toastMessage.emit("측정 대상 정보를 불러오는데 실패했습니다😭")
                    Log.d(TAG, "getClientDetail: ${response.code()}")
                }
            }.onFailure {
                _toastMessage.emit("측정 대상 정보를 불러오는데 실패했습니다😭")
                Log.e(TAG, "getClientDetail: ${it.message}", it)
            }
        }
    }

    private fun getMeasureStatus(clientId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getMeasureStatus(clientId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _isMeasured = it.measured
                        _isInfoSuccess.emit(true)
                    }
                    Log.d(TAG, "getMeasureStatus: ${response.body()}")
                } else {
                    _toastMessage.emit("측정 대상 정보를 불러오는데 실패했습니다😭")
                    Log.d(TAG, "getMeasureStatus: ${response.code()}")
                }
            }.onFailure {
                _toastMessage.emit("측정 대상 정보를 불러오는데 실패했습니다😭")
                Log.e(TAG, "getMeasureStatus: ${it.message}", it)
            }
        }
    }

    private fun startScan() {
        _isConnected.value = manager.fitrusConnectionState
        if (!manager.fitrusConnectionState) {
            manager.startFitrusScan()
        }
    }

    private fun stopScan() {
        manager.stopFitrusScan()
    }

    fun tryConnectDevice() {
        connectJob = viewModelScope.launch {
            while (!_isConnected.value) {
                Log.d(TAG, "tryConnectDevice: ")
                stopScan()
                startScan()
                delay(30000)
            }
        }
    }

    fun stopTryConnectDevice() {
        Log.d(TAG, "stopTryConnectDevice: ")
        connectJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        stopScan()
        connectJob = null
    }

    fun disconnectDevice() {
        Log.d(TAG, "disconnectDevice: ")
        if (manager.fitrusConnectionState) {
            manager.disconnectFitrus()
        }
        _isConnected.value = false
    }

    fun startMeasure() {
        when (measureType) {
            HealthDataType.BODY_COMPOSITION -> {
                Log.d(TAG, "startMeasure: 체성분")
                manager.startFitrusCompMeasure(
                    if (client.gender == 0) {
                        Gender.MALE
                    } else {
                        Gender.FEMALE
                    },
                    client.height,
                    client.weight,
                    CommonUtils.convertDateFormat(client.birth),
                    0f,
                )
            }

            HealthDataType.HEART_RATE -> {
                Log.d(TAG, "startMeasure: 심박")
                manager.startFitrusHeartRateMeasure()
            }

            HealthDataType.BLOOD_PRESSURE -> {
                Log.d(TAG, "startMeasure: 혈압")
                manager.StartFitrusBloodPressure(120f, 80f)
            }

            HealthDataType.STRESS -> {
                Log.d(TAG, "startMeasure: 스트레스")
                manager.startFitrusStressMeasure(CommonUtils.convertDateFormat(client.birth))
            }

            HealthDataType.TEMPERATURE -> {
                Log.d(TAG, "startMeasure: 체온")
                manager.startFitrusTempBodyMeasure()
            }
        }
    }

    override fun fitrusDispatchError(error: String) {
        Log.e(TAG, "fitrusDispatchError: $error")
    }

    override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusCompMeasured(result: Map<String, String>) {
        viewModelScope.launch {
            runCatching {
                val data = mapToDataClass<BodyCompositionResult>(result)
                _measureResult.emit(data)
                Log.d(TAG, "handleFitrusCompMeasured: $data")
            }.onFailure {
                Log.e(TAG, "BodyComposition 파싱 실패: ${it.message}", it)
            }
        }
    }

    override fun handleFitrusConnected() {
        Log.d(TAG, "handleFitrusConnected: ")
        _isConnected.value = true
    }

    override fun handleFitrusDeviceInfo(result: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusDisconnected() {
        Log.d(TAG, "handleFitrusDisconnected: ")
        _isConnected.value = false
    }

    override fun handleFitrusPpgMeasured(result: Map<String, Any>) {
        viewModelScope.launch {
            when (measureType) {
                HealthDataType.HEART_RATE -> {
                    runCatching {
                        val data = mapToDataClass<HeartRateResult>(result)
                        _measureResult.emit(data)
                        Log.d(TAG, "handleFitrusPpgMeasured: $data")
                    }.onFailure {
                        Log.e(TAG, "HeartRate 파싱 실패: ${it.message}", it)
                    }
                }

                HealthDataType.BLOOD_PRESSURE -> {
                    runCatching {
                        val data = mapToDataClass<BloodPressureResult>(result)
                        _measureResult.emit(data)
                        Log.d(TAG, "handleFitrusPpgMeasured: $data")
                    }.onFailure {
                        Log.e(TAG, "BloodPressure 파싱 실패: ${it.message}", it)
                    }
                }

                HealthDataType.STRESS -> {
                    runCatching {
                        val data = mapToDataClass<StressResult>(result)
                        _measureResult.emit(data)
                        Log.d(TAG, "handleFitrusPpgMeasured: $data")
                    }.onFailure {
                        Log.e(TAG, "Stress 파싱 실패: ${it.message}", it)
                    }
                }

                else -> {
                    Log.e(TAG, "handleFitrusPpgMeasured: 데이터 타입이 없음")
                }
            }
        }
    }

    override fun handleFitrusTempMeasured(result: Map<String, String>) {
        viewModelScope.launch {
            runCatching {
                val data = mapToDataClass<TemperatureResult>(result)
                _measureResult.emit(data)
                Log.d(TAG, "handleFitrusTempMeasured: $data")
            }.onFailure {
                Log.e(TAG, "Temp 파싱 실패: ${it.message}", it)
            }
        }
    }
}