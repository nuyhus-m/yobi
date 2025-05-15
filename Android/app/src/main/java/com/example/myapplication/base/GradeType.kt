package com.example.myapplication.base

import com.example.myapplication.R

enum class GradeType(val stringResId: Int, val drawableResId: Int, val colorResId: Int) {
    HIGH(R.string.high, R.drawable.bg_red_sub_radius_4, R.color.red),
    MIDDLE(R.string.mid, R.drawable.bg_green_sub_radius_4, R.color.green),
    LOW(R.string.low, R.drawable.bg_blue_sub_radius_4, R.color.blue),
}