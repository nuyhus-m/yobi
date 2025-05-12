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
import org.antlr.v4.runtime.misc.IntegerStack;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    // 단건 일정 조회
    @Transactional(readOnly = true)
    public ApiResult getSchedule(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        return ScheduleResponseDTO.fromSchedule(schedule);
    }

    // 단건 일정 등록
    @Transactional
    public ApiResult createSchedule(ScheduleCreateRequestDTO requestDto) {
        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + requestDto.getClientId()));

        // JWT에서 userId 추출하여 사용하여야 함.
        // 현재는 임시 하드코딩!!!!
        Integer userId = 1;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (requestDto.getEndAt() < requestDto.getStartAt()) {
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

        // 기존 일정 존재 여부 확인 - Join Fetch를 사용하여 한 번에 관련 엔티티 로드
        Schedule schedule = scheduleRepository.findByIdWithClientAndUser(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        // 수정 권한 확인
        if (!schedule.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("해당 일정을 수정할 권한이 없음.");
        }

        Client client = schedule.getClient();
        if (requestDto.getClientId() != null) {
            client = clientRepository.findById(requestDto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));
            schedule.setClient(client);
        }

        long visitedDate = schedule.getVisitedDate();
        if (requestDto.getVisitedDate() != null) {
            visitedDate = requestDto.getVisitedDate();
            schedule.setVisitedDate(visitedDate);
        }

        long startAt = schedule.getStartAt();
        if (requestDto.getStartAt() != null) {
            startAt = requestDto.getStartAt();
            schedule.setStartAt(startAt);
        }

        long endAt = schedule.getEndAt();
        if (requestDto.getEndAt() != null) {
            endAt = requestDto.getEndAt();
            schedule.setEndAt(endAt);
        }

        // 1. 필수 필드 유효성 검사
        validateRequiredFields(schedule);

        // 2. 시간 유효성 검사
        if (endAt < startAt) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없습니다.");
        }

        // 3. 일정 중복 검사
        checkScheduleOverlap(currentUserId, scheduleId, visitedDate, startAt, endAt);

        return ScheduleResponseDTO.fromSchedule(schedule);
    }

    // 필수 필드 유효성 검사
    private void validateRequiredFields(Schedule schedule) {
        if (schedule.getClient() == null) {
            throw new IllegalArgumentException("클라이언트 정보는 필수입니다.");
        }

        if (schedule.getVisitedDate() <= 0) {
            throw new IllegalArgumentException("방문 날짜는 필수입니다.");
        }

        if (schedule.getStartAt() <= 0) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }

        if (schedule.getEndAt() <= 0) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }
    }

    // 일정 중복 검사 메소드
    private void checkScheduleOverlap(Integer userId, Integer scheduleId, long visitedDateTimestamp, long startAtTimestamp, long endAtTimestamp) {
        // 해당 날짜의 시작과 끝 타임스탬프 계산
        // 주어진 visitedDateTimestamp에서 해당 날짜의 시작(00:00:00)과 끝(23:59:59) 타임스탬프 계산
        LocalDate date = Instant.ofEpochMilli(visitedDateTimestamp).atZone(DEFAULT_ZONE).toLocalDate();
        long dayStart = date.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
        long dayEnd = date.atTime(23, 59, 59).atZone(DEFAULT_ZONE).toInstant().toEpochMilli();

        // 같은 날짜에 시간이 겹치는 일정 조회
        List<Schedule> overlappingSchedules = scheduleRepository.findByUserIdAndVisitedDateWithClient(
                userId, dayStart, dayEnd);

        List<Schedule> conflicts = overlappingSchedules.stream()
                .filter(s -> !s.getId().equals(scheduleId)) // 해당 스케줄 자기 자신 제외
                .filter(s -> isTimeOverlapping(s.getStartAt(), s.getEndAt(), startAtTimestamp, endAtTimestamp)) // 시간 중복 확인
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            Schedule conflict = conflicts.get(0);
            String errorMessage = String.format("해당 시간(%s~%s)에 이미 다른 일정(%s~%s, %s님)이 있습니다.",
                    formatTimestamp(startAtTimestamp), formatTimestamp(endAtTimestamp),
                    formatTimestamp(conflict.getStartAt()), formatTimestamp(conflict.getEndAt()),
                    conflict.getClient().getName());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    // 타임스탬프를 HH:mm:ss 형식으로 포맷팅
    private String formatTimestamp(long timestamp) {
        LocalTime time = Instant.ofEpochMilli(timestamp).atZone(DEFAULT_ZONE).toLocalTime();
        return time.toString();
    }

    // 시간 중복 확인 헬퍼 메소드
    private boolean isTimeOverlapping(long existingStart, long existingEnd, long newStart, long newEnd) {
        return newStart < existingEnd && newEnd > existingStart;
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
    @Transactional(readOnly = true)
    public ApiResult getSchedulesByUser(Integer userId) {
        List<Schedule> schedules = scheduleRepository.findByUserIdOrderByVisitedDateAscStartAtAsc(userId);

        return ScheduleResponseDTO.fromList(schedules);
    }

    // 특정 월의 일정 리스트
    @Transactional(readOnly = true)
    public ApiResult getSchedulesByMonth(Integer userId, int year, int month) {
        // 해당 월의 첫날과 마지막날의 타임스탬프 계산
        LocalDate startLocalDate = LocalDate.of(year, month, 1);
        LocalDate endLocalDate = startLocalDate.plusMonths(1).minusDays(1);

        // 타임스탬프로 변환 (해당 날짜의 시작과 끝)
        long startTimestamp = startLocalDate.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
        long endTimestamp = endLocalDate.atTime(23, 59, 59).atZone(DEFAULT_ZONE).toInstant().toEpochMilli();

        // 해당 타임스탬프 범위 내의 일정 조회
        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
                userId, startTimestamp, endTimestamp
        );

        return ScheduleResponseDTO.fromList(schedules);
    }

    // 특정일의 일정 리스트
    @Transactional(readOnly = true)
    public ApiResult getSchedulesByDay(Integer userId, long date) {
        if (date <= 0) {
            throw new IllegalArgumentException("날짜를 입력해주세요.");
        }

        LocalDate localDate = Instant.ofEpochMilli(date).atZone(DEFAULT_ZONE).toLocalDate();

        // 해당 날짜의 시작과 끝 타임스탬프 계산
        long dayStart = localDate.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
        long dayEnd = localDate.atTime(23, 59, 59).atZone(DEFAULT_ZONE).toInstant().toEpochMilli();

        // 해당 날짜의 일정 조회
        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
                userId, dayStart, dayEnd);

        List<ScheduleResponseDTO.ScheduleDTO> result = schedules.stream()
                .map(schedule -> ScheduleResponseDTO.fromSchedule(schedule))
                .collect(Collectors.toList());

        return new SimpleResultDTO<>(result);
    }

    /// OCR로 일정 등록
    @Transactional
    public ApiResult processOcrSchedules(MultipartFile image, Integer userId, Integer year, Integer month, String timezone) {
        //사용자의 시간대 설정
        ZoneId userZone = ZoneId.of(timezone);

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
            OcrResponseDTO ocrResult;
            try {
                ocrResult = ocrFastApiClient.processImage(resizedImage);
            } catch (Exception e) {
                log.error("OCR 서버 처리 중 오류: {}", e.getMessage());
                throw new IllegalArgumentException("OCR 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            }

            if (ocrResult == null || ocrResult.getSchedules() == null || ocrResult.getSchedules().isEmpty()) {
                throw new IllegalArgumentException("인식된 일정이 없습니다. 이미지를 확인해주세요.");
            }

            // 일정 등록
            int successCount = 0;
            int failCount = 0;
            List<String> failureReasons = new ArrayList<>();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            YearMonth yearMonth = YearMonth.of(year, month);
            int lastDayOfMonth = yearMonth.lengthOfMonth();

            // 결과에서 일정 정보 추출 및 저장
            for (OcrResponseDTO.ScheduleItem item : ocrResult.getSchedules()) {
                try {
                    // 날짜 유효성 검사
                    // 1일 이전인 경우, 혹은 30일/31일을 벗어나는 경우
                    if (item.getDay() <= 0 || item.getDay() > lastDayOfMonth) {
                        failCount++;
                        failureReasons.add(String.format("날짜 오류: %d일은 %d년 %d월에 존재하지 않습니다.", item.getDay(), year, month));
                        continue;
                    }

                    // 클라이언트 이름으로 클라이언트 찾기
                    // 해당 이름의 클라이언트가 없다면 생략하고, 다음 저장을 진행함.
                    Client client;
                    try {
                        client = clientRepository.findByName(item.getClientName())
                                .orElse(null);
                        if (client == null) {
                            failCount++;
                            failureReasons.add(String.format("클라이언트 찾기 실패: '%s'", item.getClientName()));
                            continue;
                        }
                    } catch (Exception e) {
                        failCount++;
                        failureReasons.add(String.format("클라이언트 조회 오류: '%s'", item.getClientName()));
                        continue;
                    }

                    try {
                        // 날짜, 시간 파싱 (LocalDate/LocalTime으로 파싱 후 타임스탬프로 변환)
                        LocalDate localDate = LocalDate.of(year, month, item.getDay());

                        LocalTime startLocalTime = LocalTime.parse(item.getStartAt() + ":00");
                        LocalTime endLocalTime = LocalTime.parse(item.getEndAt() + ":00");

                        // 타임스탬프 변환 - 사용자 시간대 사용
                        ZonedDateTime startZdt = ZonedDateTime.of(localDate, startLocalTime, userZone);
                        ZonedDateTime endZdt = ZonedDateTime.of(localDate, endLocalTime, userZone);

                        long visitedDateTimestamp = localDate.atStartOfDay(userZone).toInstant().toEpochMilli();
                        long startAtTimestamp = startZdt.toInstant().toEpochMilli();
                        long endAtTimestamp = endZdt.toInstant().toEpochMilli();

                        // 중복 일정 확인 (수정된 레포지토리 메서드 사용)
                        boolean hasOverlap = false;
                        try {
                            // 해당 날짜의 시작과 끝 타임스탬프
                            long dayStart = localDate.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
                            long dayEnd = localDate.atTime(23, 59, 59).atZone(DEFAULT_ZONE).toInstant().toEpochMilli();

                            List<Schedule> overlappingSchedules = scheduleRepository.findByUserIdAndVisitedDateAndTimeOverlapping(
                                    userId, dayStart, dayEnd, startAtTimestamp, endAtTimestamp);
                            hasOverlap = !overlappingSchedules.isEmpty();
                        } catch (Exception e) {
                            log.warn("중복 일정 확인 중 오류: {}", e.getMessage());
                            // 중복 확인 실패해도 일정 등록은 진행
                        }

                        if (hasOverlap) {
                            failCount++;
                            failureReasons.add(String.format("중복 일정: %s일 %s~%s", item.getDay(), item.getStartAt(), item.getEndAt()));
                            continue;
                        }

                        // Schedule 객체 생성 및 저장
                        Schedule schedule = Schedule.builder()
                                .user(user)
                                .client(client)
                                .visitedDate(visitedDateTimestamp)
                                .startAt(startAtTimestamp)
                                .endAt(endAtTimestamp)
                                .build();

                        scheduleRepository.save(schedule);
                        successCount++;
                        log.info("스케줄 저장 완료 - 날짜: {}, 시작: {}, 종료: {}, 클라이언트: {}",
                                localDate, startLocalTime, endLocalTime, client.getName());
                    } catch (DateTimeParseException e) {
                        failCount++;
                        failureReasons.add(String.format("시간 형식 오류: %s일 %s~%s", item.getDay(), item.getStartAt(), item.getEndAt()));
                        continue;
                    }
                } catch (Exception e) {
                    failCount++;
                    failureReasons.add(String.format("기타 오류: %s", e.getMessage()));
                    log.error("스케줄 저장 중 오류 발생: {}", e.getMessage());
                }
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);

            if (failCount > 0) {
                resultMap.put("failureReasons", failureReasons);
            }

            return OcrDTO.OcrResultDTO.of(successCount, failCount, failCount > 0 ? failureReasons : null);
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }
}
