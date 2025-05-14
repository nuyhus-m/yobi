package com.example.myapplication.ui.measure.measuretarget.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.MeasureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val TAG = "MeasureTargetViewModel"

@HiltViewModel
class MeasureTargetViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {

    private val _isMeasured = MutableSharedFlow<Boolean>()
    val isMeasured: SharedFlow<Boolean> = _isMeasured

    fun getMeasureStatus(clientId: Int) {
        viewModelScope.launch {
            kotlin.runCatching {
                measureRepository.getMeasureStatus(clientId)
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        _isMeasured.emit(it.measured)
                    }
                    Log.d(TAG, "getMeasureStatus: ${response.body()}")
                } else {
                    Log.d(TAG, "getMeasureStatus: ${response.code()}")
                }
            }.onFailure {
                Log.e(TAG, "getMeasureStatus: ${it.message}", it)
            }
        }
    }
}
