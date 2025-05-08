package com.S209.yobi.schedules.service;

import com.S209.yobi.DTO.requestDTO.OcrDTO;
import com.S209.yobi.DTO.requestDTO.OcrDTO.OcrResponseDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.clients.repository.ClientRepository;
import com.S209.yobi.schedules.entity.Schedule;
import com.S209.yobi.schedules.repository.ScheduleRepository;
import com.S209.yobi.users.entity.User;
import com.S209.yobi.users.repository.UserRepository;
import com.S209.yobi.schedules.service.OcrFastApiClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final OcrFastApiClient ocrFastApiClient;

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
    public void createSchedule(ScheduleRequestDTO requestDto) {
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
    public void updateSchedule(Integer scheduleId, ScheduleRequestDTO requestDto) throws AccessDeniedException {
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

    // 특정 월의 일정 리스트
    @Transactional
    public List<Map<String, Object>> getSchedulesByMonth(Integer userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
                userId, startDate, endDate
        );

        log.info("시작: {}, 끝: {}", startDate, endDate);

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

    // 특정일의 일정 리스트
    @Transactional
    public List<Map<String, Object>> getSchedulesByDay(Integer userId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateOrderByStartAtAsc(
                userId, date
        );

        return schedules.stream()
                .map(schedule -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("scheduleId", schedule.getId());
                    map.put("clientId", schedule.getClient().getId());
                    map.put("clientName", schedule.getClient().getName());
                    map.put("visitedDate", schedule.getVisitedDate());
                    map.put("startAt", schedule.getStartAt());
                    map.put("endAt", schedule.getEndAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // OCR로 일정 등록
    @Transactional
    public OcrDTO.OcrResultDTO processOcrSchedules(MultipartFile image, Integer userId, Integer year, Integer month) {
        //FastAPI 서버에 이미지 전송
        OcrResponseDTO ocrResult = ocrFastApiClient.processImage(image);

        //일정 등록
        int count = 0;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        //결과에서 일정 정보 추출 및 저장
        for (OcrResponseDTO.ScheduleItem item : ocrResult.getSchedules()) {
            try {
                // 클라이언트 이름으로 클라이언트 찾기
                Client client = clientRepository.findByName(item.getClientName())
                        .orElseThrow(() -> new EntityNotFoundException("Client not found with name: " + item.getClientName()));

                //날짜, 시간 파싱
                LocalDate visitedDate = LocalDate.of(year, month, item.getDay());
                LocalTime startAt = LocalTime.parse(item.getStartAt() + ":00");  // 초 추가
                LocalTime endAt = LocalTime.parse(item.getEndAt() + ":00");      // 초 추가

                // Schedule 생성 및 저장
                Schedule schedule = Schedule.builder()
                        .user(user)
                        .client(client)
                        .visitedDate(visitedDate)
                        .startAt(startAt)
                        .endAt(endAt)
                        .build();

                scheduleRepository.save(schedule);
                count++;
                log.info("스케줄 저장 완료 - 날짜: {}, 시작: {}, 종료: {}, 클라이언트: {}", 
                    visitedDate, startAt, endAt, client.getName());
            } catch (EntityNotFoundException e) {
                log.error("클라이언트를 찾을 수 없음: {}", item.getClientName());
                throw e;
            } catch (Exception e) {
                log.error("스케줄 저장 중 오류 발생: {}", e.getMessage());
                throw e;
            }
        }

        return OcrDTO.OcrResultDTO.builder().count(count).build();
    }
}