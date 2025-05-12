package com.example.myapplication.base

import com.example.myapplication.R

enum class HealthDataType(val resId: Int) {
    BODY_COMPOSITION(R.string.body_composition),
    HEART_RATE(R.string.heart_rate),
    BLOOD_PRESSURE(R.string.blood_pressure),
    STRESS(R.string.stress),
    TEMPERATURE(R.string.temperature),
}