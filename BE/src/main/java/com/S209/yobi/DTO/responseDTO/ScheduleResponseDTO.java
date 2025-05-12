package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.Mapper.DateTimeUtils;
import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
//        private LocalDate visitedDate;
//        private LocalTime startAt;
//        private LocalTime endAt;
    }

    // 여러 일정을 변환하는 정적 메소드
    public static ScheduleResponseDTO fromList(List<Schedule> schedules) {
        List<ScheduleDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> {
//                    LocalDateTime startDateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getStartAt());
//                    LocalDateTime endDateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getEndAt());

//                    // DateTimeUtils를 사용해서 변환
//                    long visitedDate = DateTimeUtils.toEpochMilli(schedule.getVisitedDate());
//                    long startAt = startDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
//                    long endAt = endDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();

                    return ScheduleDTO.builder()
                            .scheduleId(schedule.getId())
                            .clientId(schedule.getClient().getId())
                            .clientName(schedule.getClient().getName())
                            .visitedDate(schedule.getVisitedDate())
                            .startAt(schedule.getStartAt())
                            .endAt(schedule.getEndAt())
                            .build();
                })
//                .map(schedule -> ScheduleDTO.builder()
//                        .scheduleId(schedule.getId())
//                        .clientId(schedule.getClient().getId())
//                        .clientName(schedule.getClient().getName())
//                        .visitedDate(schedule.getVisitedDate())
//                        .startAt(schedule.getStartAt())
//                        .endAt(schedule.getEndAt())
//                        .build())
                .collect(Collectors.toList());

        return new ScheduleResponseDTO(scheduleDTOs);
    }

    // 단일 일정을 변환하는 정적 메소드
    public static ScheduleDTO fromSchedule(Schedule schedule) {
//        LocalDateTime startDateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getStartAt());
//        LocalDateTime endDateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getEndAt());
//
//        long visitedDate = DateTimeUtils.toEpochMilli(schedule.getVisitedDate());
//        long startAt = startDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();
//        long endAt = endDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();

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