package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO.DailyLogDetailDTO;
import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO.SimpleDailyLogDTO;
import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO;
import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.domain.schedules.repository.ScheduleRepository;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        if (content != null) {
            schedule.setLogContent(content);
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

    // 일지 전체 리스트
    @Transactional
    public ApiResult getDailyLogsByUser(Integer userId) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByVisitedDateAscStartAtAsc(userId);
        if (schedules.isEmpty()) {
            return null;
        }

        List<SimpleDailyLogDTO> dailyLogs = schedules.stream()
                .map(schedule -> SimpleDailyLogDTO.builder()
                        .scheduleId(schedule.getId())
                        .clientName(schedule.getClient().getName())
                        .visitedDate(schedule.getVisitedDate())
                        .build())
                .collect(Collectors.toList());

        return new DailyLogResponseDTO(dailyLogs);
    }

    // 특정 돌봄 대상에 대한 일지 리스트


    // 일지 단건 조회
    @Transactional
    public ApiResult getDailyLog(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule Not Found."));

        String logContent = schedule.getLogContent();
        String clientName = schedule.getClient().getName();
        LocalDate visitedDate = schedule.getVisitedDate();

        // logContent가 null인 경우에도 다른 정보는 반환하도록 수정
//        if (logContent == null) {
//            logContent = "";
//        }

        DailyLogDetailDTO detailDTO = DailyLogDetailDTO.builder()
                .logContent(logContent)
                .clientName(clientName)
                .visitedDate(visitedDate)
                .build();

        return detailDTO;
    }
}
