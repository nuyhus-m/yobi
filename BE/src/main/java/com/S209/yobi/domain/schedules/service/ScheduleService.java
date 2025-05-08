package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.requestDTO.OcrDTO;
import com.S209.yobi.DTO.requestDTO.OcrDTO.OcrResponseDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleCreateRequestDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.S209.yobi.DTO.responseDTO.ScheduleResponseDTO;
import com.S209.yobi.DTO.responseDTO.SimpleResultDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.domain.schedules.repository.ScheduleRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final ImageResizeService imageResizeService;

    // 단건 일정 조회
    @Transactional(readOnly = true)
    public ApiResult getSchedule(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        return ScheduleResponseDTO.of(schedule);
    }

    // 단건 일정 등록
    @Transactional
    public ApiResult createSchedule(ScheduleCreateRequestDTO requestDto) {
        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + requestDto.getClientId()));

        // JWT에서 userId 추출하여 사용하여야 함.
        // 현재는 임시 하드코딩!!!!
        Integer userId = 6;
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

        return null;
    }

    // 단건 일정 수정
    @Transactional
    public ApiResult updateSchedule(Integer scheduleId, ScheduleUpdateRequestDTO requestDto) throws AccessDeniedException {
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

        if (requestDto.getClientId() != null) {
            Client client = clientRepository.findById(requestDto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));
            schedule.setClient(client);
        }

        if (requestDto.getVisitedDate() != null) {
            schedule.setVisitedDate(requestDto.getVisitedDate());
        }

        if (requestDto.getStartAt() != null) {
            schedule.setStartAt(requestDto.getStartAt());
        }

        if (requestDto.getEndAt() != null) {
            schedule.setEndAt(requestDto.getEndAt());
        }

        if (requestDto.getStartAt() != null && requestDto.getEndAt() != null) {
            if (requestDto.getEndAt().isBefore(requestDto.getStartAt())) {
                throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없음.");
            }
        }


//        scheduleRepository.save(schedule); // 트랜잭션 내에서 변경 감지로 업데이트됨 -> 명시적 저장 불필요

        return null;
    }

    // 단건 일정 삭제
    @Transactional
    public ApiResult deleteSchedule(Integer scheduleId) {
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

        return null;
    }

    // 특정 요양보호사의 일정 리스트
    @Transactional
    public ApiResult getSchedulesByUser(Integer userId) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByVisitedDateAscStartAtAsc(userId);

        List<Map<String, Object>> result = schedules.stream()
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

        return new SimpleResultDTO<>(result);
    }

    // 특정 월의 일정 리스트
    @Transactional
    public ApiResult getSchedulesByMonth(Integer userId, int year, int month) {
        if (year < 2000 || year > 2100 || month < 1 || month >12) {
                throw new IllegalArgumentException("유효하지 않은 년월임.");
            }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
                userId, startDate, endDate
        );

        List<Map<String, Object>> result = schedules.stream()
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

        return new SimpleResultDTO<>(result);
    }

    // 특정일의 일정 리스트
    @Transactional
    public ApiResult getSchedulesByDay(Integer userId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해주세요.");
        }

        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateOrderByStartAtAsc(
                userId, date
        );

        List<Map<String, Object>> result = schedules.stream()
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

        return new SimpleResultDTO<>(result);
    }

    // OCR로 일정 등록
    @Transactional
    public ApiResult processOcrSchedules(MultipartFile image, Integer userId, Integer year, Integer month) {
        //이미지 유효성 검사
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없음.");
        }

        //년월 유효성 검사
        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
            throw new IllegalArgumentException("유효하지 않은 년월입니다.");
        }

        try {
            // 이미지 리사이징 처리
            MultipartFile resizedImage = imageResizeService.resizeImageIfNeeded(image);
            log.info("이미지 크기: 원본 {}KB -> 변환 후 {}KB",
                    image.getSize() / 1024,
                    resizedImage.getSize() / 1024);

            // FastAPI 서버에 리사이징된 이미지 전송
            OcrResponseDTO ocrResult = ocrFastApiClient.processImage(resizedImage);

            // 일정 등록
            int count = 0;
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // 결과에서 일정 정보 추출 및 저장
            for (OcrResponseDTO.ScheduleItem item : ocrResult.getSchedules()) {
                try {
                    // 클라이언트 이름으로 클라이언트 찾기
                    Client client = clientRepository.findByName(item.getClientName())
                            .orElseThrow(() -> new EntityNotFoundException("Client not found with name: " + item.getClientName()));

                    // 날짜, 시간 파싱
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

            return new SimpleResultDTO<>(OcrDTO.OcrResultDTO.builder().count(count).build());
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }
}
