package com.S209.yobi.schedules.service;

import com.S209.yobi.DTO.requestDTO.ScheduleRequestDto;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.clients.repository.ClientRepository;
import com.S209.yobi.schedules.entity.Schedule;
import com.S209.yobi.schedules.repository.ScheduleRepository;
import com.S209.yobi.users.entity.User;
import com.S209.yobi.users.repository.UserRepository;
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
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    // 단건 일정 조회
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

    // 단건 일정 등록
    @Transactional
    public void createSchedule(ScheduleRequestDto requestDto) {
        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + requestDto.getClientId()));

        // JWT에서 userId 추출하여 사용하여야 함.
        // 현재는 임시 하드코딩!!!!
        Integer userId = 1;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (requestDto.getEndAt().isBefore(requestDto.getStartAt())) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없음.");
        }

        Schedule schedule = Schedule.builder()
                .user(user)
                .client(client)
                .visitedDate(requestDto.getVisitedDate())
                .startAt(requestDto.getStartAt())
                .endAt(requestDto.getEndAt())
                .build();

        scheduleRepository.save(schedule);
    }
}