package com.example.myapplication.util


import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TimeUtils {
    /**
     * Long (UTC millis) → LocalDate
     * ex: 1742828400000 → 2025-05-14
     */
    fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    /**
     * Long (UTC millis) → LocalTime
     * ex: 1742828400000 → 10:00
     */
    fun Long.toLocalTime(): LocalTime =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalTime()

    /**
     * Long (UTC millis) → "HH:mm" 포맷 텍스트
     * ex: 1742828400000 → "10:00"
     */
    fun Long.toTimeText(): String =
        this.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

    /**
     * LocalDate → 해당 날짜의 00:00을 기준으로 한 UTC millis
     * ex: 2025-05-14 → 1742828400000
     */
    fun LocalDate.toEpochMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * LocalDateTime → UTC millis (시간 포함)
     * ex: 2025-05-14T10:00 → 1742864400000
     */
    fun LocalDateTime.toEpochMillis(): Long =
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * ZonedDateTime → UTC millis (정확한 시간대 포함)
     */
    fun ZonedDateTime.toEpochMillis(): Long =
        this.toInstant().toEpochMilli()

    /**
     * "MM/dd" 형식(예: "05/14") → 00:00:00 기준 epoch millis
     *  - 파싱이 실패하면 0L 반환
     */
    fun String.toEpochMillisFromMMDD(): Long {
        return try {
            val monthDayFmt = DateTimeFormatter.ofPattern("MM/dd")
            val parsedDate  = LocalDate.parse(this, monthDayFmt)   // 올해 날짜로 해석
            parsedDate.toEpochMillis()                             // 기존 확장함수 재활용
        } catch (e: Exception) {
            0L
        }
    }


    /**
     * LocalDate + LocalTime → UTC millis
     * ex: 2025-05-14 + 10:00 → 1742864400000
     */
    fun toEpochMillis(date: LocalDate, time: LocalTime): Long =
        date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}