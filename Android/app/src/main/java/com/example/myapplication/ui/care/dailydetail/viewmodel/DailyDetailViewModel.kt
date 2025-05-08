package com.example.myapplication.ui.care.dailydetail.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.care.dailydetail.data.DailyDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DailyDetailViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableLiveData(
        DailyDetailUiState(
            bodyFat = "100%",
            muscleMass = "100kg",
            bmr = "1000kcal",
            bodyWater = "100L",
            protein = "100kg",
            mineral = "100kg",
            bodyAge = "100세",
            temperature = "100",
            systolic = "100",
            diastolic = "100",
            heartRate = "72",
            oxygen = "98",
            stressIndex = "100",
            stressLevel = "높음"
        )
    )
    val uiState: LiveData<DailyDetailUiState> = _uiState
}
