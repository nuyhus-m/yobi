package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO implements ApiResult {
    private List<ScheduleDTO> schedules;

    @Getter
    @Builder
    public static class ScheduleDTO implements ApiResult {
        private Integer scheduleId;
        private Integer clientId;
        private String clientName;
        private LocalDate visitedDate;
        private LocalTime startAt;
        private LocalTime endAt;
    }

    // 여러 일정을 변환하는 정적 메소드
    public static ScheduleResponseDTO fromList(List<Schedule> schedules) {
        List<ScheduleDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> ScheduleDTO.builder()
                        .scheduleId(schedule.getId())
                        .clientId(schedule.getClient().getId())
                        .clientName(schedule.getClient().getName())
                        .visitedDate(schedule.getVisitedDate())
                        .startAt(schedule.getStartAt())
                        .endAt(schedule.getEndAt())
                        .build())
                .collect(Collectors.toList());

        return new ScheduleResponseDTO(scheduleDTOs);
    }

    // 단일 일정을 변환하는 정적 메소드
    public static ScheduleDTO fromSchedule(Schedule schedule) {
        return ScheduleDTO.builder()
                .scheduleId(schedule.getId())
                .clientId(schedule.getClient().getId())
                .clientName(schedule.getClient().getName())
                .visitedDate(schedule.getVisitedDate())
                .startAt(schedule.getStartAt())
                .endAt(schedule.getEndAt())
                .build();
    }
}