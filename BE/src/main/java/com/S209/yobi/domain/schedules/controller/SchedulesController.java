package com.S209.yobi.domain.schedules.controller;

import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleCreateRequestDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.S209.yobi.DTO.responseDTO.SimpleResultDTO;
import com.S209.yobi.domain.schedules.service.ScheduleService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleService scheduleService;

    @Operation(summary = "단건 일정 조회", description = "scheduleId를 넘기면 단건 일정의 정보를 조회할 수 있습니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getSchedule(
            @PathVariable Integer scheduleId
    ) {
        ApiResult result = scheduleService.getSchedule(scheduleId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "단건 일정 등록", description = "(OCR X) 단건 일정을 등록합니다.<br>" +
                    "clientId: 1<br>" +
                    "visitedDate: \"2022-05-01\"<br>" +
                    "startAt: \"09:00:00\"<br>" +
                    "endAt: \"10:00:00\"<br>" +
                    "의 형태로 request 넘기면 됩니다."
    )
    @PostMapping
    public ResponseEntity<?> createSchedule(
            @Valid @RequestBody ScheduleCreateRequestDTO requestDTO
    ) {
        ApiResult result = scheduleService.createSchedule(requestDTO);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "(수정중) 단건 일정 수정", description = "일정 정보를 수정합니다. <br/>" +
            "clientId: 112,<br/>" +
            "visitedDate: \"2025-05-03\",<br/>" +
            "startAt: \"14:00:00\",<br/>" +
            "endAt: \"15:00:00\"<br/>" +
            "의 형태로 request 요청하면 됩니다.")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody ScheduleUpdateRequestDTO requestDTO
    ) {
        ApiResult result = scheduleService.updateSchedule(scheduleId, requestDTO);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "단건 일정 삭제", description = "scheduleId에 해당하는 일정을 삭제합니다.")
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Integer scheduleId) {
        ApiResult result = scheduleService.deleteSchedule(scheduleId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "요양보호사별 일정 목록 조회", description = "현재 로그인한 요양보호사의 모든 일정을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getSchedulesByUser() {
        // 임시 하드코딩. JWT에서 추출해야 합니다!
        Integer userId = 1;

        ApiResult result = scheduleService.getSchedulesByUser(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 월의 일정 목록 조회", description = "현재 로그인한 사용자에 대해 특정 year, month에 해당하는 일정을 조회합니다.")
    @GetMapping("/month")
    public ResponseEntity<?> getSchedulesByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        // 하드코딩
        Integer userId = 1;

        ApiResult result = scheduleService.getSchedulesByMonth(userId, year, month);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정일 일정 목록 조회", description = "현재 로그인한 사용자에 대해 특정일의 일정을 조회합니다.<br/>"
    + "2025-05-04 형태로 입력하면 결과를 반환합니다.")
    @GetMapping("/day")
    public ResponseEntity<?> getSchedulesByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // 하드코딩 임시
        Integer userId = 1;

        ApiResult result = scheduleService.getSchedulesByDay(userId, date);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        // SimpleResultDTO에서 데이터만 추출해서 반환
        if (result instanceof SimpleResultDTO<?> simpleResult) {
            return ResponseEntity.ok(simpleResult.getData());
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "OCR을 이용한 일정 등록", description = "이미지, 년, 월을 등록하면 OCR로 분석 후 일정을 자동 등록합니다.")
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerScheduleByOcr(
            @RequestParam("image") MultipartFile image,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        //임시 하드코딩
        Integer userId = 1;

        ApiResult result = scheduleService.processOcrSchedules(image, userId, year, month);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}