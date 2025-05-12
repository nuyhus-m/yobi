package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.base.HealthDataType
import com.onesoftdigm.fitrus.device.sdk.FitrusBleDelegate
import com.onesoftdigm.fitrus.device.sdk.FitrusDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FitrusViewModel"

@HiltViewModel
class FitrusViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), FitrusBleDelegate {

    private var _clientId = -1
    val clientId: Int get() = _clientId

    private var _clientName = ""
    val clientName: String get() = _clientName

    private var _isMeasured = false
    val isMeasured: Boolean get() = _isMeasured

    private var _type = HealthDataType.BODY_COMPOSITION
    val type: HealthDataType get() = _type

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private var connectJob: Job? = null

    private val manager = FitrusDevice(context, this, "normal_key")

    fun setClientId(id: Int) {
        _clientId = id
    }

    fun setClientName(name: String) {
        _clientName = name
    }

    fun setMeasureStatus(status: Boolean) {
        _isMeasured = status
    }

    fun setMeasureType(type: HealthDataType) {
        _type = type
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

    override fun fitrusDispatchError(error: String) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusCompMeasured(result: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusConnected() {
        _isConnected.value = true
    }

    override fun handleFitrusDeviceInfo(result: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusDisconnected() {
        _isConnected.value = false
    }

    override fun handleFitrusPpgMeasured(result: Map<String, Any>) {
        TODO("Not yet implemented")
    }

    override fun handleFitrusTempMeasured(result: Map<String, String>) {
        TODO("Not yet implemented")
    }
}