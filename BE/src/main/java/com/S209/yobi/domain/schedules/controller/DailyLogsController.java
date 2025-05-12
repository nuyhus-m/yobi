package com.S209.yobi.domain.schedules.controller;

import com.S209.yobi.DTO.responseDTO.DailyLogResponseDTO;
import com.S209.yobi.domain.schedules.service.DailyLogService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "일지 작성 및 수정", description = "scheduleId에 해당하는 log_content를 기입합니다. 성공시 null을 반환합니다.")
    @PatchMapping("/{scheduleId}/update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일지 작성/수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "null")))
    })
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

    @Operation(summary = "일지 삭제", description = "scheduleId에 해당하는 log_content를 삭제합니다. 성공시 null을 반환합니다.")
    @PatchMapping("/{scheduleId}/delete")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일지 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "null")))
    })
    public ResponseEntity<?> deleteDailyLog(
            @PathVariable Integer scheduleId
    ) {
        ApiResult result = dailyLogService.deleteDailyLog(scheduleId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "사용자의 일지 전체 리스트", description = "사용자의 일지 전체 리스트를 위한 scheduleId, client_name, visited_date를 반환합니다.")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 일지 리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[{\"scheduleId\":95,\"clientName\":\"홍길동\",\"visitedDateTimestamp\":1735660800000},{\"scheduleId\":117,\"clientName\":\"홍길동\",\"visitedDateTimestamp\":1735833600000}]")))
    })
    public ResponseEntity<?> getDailyLogsByUser() {
        //하드코딩
        Integer userId = 1;

        ApiResult result = dailyLogService.getDailyLogsByUser(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        if (result instanceof DailyLogResponseDTO responseDTO) {
            return ResponseEntity.ok(responseDTO.getDailyLogs());
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 돌봄 대상에 대한 일지 리스트", description = "사용자의 일지 전체 리스트를 위한 scheduleId, client_name, visited_date를 반환합니다.")
    @GetMapping("/client/{clientId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "클라이언트별 일지 리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[{\"scheduleId\":95,\"clientName\":\"김철수\",\"visitedDateTimestamp\":1735660800000},{\"scheduleId\":117,\"clientName\":\"김철수\",\"visitedDateTimestamp\":1735833600000}]")))
    })
    public ResponseEntity<?> getDailyLogsByClient(
            @PathVariable Integer clientId
    ) {
        //하드코딩
        Integer userId = 1;

        ApiResult result = dailyLogService.getDailyLogsByClient(userId, clientId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        if (result instanceof DailyLogResponseDTO responseDTO) {
            return ResponseEntity.ok(responseDTO.getDailyLogs());
        }

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "일지 단건 조회", description = "scheduleId에 해당하는 일지를 조회합니다.")
    @GetMapping("/{scheduleId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일지 상세 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"logContent\":\"오늘 홍길동 어르신은 혈압이 안정적이었습니다. 식사도 잘 하셨고 산책도 다녀왔습니다.\",\"clientName\":\"홍길동\",\"visitedDateTimestamp\":1735660800000}")))
    })
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
