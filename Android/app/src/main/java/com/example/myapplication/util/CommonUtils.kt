package com.example.myapplication.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

object CommonUtils {

    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> mapToDataClass(map: Map<String, Any>): T {
        val jsonObject = JsonObject(map.mapValues { JsonPrimitive(it.value.toString()) })
        return json.decodeFromJsonElement(jsonObject)
    }

    fun convertDateFormat(date: String): String {
        return date.replace("-", "/")
    }
}