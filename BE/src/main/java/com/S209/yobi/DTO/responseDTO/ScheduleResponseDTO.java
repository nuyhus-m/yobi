package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "스케줄 응답 객체")
public class ScheduleResponseDTO implements ApiResult {
    private List<ScheduleDTO> schedules;

    @Getter
    @Builder
    public static class ScheduleDTO implements ApiResult {
        private Integer scheduleId;
        private Integer clientId;
        private String clientName;
        private long visitedDate;
        private long startAt;
        private long endAt;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean hasLogContent;
    }

    // 여러 일정을 변환하는 정적 메소드
    public static ScheduleResponseDTO fromList(List<Schedule> schedules) {
        List<ScheduleDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> {

                    return ScheduleDTO.builder()
                            .scheduleId(schedule.getId())
                            .clientId(schedule.getClient().getId())
                            .clientName(schedule.getClient().getName())
                            .visitedDate(schedule.getVisitedDate())
                            .startAt(schedule.getStartAt())
                            .endAt(schedule.getEndAt())
                            .build();
                })
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