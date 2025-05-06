package com.S209.yobi.schedules.controller;

import com.S209.yobi.DTO.requestDTO.ScheduleRequestDto;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.schedules.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleService scheduleService;

    @Operation(summary = "단건 일정 조회", description = "scheduleId를 넘기면 단건 일정의 정보를 조회할 수 있습니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getSchedule(
            @PathVariable Integer scheduleId
    ) {
        try {
            Map<String, Object> schedule = scheduleService.getSchedule(scheduleId);

            ApiResponseDTO<Map<String, Object>> response = ApiResponseDTO.success(schedule);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw e;
        }
    }

    @Operation(summary = "단건 일정 등록", description = "(OCR X) 단건 일정을 등록합니다.  \n" +
            "visitedDate: \"2022-05-01\",  \n" +
            "startAt: \"09:00:00\",  \n" +
            "endAt: \"10:00:00\"  \n" +
            "의 형태로 request 넘기면 됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> createSchedule(
            @Valid @RequestBody ScheduleRequestDto requestDto
            ) {
        try {
            scheduleService.createSchedule(requestDto);
            return ResponseEntity.ok(ApiResponseDTO.success(null));
        } catch (Exception e) {
            throw e;
        }
    }

    @Operation(summary = "단건 일정 수정", description = "일정 정보를 수정합니다. <br/>" +
            "clientId: 112,<br/>" +
            "visitedDate: \"2025-05-03\",<br/>" +
            "startAt: \"14:00:00\",<br/>" +
            "endAt: \"15:00:00\"<br/>" +
            "의 형태로 request 요청하면 됩니다.")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateSchedule(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody ScheduleRequestDto requestDto) {

        try {
            scheduleService.updateSchedule(scheduleId, requestDto);
            log.info("일정 수정 성공");

            return ResponseEntity.ok(ApiResponseDTO.success(null));

        } catch (Exception e) {
            log.error("Controller에서 에러 발생: ", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Operation(summary = "단건 일정 삭제", description = "scheduleId에 해당하는 일정을 삭제합니다.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSchedule(@PathVariable Integer scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(ApiResponseDTO.success(null));
        } catch (Exception e) {
            throw e;
        }
    }

    @Operation(summary = "요양보호사별 일정 목록 조회", description = "현재 로그인한 요양보호사의 모든 일정을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getSchedulesByUser() {
        try {
            // 임시 하드코딩. JWT에서 추출해야 합니다!
            Integer userId = 1;

            List<Map<String, Object>> schedules = scheduleService.getSchedulesByUser(userId);
            log.info("스케줄수: {}", schedules.size());
            return ResponseEntity.ok(ApiResponseDTO.success(schedules));
        } catch (Exception e) {
            throw e;
        }
    }

    @Operation(summary = "특정 월의 일정 목록 조회", description = "현재 로그인한 사용자에 대해 특정 year, month에 해당하는 일정을 조회합니다.")
    @GetMapping("/month")
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getSchedulesByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        try {
            // 입력값의 유효성 검사
            if (year < 2000 || year > 2100 || month < 1 || month >12) {
                throw new IllegalArgumentException("유효하지 않은 년월입니다.");
            }

            // 임시 하드코딩. JWT 추출 필요
            Integer userId = 1;

            List<Map<String, Object>> schedules = scheduleService.getSchedulesByMonth(userId, year, month);
            return ResponseEntity.ok(ApiResponseDTO.success(schedules));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}