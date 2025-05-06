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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 단건 일정 수정
    @Transactional
    public void updateSchedule(Integer scheduleId, ScheduleRequestDto requestDto) throws AccessDeniedException {
        // 현재 인증된 사용자인지 확인
        // 임시 하드코딩! 추후 JWT에서 추출해야 합니다.
        Integer currentUserId = 1;

        // 기존 일정 존재 여부 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        // 수정 권한 확인
        if (!schedule.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("해당 일정을 수정할 권한이 없음.");
        }

        // client 존재 여부 확인
        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        // 시간 유효성 검사
        if (requestDto.getEndAt().isBefore(requestDto.getStartAt())) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없음.");
        }

        schedule.setClient(client);
        schedule.setVisitedDate(requestDto.getVisitedDate());
        schedule.setStartAt(requestDto.getStartAt());
        schedule.setEndAt(requestDto.getEndAt());

//        scheduleRepository.save(schedule); // 트랜잭션 내에서 변경 감지로 업데이트됨 -> 명시적 저장 불필요
    }

    // 단건 일정 삭제
    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        //Schedule 존재 여부 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        // 권한 검증
        // 임시로 하드코딩. JWT 추출 필요
        Integer currentUserId = 1;
        if (!schedule.getUser().getId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("삭제 권한이 없음.");
        }

        scheduleRepository.delete(schedule);
    }

    // 특정 요양보호사의 일정 리스트
    @Transactional
    public List<Map<String, Object>> getSchedulesByUser(Integer userId) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByVisitedDateAscStartAtAsc(userId);

        return schedules.stream()
                .map(schedule -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("scheduleId", schedule.getId());
                    map.put("clientId", schedule.getClient().getId());
                    map.put("visitedDate", schedule.getVisitedDate());
                    map.put("startAt", schedule.getStartAt());
                    map.put("endAt", schedule.getEndAt());
                    return map;
                })
                .collect(Collectors.toList());
    }
}