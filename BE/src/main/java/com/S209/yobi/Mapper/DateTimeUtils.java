package com.S209.yobi.Mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


/**
 * 시간 및 날짜를 Long 타입으로 변환
 */
public class DateTimeUtils {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    // local 타입 -> long
    public static long toEpochMilli(LocalDate date) {
        return date.atStartOfDay(DEFAULT_ZONE)
                .toInstant()
                .toEpochMilli(); // UTC millis 기준
    }

    // Instant 타입 -> long
    public static long toEpochMilli(Instant instant) {
        return instant.toEpochMilli(); // 그대로 UTC millis
    }
}
