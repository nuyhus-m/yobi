package com.S209.yobi.measures.controller;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.exception.ApiResponseCode;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.measures.service.MeasureService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/health")
public class MeasureController {

    private final MeasureService measureService;

    @Operation(summary = "피트러스 필수 데이터 저장 (체성분/혈압)", description = "피트러스 필수 데이터를 저장합니다(체성분/혈압)")
    @PostMapping(value = "/base/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveBaseElement(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody BaseRequestDTO requestDTO
    ){

        ApiResponseDTO<?> response = measureService.saveBaseElement(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);

    }

    @Operation(summary = "피트러스 심박 측정 저장", description = "피트러스 심박 데이터를 저장합니다")
    @PostMapping(value = "/heart-rate/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveHeartRate(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody HeartRateRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.saveHeartRate(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 스트레스 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PostMapping(value = "/stress/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody StressRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.saveStress(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 체온 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PostMapping(value = "/temperature/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody TemperatureRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.saveTemperature(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 체성분 데이터 저장(재측정)", description = "재측정된 피트러스 체성분 데이터를 저장합니다")
    @PostMapping(value = "/body/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody ReBodyRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.saveBodyComposition(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }


    @Operation(summary = "피트러스 혈압 데이터 저장(재측정)", description = "재측정된 피트러스 혈압 데이터를 저장합니다")
    @PostMapping(value = "/blood-pressure/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody ReBloodRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.saveBloodPressure(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }


    @Operation(summary = "오늘 필수 데이터 측정했는지 여부(T/F)", description = "당일 필수 데이터 측정여부를 확인합니다.")
    @PostMapping(value = "/health/check/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> checkBase(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody CheckBaseRequestDTO requestDTO
    ){
        ApiResponseDTO<?> response = measureService.checkBase(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }


    @Operation(summary = "단건 데이터 조회 (주요 데이터)",
            description = "건강 주요 데이터를 조회합니다(체지방률/기초대사량/체내수분/스트레스/심박/혈압)")
    @PostMapping(value = "/health/main/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> getMainData(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody CheckBaseRequestDTO requestDTO
    ){
        ApiResponseDTO<?>response = measureService.getMainData(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }




}
