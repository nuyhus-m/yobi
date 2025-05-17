package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.requestDTO.OcrDTO;
import com.S209.yobi.DTO.requestDTO.OcrDTO.OcrResponseDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleCreateRequestDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.S209.yobi.DTO.responseDTO.ScheduleResponseDTO;
import com.S209.yobi.DTO.responseDTO.SimpleResultDTO;
import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.schedules.entity.Schedule;
import com.S209.yobi.domain.schedules.repository.ScheduleRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtProvider jwtProvider;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        // JWT 토큰에서 userId 추출
        String token = authentication.getCredentials().toString();
        log.info("우라라ㅏ라ㅏ: {}", jwtProvider.extractUserId(token));
        return jwtProvider.extractUserId(token);
    }

    // 단건 일정 조회
    @Transactional(readOnly = true)
    public ApiResult getSchedule(Integer scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        return ScheduleResponseDTO.fromSchedule(schedule);
    }

    // 단건 일정 등록
    @Transactional
    public ApiResult createSchedule(Integer userId, ScheduleCreateRequestDTO requestDto) {
        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + requestDto.getClientId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (requestDto.getEndAt() < requestDto.getStartAt()) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 빠를 수 없음.");
        }

        // 일정 중복 검사
        ApiResult overlapResult = checkScheduleOverlap(userId, null, requestDto.getVisitedDate(),
                requestDto.getStartAt(), requestDto.getEndAt(), requestDto.getClientId());
        if (overlapResult instanceof ApiResponseDTO) {
            ApiResponseDTO<?> responseDTO = (ApiResponseDTO<?>) overlapResult;
            if (!"200".equals(responseDTO.getCode())) {
                return overlapResult; // 중복 일정 에러 반환
            }
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
        // 현재 인증된 사용자 ID 가져오기
        Integer currentUserId = getCurrentUserId();

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
        ApiResult overlapResult = checkScheduleOverlap(currentUserId, scheduleId, visitedDate, startAt, endAt, client.getId());
        if (overlapResult instanceof ApiResponseDTO) {
            ApiResponseDTO<?> responseDTO = (ApiResponseDTO<?>) overlapResult;
            if (!"200".equals(responseDTO.getCode())) {
                return overlapResult; // 중복 일정 에러 반환
            }
        }

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
    private ApiResult checkScheduleOverlap(Integer userId, Integer scheduleId, long visitedDateTimestamp, long startAtTimestamp, long endAtTimestamp, Integer clientId) {
        // 해당 날짜의 시작과 끝 타임스탬프 계산
        LocalDate date = Instant.ofEpochMilli(visitedDateTimestamp).atZone(DEFAULT_ZONE).toLocalDate();
        long dayStart = date.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
        long dayEnd = date.atTime(23, 59, 59).atZone(DEFAULT_ZONE).toInstant().toEpochMilli();

        // 같은 날짜에 시간이 겹치는 일정 조회
        List<Schedule> overlappingSchedules = scheduleRepository.findByUserIdAndVisitedDateWithClient(
                userId, dayStart, dayEnd);

        // 1. 같은 날짜, 같은 클라이언트 체크
        List<Schedule> sameClientSchedules = overlappingSchedules.stream()
                .filter(s -> scheduleId == null || !s.getId().equals(scheduleId))
                .filter(s -> s.getClient().getId().equals(clientId))
                .collect(Collectors.toList());

        if (!sameClientSchedules.isEmpty()) {
            log.error("같은 날짜에 같은 클라이언트 일정이 이미 존재합니다. 날짜: {}, 클라이언트ID: {}",
                    date, clientId);
            return ApiResponseDTO.fail(ApiResponseCode.DUPLICATE_DATE_CLIENT);
        }

        // 2. 시간 중복 체크 (기존 로직)
        List<Schedule> conflicts = overlappingSchedules.stream()
                .filter(s -> scheduleId == null || !s.getId().equals(scheduleId))
                .filter(s -> isTimeOverlapping(s.getStartAt(), s.getEndAt(), startAtTimestamp, endAtTimestamp))
                .collect(Collectors.toList());

        if (!conflicts.isEmpty()) {
            Schedule conflict = conflicts.get(0);
            String errorMessage = String.format("해당 시간(%s~%s)에 이미 다른 일정(%s~%s, %s님)이 있습니다.",
                    formatTimestamp(startAtTimestamp), formatTimestamp(endAtTimestamp),
                    formatTimestamp(conflict.getStartAt()), formatTimestamp(conflict.getEndAt()),
                    conflict.getClient().getName());

            log.error("일정 등록 중 에러: {}", errorMessage);
            return ApiResponseDTO.fail(ApiResponseCode.DUPLICATE_SCHEDULE_TIME);
        }

        return ApiResponseDTO.success(null);
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
        // Schedule 존재 여부 확인
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        // 현재 인증된 사용자 ID 가져오기
        Integer currentUserId = getCurrentUserId();

        // 권한 검증
        if (!schedule.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("삭제 권한이 없음.");
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
                .map(schedule -> {
                    return ScheduleResponseDTO.ScheduleDTO.builder()
                            .scheduleId(schedule.getId())
                            .clientId(schedule.getClient().getId())
                            .clientName(schedule.getClient().getName())
                            .visitedDate(schedule.getVisitedDate())
                            .startAt(schedule.getStartAt())
                            .endAt(schedule.getEndAt())
                            .hasLogContent(schedule.getLogContent() != null && !schedule.getLogContent().isEmpty()) // 로그 존재 여부
                            .build();
                })
                .collect(Collectors.toList());

        return new SimpleResultDTO<>(result);
    }

    /// OCR로 일정 등록
//    @Transactional
//    public ApiResult processOcrSchedules(MultipartFile image, Integer userId, Integer year, Integer month, String timezone) {
//        //사용자의 시간대 설정
//        ZoneId userZone = ZoneId.of(timezone);
//
//        //이미지 유효성 검사
//        if (image == null || image.isEmpty()) {
//            throw new IllegalArgumentException("이미지 파일이 없음.");
//        }
//
//        //년월 유효성 검사
//        if (year < 2000 || year > 2100 || month < 1 || month > 12) {
//            throw new IllegalArgumentException("유효하지 않은 년월입니다.");
//        }
//
//        try {
//            // 이미지 리사이징 처리
//            MultipartFile resizedImage = imageResizeService.resizeImageIfNeeded(image);
//            log.info("이미지 크기: 원본 {}KB -> 변환 후 {}KB",
//                    image.getSize() / 1024,
//                    resizedImage.getSize() / 1024);
//
//            // FastAPI 서버에 리사이징된 이미지 전송
//            OcrResponseDTO ocrResult;
//            try {
//                ocrResult = ocrFastApiClient.processImage(resizedImage);
//            } catch (Exception e) {
//                log.error("OCR 서버 처리 중 오류: {}", e.getMessage());
//                throw new IllegalArgumentException("OCR 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
//            }
//
//            if (ocrResult == null || ocrResult.getSchedules() == null || ocrResult.getSchedules().isEmpty()) {
//                throw new IllegalArgumentException("인식된 일정이 없습니다. 이미지를 확인해주세요.");
//            }
//
//            Boolean formMatch = ocrResult.getFormMatch();
//            if (formMatch != null && !formMatch) {
//                log.warn("달력과 실제 요일이 일치하지 않습니다.");
//            }
//
//            // 일정 등록
//            int successCount = 0;
//            int failCount = 0;
//            List<String> failureReasons = new ArrayList<>();
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//            YearMonth yearMonth = YearMonth.of(year, month);
//            int lastDayOfMonth = yearMonth.lengthOfMonth();
//
//            // 결과에서 일정 정보 추출 및 저장
//            for (OcrResponseDTO.ScheduleItem item : ocrResult.getSchedules()) {
//                try {
//                    // 날짜 유효성 검사
//                    // 1일 이전인 경우, 혹은 30일/31일을 벗어나는 경우
//                    if (item.getDay() <= 0 || item.getDay() > lastDayOfMonth) {
//                        failCount++;
//                        failureReasons.add(String.format("날짜 오류: %d일은 %d년 %d월에 존재하지 않습니다.", item.getDay(), year, month));
//                        continue;
//                    }
//
//                    // 클라이언트 이름으로 클라이언트 찾기
//                    // 해당 이름의 클라이언트가 없다면 생략하고, 다음 저장을 진행함.
//                    Client client;
//                    try {
//                        client = clientRepository.findByName(item.getClientName())
//                                .orElse(null);
//                        if (client == null) {
//                            failCount++;
//                            failureReasons.add(String.format("클라이언트 찾기 실패: '%s'", item.getClientName()));
//                            continue;
//                        }
//                    } catch (Exception e) {
//                        failCount++;
//                        failureReasons.add(String.format("클라이언트 조회 오류: '%s'", item.getClientName()));
//                        continue;
//                    }
//
//                    try {
//                        // 날짜, 시간 파싱 (LocalDate/LocalTime으로 파싱 후 타임스탬프로 변환)
//                        LocalDate localDate = LocalDate.of(year, month, item.getDay());
//
//                        LocalTime startLocalTime = LocalTime.parse(item.getStartAt() + ":00");
//                        LocalTime endLocalTime = LocalTime.parse(item.getEndAt() + ":00");
//
//                        // 타임스탬프 변환 - 사용자 시간대 사용
//                        ZonedDateTime startZdt = ZonedDateTime.of(localDate, startLocalTime, userZone);
//                        ZonedDateTime endZdt = ZonedDateTime.of(localDate, endLocalTime, userZone);
//
//                        long visitedDateTimestamp = localDate.atStartOfDay(userZone).toInstant().toEpochMilli();
//                        long startAtTimestamp = startZdt.toInstant().toEpochMilli();
//                        long endAtTimestamp = endZdt.toInstant().toEpochMilli();
//
//                        // 중복 일정 확인
//                        ApiResult overlapResult = checkScheduleOverlap(userId, null, visitedDateTimestamp,
//                                startAtTimestamp, endAtTimestamp, client.getId());
//                        if (overlapResult instanceof ApiResponseDTO) {
//                            ApiResponseDTO<?> responseDTO = (ApiResponseDTO<?>) overlapResult;
//                            if (!"200".equals(responseDTO.getCode())) {
//                                // 중복 일정이 있는 경우
//                                failCount++;
//                                String reason;
//                                if (responseDTO.getCode().equals(ApiResponseCode.DUPLICATE_DATE_CLIENT.getCode())) {
//                                    reason = String.format("같은 날짜에 같은 클라이언트 일정 중복: %s일 %s님",
//                                            item.getDay(), item.getClientName());
//                                } else {
//                                    reason = String.format("시간 중복 일정: %s일 %s~%s",
//                                            item.getDay(), item.getStartAt(), item.getEndAt());
//                                }
//                                failureReasons.add(reason);
//                                continue;
//                            }
//                        }
//
//                        // Schedule 객체 생성 및 저장
//                        Schedule schedule = Schedule.builder()
//                                .user(user)
//                                .client(client)
//                                .visitedDate(visitedDateTimestamp)
//                                .startAt(startAtTimestamp)
//                                .endAt(endAtTimestamp)
//                                .build();
//
//                        scheduleRepository.save(schedule);
//                        successCount++;
//                        log.info("스케줄 저장 완료 - 날짜: {}, 시작: {}, 종료: {}, 클라이언트: {}",
//                                localDate, startLocalTime, endLocalTime, client.getName());
//                    } catch (DateTimeParseException e) {
//                        failCount++;
//                        failureReasons.add(String.format("시간 형식 오류: %s일 %s~%s", item.getDay(), item.getStartAt(), item.getEndAt()));
//                        continue;
//                    }
//                } catch (Exception e) {
//                    failCount++;
//                    failureReasons.add(String.format("기타 오류: %s", e.getMessage()));
//                    log.error("스케줄 저장 중 오류 발생: {}", e.getMessage());
//                }
//            }
//
//            Map<String, Object> resultMap = new HashMap<>();
//            resultMap.put("successCount", successCount);
//            resultMap.put("failCount", failCount);
//
//            if (failCount > 0) {
//                resultMap.put("failureReasons", failureReasons);
//            }
//
//            return OcrDTO.OcrResultDTO.of(successCount, failCount, failCount > 0 ? failureReasons : null, formMatch);
//        } catch (IOException e) {
//            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
//        }
//    }

    // 특정 기간의 일정 리스트
    @Transactional(readOnly = true)
    public ApiResult getSchedulesByPeriod(Integer userId, long startDate, long endDate) {
        if (startDate <= 0 || endDate <= 0) {
            return ApiResponseDTO.fail(ApiResponseCode.PERIOD_NO_INPUT);
        }

        if (endDate < startDate) {
            return ApiResponseDTO.fail(ApiResponseCode.START_END_ERROR);
        }

        List<Schedule> schedules = scheduleRepository.findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
                userId, startDate, endDate
        );

        return ScheduleResponseDTO.fromList(schedules);
    }






    // 1. OCR 분석만 수행하는 메서드 (DB 저장 X)
    @Transactional(readOnly = true)
    public ApiResult analyzeSchedulesWithOcr(MultipartFile image, Integer year, Integer month, String timezone) {
        // 사용자의 시간대 설정
        ZoneId userZone = ZoneId.of(timezone);

        // 이미지 유효성 검사
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없음.");
        }

        // 년월 유효성 검사
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
            // FastApiResponseDTO 사용 (내부 통신용)
            OcrDTO.FastApiResponseDTO fastApiResponse;
            try {
                fastApiResponse = ocrFastApiClient.processImage(resizedImage);
            } catch (Exception e) {
                log.error("OCR 서버 처리 중 오류: {}", e.getMessage());
                throw new IllegalArgumentException("OCR 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            }

            if (fastApiResponse == null || fastApiResponse.getSchedules() == null || fastApiResponse.getSchedules().isEmpty()) {
                throw new IllegalArgumentException("인식된 일정이 없습니다. 이미지를 확인해주세요.");
            }

            // FastAPI에서 받은 whichDay (1일이 있는 열 번호)
            Integer whichDay = fastApiResponse.getWhichDay();

            // whichDay가 null이면 기본값 설정 (FastAPI에서 받아오지 못한 경우)
            if (whichDay == null) {
                log.warn("FastAPI에서 whichDay 값을 받아오지 못했습니다. 기본값 1(월요일)로 설정합니다.");
                whichDay = 1; // 기본값: 월요일
            }

            // 1일의 요일 로깅
            String dayName = getDayName(whichDay);
            log.info("달력에서 1일은 {}열에 위치: {}", whichDay, dayName);

            // 실제 1일의 요일 계산
            LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
            int actualWeekday = firstDayOfMonth.getDayOfWeek().getValue(); // 1(월요일) ~ 7(일요일)

            // 일요일은 Java에서 7이지만, 우리 시스템에서는 0으로 설정
            if (actualWeekday == 7) {
                actualWeekday = 0;
            }

            String actualDayName = getDayName(actualWeekday);
            log.info("실제 {}년 {}월 1일은 {}", year, month, actualDayName);

            // 요일 비교
            boolean formMatch = (whichDay == actualWeekday);
            log.info("달력 형식 일치 여부: {}", formMatch);

            // 새로운 OcrResponseDTO 생성 (타임스탬프만 포함)
            OcrResponseDTO convertedResponse = new OcrResponseDTO();
            convertedResponse.setFormMatch(formMatch);
            convertedResponse.setWhichDay(whichDay);

            // FastAPI 응답을 타임스탬프로 변환
            List<OcrResponseDTO.ScheduleItem> convertedSchedules = new ArrayList<>();

            for (OcrDTO.FastApiResponseDTO.FastApiScheduleItem rawItem : fastApiResponse.getSchedules()) {
                try {
                    // FastAPI에서 받은 값 추출
                    Integer day = rawItem.getDay();
                    String startAtStr = rawItem.getStartAt();
                    String endAtStr = rawItem.getEndAt();
                    String clientName = rawItem.getClientName();

                    // 날짜, 시간 생성
                    LocalDate localDate = LocalDate.of(year, month, day);
                    LocalTime startLocalTime = LocalTime.parse(startAtStr + ":00");
                    LocalTime endLocalTime = LocalTime.parse(endAtStr + ":00");

                    // 타임스탬프 변환
                    long dateTimestamp = localDate.atStartOfDay(userZone).toInstant().toEpochMilli();
                    long startTimestamp = ZonedDateTime.of(localDate, startLocalTime, userZone).toInstant().toEpochMilli();
                    long endTimestamp = ZonedDateTime.of(localDate, endLocalTime, userZone).toInstant().toEpochMilli();

                    // 새 ScheduleItem 생성 (타임스탬프만 포함)
                    OcrResponseDTO.ScheduleItem convertedItem = new OcrResponseDTO.ScheduleItem();
                    convertedItem.setDateTimestamp(dateTimestamp);
                    convertedItem.setStartTimestamp(startTimestamp);
                    convertedItem.setEndTimestamp(endTimestamp);
                    convertedItem.setClientName(clientName);

                    convertedSchedules.add(convertedItem);

                    log.info("타임스탬프 변환 완료: {}일 {}~{} -> {}, {}, {}",
                            day, startAtStr, endAtStr,
                            dateTimestamp, startTimestamp, endTimestamp);
                } catch (Exception e) {
                    log.error("타임스탬프 변환 중 오류: {}일 {}~{}, 오류: {}",
                            rawItem.getDay(), rawItem.getStartAt(), rawItem.getEndAt(), e.getMessage());
                }
            }

            // 변환된 일정 설정
            convertedResponse.setSchedules(convertedSchedules);

            return convertedResponse;
        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }

    // 요일명 가져오기 헬퍼 메서드
    private String getDayName(int dayIndex) {
        switch (dayIndex) {
            case 0: return "일요일";
            case 1: return "월요일";
            case 2: return "화요일";
            case 3: return "수요일";
            case 4: return "목요일";
            case 5: return "금요일";
            case 6: return "토요일";
            default: return "알 수 없음";
        }
    }

    // 2. OCR 결과를 DB에 저장하는 새로운 메서드
    @Transactional
    public ApiResult saveOcrSchedules(Integer userId, List<OcrResponseDTO.ScheduleItem> schedules) {

        // 파라미터 유효성 검사
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException("저장할 일정이 없습니다.");
        }

        // 일정 등록
        int successCount = 0;
        int failCount = 0;
        List<String> failureReasons = new ArrayList<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 결과에서 일정 정보 추출 및 저장
        for (OcrResponseDTO.ScheduleItem item : schedules) {
            try {
                // 전달된 타임스탬프 정보 추출
                Long dateTimestamp = item.getDateTimestamp();
                Long startTimestamp = item.getStartTimestamp();
                Long endTimestamp = item.getEndTimestamp();
                String clientName = item.getClientName();

                // 타임스탬프 유효성 검사
                if (dateTimestamp == null || startTimestamp == null || endTimestamp == null) {
                    failCount++;
                    failureReasons.add(String.format("타임스탬프 누락: '%s'", clientName));
                    continue;
                }

                if (startTimestamp >= endTimestamp) {
                    failCount++;
                    failureReasons.add(String.format("시간 오류: '%s' 시작 시간이 종료 시간보다 늦거나 같습니다.", clientName));
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

                // 일정 중복 검사
                ApiResult overlapResult = checkScheduleOverlap(userId, null, dateTimestamp,
                        startTimestamp, endTimestamp, client.getId());
                if (overlapResult instanceof ApiResponseDTO) {
                    ApiResponseDTO<?> responseDTO = (ApiResponseDTO<?>) overlapResult;
                    if (!"200".equals(responseDTO.getCode())) {
                        // 중복 일정이 있는 경우
                        failCount++;
                        String reason;

                        if (responseDTO.getCode().equals(ApiResponseCode.DUPLICATE_DATE_CLIENT.getCode())) {
                            reason = String.format("같은 날짜에 같은 클라이언트 일정 중복: %s님 (%s)",
                                    clientName, dateTimestamp);
                        } else {
                            reason = String.format("시간 중복 일정: %s님 (%s %s~%s)",
                                    clientName, dateTimestamp, startTimestamp, endTimestamp);
                        }
                        failureReasons.add(reason);
                        continue;
                    }
                }

                    // Schedule 객체 생성 및 저장
                    Schedule schedule = Schedule.builder()
                            .user(user)
                            .client(client)
                            .visitedDate(dateTimestamp)
                            .startAt(startTimestamp)
                            .endAt(endTimestamp)
                            .build();

                scheduleRepository.save(schedule);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failureReasons.add(String.format("기타 오류: %s", e.getMessage()));
                log.error("스케줄 저장 중 오류 발생: {}", e.getMessage());
            }
        }

        // 저장 결과 반환
        Boolean formMatch = null; // 분석 메서드에서 설정된 경우 가져올 수 있음
        return OcrDTO.OcrResultDTO.of(successCount, failCount, failCount > 0 ? failureReasons : null, formMatch);
    }

//    // 기존 메서드는 새 메서드들을 사용하는 방식으로 유지 (하위 호환성)
//    @Transactional
//    public ApiResult processOcrSchedules(MultipartFile image, Integer userId, Integer year, Integer month, String timezone) {
//        // OCR 분석 수행
//        ApiResult analyzeResult = analyzeSchedulesWithOcr(image, year, month, timezone);
//
//        // OCR 결과가 OcrResponseDTO 타입인지 확인
//        if (!(analyzeResult instanceof OcrResponseDTO)) {
//            throw new RuntimeException("OCR 분석 결과가 올바른 형식이 아닙니다.");
//        }
//
//        // DB 저장 수행
//        OcrResponseDTO ocrResult = (OcrResponseDTO) analyzeResult;
//        return saveOcrSchedules(userId, ocrResult, year, month, timezone);
//    }
}
