package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.ScheduleService
import jakarta.inject.Inject

class ScheduleRepository @Inject constructor(
    private val scheduleService: ScheduleService
){

}