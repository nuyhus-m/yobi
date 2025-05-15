package com.S209.yobi.domain.measures.controller;

import com.S209.yobi.DTO.responseDTO.BodyCompositionResponseDTO;
import com.S209.yobi.Mapper.AuthUtils;
import com.S209.yobi.domain.measures.service.HealthDataService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.amazonaws.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthDataController {

    private final HealthDataService healthDataService;
    private final AuthUtils authUtils;

    @Operation(summary = "체성분 데이터 조회", description = "특정 ID의 체성분 데이터를 조회합니다.")
    @GetMapping(value = "/body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체성분 데이터 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(
                                    name = "기본 응답",
                                    value = "{\n \"compositionId\": 1,\n \"bfp\": { \"value\": 31.6, \"level\": \"높음\" },\n \"bfm\": { \"value\": 21.2, \"level\": \"높음\" },\n \"smm\": { \"value\": 25.4, \"level\": \"낮음\" },\n \"bmr\": { \"value\": 1367, \"level\": \"보통\" },\n \"ecf\": { \"value\": 50.4, \"level\": \"높음\" },\n \"protein\": { \"value\": 9.2, \"level\": \"보통\" },\n \"mineral\": { \"value\": 3.3, \"level\": \"보통\" },\n \"bodyAge\": 48\n}"
                            )}))
    })
    public ResponseEntity<?> getBodyComposition(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long bodyId
            ){
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = healthDataService.getBodyComposition(userId, bodyId);
        if (result instanceof ApiResponseDTO<?> errorResult) {
            if (!errorResult.getCode().equals("200")) {
                String code = errorResult.getCode();
                HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
                return ResponseEntity.status(status).body(errorResult);
            }

            // 성공 응답인 경우 data 필드만 추출하여 반환
            return ResponseEntity.ok(errorResult.getData());
        }

        // 다른 타입의 결과인 경우 그대로 반환 (예외 케이스)
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "혈압 데이터 조회", description = "특정 ID의 혈압 데이터를 조회합니다.")
    @GetMapping(value = "/blood")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "혈압 데이터 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(
                                    name = "기본 응답",
                                    value = "{\n \"bloodId\": 1,\n \"sbp\": { \"value\": 119.8, \"level\": \"높음\" },\n \"dbp\": { \"value\": 79.8, \"level\": \"높음\" }\n}"
                            )}))
    })
    public ResponseEntity<?> getBloodPressure(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long bloodId
    ) {
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = healthDataService.getBloodPressure(userId, bloodId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();

            // 성공 코드인 경우 data만 반환
            if (code.equals("200")) {
                return ResponseEntity.ok(errorResult.getData());
            }

            // 오류 코드인 경우
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "심박 데이터 조회", description = "특정 ID의 심박 데이터를 조회합니다.")
    @GetMapping(value = "/heartRate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "심박 데이터 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(
                                    name = "기본 응답",
                                    value = "{\n \"heartId\": 1,\n \"bpm\": { \"value\": 80, \"level\": \"높음\" },\n \"oxygen\": { \"value\": 98, \"level\": \"높음\" }\n}"
                            )}))
    })
    public ResponseEntity<?> getHeartRate(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long heartRateId
    ) {
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = healthDataService.getHeartRateById(userId, heartRateId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();

            // 성공 코드인 경우 data만 반환
            if (code.equals("200")) {
                return ResponseEntity.ok(errorResult.getData());
            }

            // 오류 코드인 경우
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }



}
