package com.S209.yobi.domain.schedules.controller;

import com.S209.yobi.domain.schedules.service.DailyLogService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dailylogs")
@RequiredArgsConstructor
public class DailyLogsController {
    private final DailyLogService dailyLogService;

    @Operation(summary = "일지 작성 및 수정", description = "scheduleId에 해당하는 log_content를 기입합니다.")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<?> updateDailyLog(
            @PathVariable Integer scheduleId,
            @Valid @RequestBody String content
    ) {
        ApiResult result = dailyLogService.updateDailyLog(scheduleId, content);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "일지 단건 조회", description = "scheduleId에 해당하는 일지를 조회합니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getDailyLog(
            @PathVariable Integer scheduleId
    ) {
        ApiResult result = dailyLogService.getDailyLog(scheduleId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

}
