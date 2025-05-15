package com.example.myapplication.ui.schedule.photoschedule.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dto.response.schedule.OCRScheduleResponse
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
                    Log.d("registerPhotoSchedule", "${response.body()}")
                    _ocrError.value = "등록에 실패했습니다. 다시 시도해 주세요."
                }
            }.onFailure {
                Log.d("registerPhotoSchedule", "${it}")
                _ocrError.value = "등록에 실패했습니다. 다시 시도해 주세요."
            }.also {
                _isLoading.value = false
            }

        }
    }
}