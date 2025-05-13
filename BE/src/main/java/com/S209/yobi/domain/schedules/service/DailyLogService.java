package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO.DailyLogDetailDTO;
import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO.SimpleDailyLogDTO;
import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO;
import com.S209.yobi.Mapper.DateTimeUtils;
import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.domain.schedules.repository.ScheduleRepository;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyLogService {
    private final ScheduleRepository scheduleRepository;

    // 일지 작성 및 수정
    @Transactional
    public ApiResult updateDailyLog(Integer scheduleId, String content) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("Schedule Not Found."));

        ZonedDateTime seoulTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        if (content != null) {
            if (schedule.getLogCreatedAt() == null) {
                //최초 등록이라면
                schedule.setLogContent(content);
                schedule.setLogCreatedAt(seoulTime.toInstant());
            } else {
                // 수정이라면
                schedule.setLogContent(content);
                schedule.setLogUpdatedAt(seoulTime.toInstant());
            }
        }

        return null;
    }

    // 일지 삭제
    @Transactional
    public ApiResult deleteDailyLog(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("Schedule Not Found."));

        schedule.setLogContent(null);

        return null;
    }

    private List<SimpleDailyLogDTO> mapToSimpleDailyLogDTOs(List<Schedule> schedules) {
        return schedules.stream()
                .map(schedule -> SimpleDailyLogDTO.builder()
                        .scheduleId(schedule.getId())
                        .clientName(schedule.getClient().getName())
                        .visitedDate(schedule.getVisitedDate())
                        .build())
                .collect(Collectors.toList());
    }

    // 사용자의 일지 전체 리스트
    @Transactional
    public ApiResult getDailyLogsByUser(Integer userId) {
        List<Schedule> schedulesWithLogs = scheduleRepository.findByUserIdAndLogContentNotNullOrderByVisitedDateDescStartAtDesc(userId);

        if (schedulesWithLogs.isEmpty()) {
            return null;
        }

        return new DailyLogResponseDTO(mapToSimpleDailyLogDTOs(schedulesWithLogs));
    }

    // 특정 돌봄 대상에 대한 일지 리스트
    @Transactional
    public ApiResult getDailyLogsByClient(Integer userId, Integer clientId) {
        List<Schedule> schedulesWithLogs = scheduleRepository.findByUserIdAndClientIdAndLogContentNotNullOrderByVisitedDateDesc(userId, clientId);

        if (schedulesWithLogs.isEmpty()) {
            return null;
        }

        return new DailyLogResponseDTO(mapToSimpleDailyLogDTOs(schedulesWithLogs));
    }

    // 일지 단건 조회
    @Transactional
    @Schema
    public ApiResult getDailyLog(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule Not Found."));

        String logContent = schedule.getLogContent();
        String clientName = schedule.getClient().getName();

        DailyLogDetailDTO detailDTO = DailyLogDetailDTO.builder()
                .logContent(logContent)
                .clientName(clientName)
                .visitedDate(schedule.getVisitedDate())
                .build();

        return detailDTO;
    }
}
