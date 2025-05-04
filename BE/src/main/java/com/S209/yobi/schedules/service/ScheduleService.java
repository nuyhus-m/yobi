package com.S209.yobi.schedules.service;

import com.S209.yobi.schedules.entity.Schedule;
import com.S209.yobi.schedules.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getSchedule(Integer scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", schedule.getId());
        response.put("clientId", schedule.getClient().getId());

        // LocalDate -> Unix timestamp(밀리초)
        response.put("visitedDate", schedule.getVisitedDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());

        // LocalTime -> 해당 날짜의 timestamp로
        LocalDateTime dateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getStartAt());

        response.put("startAt", dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());

        dateTime = LocalDateTime.of(schedule.getVisitedDate(), schedule.getEndAt());
        response.put("endAt", dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());

        return response;
    }
}