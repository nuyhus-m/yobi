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
import com.example.myapplication.data.dto.response.measure.HealthDataResponse
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
    private val measureRepository: MeasureRepository,
) : ViewModel(), FitrusBleDelegate {

    private lateinit var _client: ClientDetailResponse
    val client: ClientDetailResponse get() = _client

    private var _isMeasured = false
    val isMeasured: Boolean get() = _isMeasured

    private var _measureType = HealthDataType.BODY_COMPOSITION
    val measureType: HealthDataType get() = _measureType

    private lateinit var _bodyCompositionResult: BodyCompositionResult
    val bodyCompositionResult: BodyCompositionResult get() = _bodyCompositionResult

    private lateinit var _healthDataResponse: HealthDataResponse
    val healthDataResponse: HealthDataResponse get() = _healthDataResponse

    private var _isMeasuring = false
    val isMeasuring: Boolean get() = _isMeasuring

    private var _isFirst = true
    val isFirst: Boolean get() = _isFirst

    private val _measureResult = MutableSharedFlow<MeasureResult>()
    val measureResult: SharedFlow<MeasureResult> = _measureResult

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var connectJob: Job? = null

    private lateinit var manager: FitrusDevice

    fun initFitrusDevice(device: FitrusDevice) {
        manager = device
    }

    fun setClient(client: ClientDetailResponse) {
        _client = client
    }

    fun setMeasureStatus(status: Boolean) {
        _isMeasured = status
    }

    fun setMeasureType(type: HealthDataType) {
        _measureType = type
    }

    fun setMeasuringStatus(status: Boolean) {
        _isMeasuring = status
    }

    fun setIsFirst(status: Boolean) {
        _isFirst = status
    }

    fun setBodyCompositionResult(result: BodyCompositionResult) {
        _bodyCompositionResult = result
    }

    fun setHealthDataResponse(result: HealthDataResponse) {
        _healthDataResponse = result
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
    }

    fun startMeasure() {
        if (manager.fitrusConnectionState) {
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
        } else {
            Log.d(TAG, "startMeasure: off")
            _isConnected.value = false
        }
    }

    override fun fitrusDispatchError(error: String) {
        Log.e(TAG, "fitrusDispatchError: $error")
        viewModelScope.launch {
            _toastMessage.emit("측정에 실패하였습니다. 다시 시도해주세요😭😭")
        }
        disconnectDevice()
    }

    override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusCompMeasured(result: Map<String, String>) {
        Log.d(TAG, "handleFitrusCompMeasured: $result")
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
        Log.d(TAG, "handleFitrusPpgMeasured: $result")
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
        Log.d(TAG, "handleFitrusPpgMeasured: $result")
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