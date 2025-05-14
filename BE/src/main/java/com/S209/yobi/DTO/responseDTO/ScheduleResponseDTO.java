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
    // 여러 스케줄 정보를 담는 리스트
    private List<ScheduleDTO> schedules;


    /*
    * 개별 스케줄 정보를 담는 내부 클래스
    * 각 스케줄의 기본 정보를 포함함.
    * */
    @Getter
    @Builder
    public static class ScheduleDTO implements ApiResult {
        private Integer scheduleId;
        private Integer clientId;
        private String clientName;
        private long visitedDate;
        private long startAt;
        private long endAt;

        // null일 경우 JSON 응답에서 제외됨
        // '특정일의 일정 리스트' 응답 반환시에만 필요한 필드임.
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean hasLogContent;
    }

    /*
    * 스케줄 엔티티 리스트를 DTO 리스트로 변환하는 메서드
    * 여러 스케줄 정보를 한 번에 변환할 때 사용함.
    * */
    public static ScheduleResponseDTO fromList(List<Schedule> schedules) {
        // 스케줄 엔티티 리스트를 DTO 리스트로 스트림 변환
        List<ScheduleDTO> scheduleDTOs = schedules.stream()
                .map(schedule -> {
                    // 각 스케줄 엔티티를 DTO로 변환
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

    /*
    * 단일 스케줄 엔티티를 DTO로 변환하는 메서드
    * 단일 스케줄 정보만 필요할 때 사용*/
    public static ScheduleDTO fromSchedule(Schedule schedule) {

        // 스케줄 엔티티를 DTO로 변환하여 반환
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