package com.example.myapplication.ui.visitlog.visitloglist.viewmodel.data

data class FilterItem(
    val name: String,
    val isSelected: Boolean = false,
    val clientId: Int? = null // "전체"일 경우 null

)
