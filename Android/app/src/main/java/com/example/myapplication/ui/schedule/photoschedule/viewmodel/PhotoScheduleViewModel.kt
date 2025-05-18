package com.example.myapplication.ui.schedule.photoschedule.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.schedule.OCRScheduleItem
import com.example.myapplication.data.dto.response.schedule.OCRScheduleResponse
import com.example.myapplication.data.dto.response.schedule.SaveOCRScheduleResponse
import com.example.myapplication.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class PhotoScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
): ViewModel() {
    private val _ocrResult = MutableLiveData<OCRScheduleResponse>()
    val ocrResult: LiveData<OCRScheduleResponse> = _ocrResult

    private val _ocrError = MutableLiveData<String>()
    val ocrError: LiveData<String> = _ocrError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<SaveOCRScheduleResponse>()
    val saveResult: LiveData<SaveOCRScheduleResponse> = _saveResult



    fun registerPhotoSchedule(imageFile: File, year: Int, month: Int) {
        val timezone = TimeZone.getDefault().id
        _isLoading.value = true

        viewModelScope.launch {
            val requestBody = imageFile
                .asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                name = "image",
                filename = imageFile.name,
                body = requestBody
            )

            runCatching {
                val response = scheduleRepository.registerPhotoSchedule(imagePart, year, month, timezone)
                if (response.isSuccessful) {
                    Log.d("registerPhotoSchedule", "${response.body()}")
                    _ocrResult.value = response.body()
                } else {
                    _ocrError.value = "OCR 분석 실패: ${response.code()}"
                    Log.d("registerPhotoSchedule", "${response.body()}")
                }
            }.onFailure {
                Log.d("registerPhotoSchedule", "${it}")
                _ocrError.value = "네트워크 오류: ${it.message}"
            }.also {
                _isLoading.value = false
            }

        }
    }

    fun savePhotoSchedule(request: List<OCRScheduleItem>) {
        viewModelScope.launch {
            runCatching {
                val response = scheduleRepository.savePhotoSchedule(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _saveResult.value = it
                    }
                    Log.d("savePhotoSchedule", "${response.body()}")
                } else {
                    _ocrError.value = "일정 저장 실패: ${response.code()}"
                    Log.d("savePhotoSchedule", "${response.body()}")
                }
            }.onFailure {
                _ocrError.value = "일정 저장 실패: ${it}"
                Log.d("savePhotoSchedule", "${it}")
                }
        }

    }
}