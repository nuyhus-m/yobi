package com.S209.yobi.domain.schedules.controller;

import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleCreateRequestDTO;
import com.S209.yobi.DTO.requestDTO.ScheduleRequestDTO.ScheduleUpdateRequestDTO;
import com.S209.yobi.DTO.responseDTO.ScheduleResponseDTO;
import com.S209.yobi.DTO.responseDTO.SimpleResultDTO;
import com.S209.yobi.Mapper.AuthUtils;
import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.schedules.service.ScheduleService;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleService scheduleService;
    private final AuthUtils authUtils;

    @Operation(summary = "단건 일정 조회", description = "scheduleId를 넘기면 단건 일정의 정보를 조회할 수 있습니다.")
    @GetMapping("/{scheduleId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDTO.ScheduleDTO.class),
                            examples = @ExampleObject(value = "{\"scheduleId\":95,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1742569200000,\"startAt\":1742605200000,\"endAt\":1742616000000}")))
    })
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

    @Operation(summary = "단건 일정 등록", description = "(OCR X) 단건 일정을 등록합니다.")
    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 등록 성공",
                    content = @Content(mediaType = "application/json"))
    })
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


    @Operation(summary = "단건 일정 수정", description = "일정 정보를 수정합니다.")
    @PatchMapping("/{scheduleId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 수정 성공",
                    content = @Content(mediaType = "application/json"))
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 삭제 성공",
                    content = @Content(mediaType = "application/json"))
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDTO.class),
                            examples = @ExampleObject(value = "[{\"scheduleId\":95,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1735657200000,\"startAt\":1735693200000,\"endAt\":1735704000000},{\"scheduleId\":117,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1735657200000,\"startAt\":1735693200000,\"endAt\":1735704000000}]")))
    })
    public ResponseEntity<?> getSchedulesByUser(
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        Integer userId = userDetails != null ?
                authUtils.getUserIdFromUserDetails(userDetails) :
                authUtils.getCurrentUserId();

        ApiResult result = scheduleService.getSchedulesByUser(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        // ScheduleResponseDTO에서 schedules 배열만 추출하여 반환
        if (result instanceof ScheduleResponseDTO responseDTO) {
            return ResponseEntity.ok(responseDTO.getSchedules());
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 월의 일정 목록 조회", description = "현재 로그인한 사용자에 대해 특정 year, month에 해당하는 일정을 조회합니다.")
    @GetMapping("/month")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "월별 일정 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDTO.class),
                            examples = @ExampleObject(value = "[{\"scheduleId\":95,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1746025200000,\"startAt\":1746057600000,\"endAt\":1746061200000},{\"scheduleId\":117,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1746111600000,\"startAt\":1746144000000,\"endAt\":1746151200000}]")))
    })
    public ResponseEntity<?> getSchedulesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = userDetails != null ?
                authUtils.getUserIdFromUserDetails(userDetails) :
                authUtils.getCurrentUserId();

        ApiResult result = scheduleService.getSchedulesByMonth(userId, year, month);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        // ScheduleResponseDTO에서 schedules 배열만 추출하여 반환
        if (result instanceof ScheduleResponseDTO responseDTO) {
            return ResponseEntity.ok(responseDTO.getSchedules());
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정일 일정 목록 조회", description = "현재 로그인한 사용자에 대해 특정일의 일정을 조회합니다.")
    @GetMapping("/day")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일별 일정 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[{\"scheduleId\":65,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1746025200000,\"startAt\":1746057600000,\"endAt\":1746061200000, \"hasLogContent\": \"false\"},{\"scheduleId\":109,\"clientId\":1,\"clientName\":\"이영희\",\"visitedDate\":1746025200000,\"startAt\":1746057600000,\"endAt\":1746061200000, \"hasLogContent\": \"false\"}]")))
    })
    public ResponseEntity<?> getSchedulesByDay(
            @RequestParam long date,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = userDetails != null ?
                authUtils.getUserIdFromUserDetails(userDetails) :
                authUtils.getCurrentUserId();

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

    @Operation(summary = "OCR을 이용한 일정 등록", description = "이미지, 년, 월을 등록하면 OCR로 분석 후 일정을 자동 등록합니다. <br/>" +
            "등록 성공한 일정 수(successCount), <br/>" +
            "양식 오류, 일치 클라이언트 없음 등으로 등록 실패한 일정 수(failCount), <br/>" +
            "등록 실패 사유(failureReasons)를 반환합니다.")
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OCR 일정 등록 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"successCount\":3,\"failCount\":2,\"failureReasons\":[\"클라이언트 찾기 실패: '김영희'\",\"시간 형식 오류: 15일 9:00~8:00\"]}")))
    })
    public ResponseEntity<?> registerScheduleByOcr(
            @RequestParam("image") MultipartFile image,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam(value = "timezone", defaultValue = "Asia/Seoul") String timezone,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = userDetails != null ?
                authUtils.getUserIdFromUserDetails(userDetails) :
                authUtils.getCurrentUserId();

        ApiResult result = scheduleService.processOcrSchedules(image, userId, year, month, timezone);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 기간의 일정 목록 조회", description = "현재 로그인한 사용자에 대해 시작일부터 종료일까지(시작일, 종료일 포함)의 일정을 조회합니다.")
    @GetMapping("/period")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기간별 일정 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleResponseDTO.class),
                            examples = @ExampleObject(value = "[{\"scheduleId\":95,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1746025200000,\"startAt\":1746057600000,\"endAt\":1746061200000},{\"scheduleId\":117,\"clientId\":1,\"clientName\":\"홍길동\",\"visitedDate\":1746111600000,\"startAt\":1746144000000,\"endAt\":1746151200000}]")))
    })
    public ResponseEntity<?> getSchedulesByPeriod(
            @RequestParam long startDate,
            @RequestParam long endDate,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Integer userId = userDetails != null ?
                authUtils.getUserIdFromUserDetails(userDetails) :
                authUtils.getCurrentUserId();

        ApiResult result = scheduleService.getSchedulesByPeriod(userId, startDate, endDate);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        if (result instanceof ScheduleResponseDTO responseDTO) {
            return ResponseEntity.ok(responseDTO.getSchedules());
        }

        return ResponseEntity.ok(result);
    }

}