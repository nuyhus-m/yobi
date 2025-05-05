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
//        Map<String, Object> schedule = scheduleService.getSchedule(scheduleId);
//        return ResponseEntity.ok(ApiResponseDTO.success(schedule));
//    }
//}

        log.info("===== Controller: getSchedule 요청 받음, scheduleId: {} =====", scheduleId);

        try {
            Map<String, Object> schedule = scheduleService.getSchedule(scheduleId);
            log.info("Service에서 응답 받음: {}", schedule);

            ApiResponseDTO<Map<String, Object>> response = ApiResponseDTO.success(schedule);
            log.info("최종 응답 생성 완료: {}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Controller에서 에러 발생: ", e);
            throw e;
        } finally {
            log.info("===== Controller: getSchedule 종료 =====");
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

        } catch (AccessDeniedException e) {
            log.error("권한 없음 에러: ", e);
            throw new RuntimeException("권한이 없습니다", e);
        } catch (Exception e) {
            log.error("Controller에서 에러 발생: ", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}