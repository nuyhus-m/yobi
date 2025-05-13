package com.example.myapplication.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtils {

    //날짜 포맷 출력
    fun dateformatYMDHM(time: Date): String {
        val format = SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.KOREA)
        format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return format.format(time)
    }

    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> mapToDataClass(map: Map<String, Any>): T {
        val jsonObject = JsonObject(map.mapValues { JsonPrimitive(it.value.toString()) })
        return json.decodeFromJsonElement(jsonObject)
    }
}