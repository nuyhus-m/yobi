package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ScheduleResponseDTO implements ApiResult {
    private Integer scheduleId;
    private Integer clientId;
    private String clientName;
    private LocalDate visitedDate;
    private LocalTime startAt;
    private LocalTime endAt;

    public static ScheduleResponseDTO of(Schedule schedule) {
        return ScheduleResponseDTO.builder()
                .scheduleId(schedule.getId())
                .clientId(schedule.getClient().getId())
                .clientName(schedule.getClient().getName())
                .visitedDate(schedule.getVisitedDate())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .build();
    }
} 