package com.S209.yobi.measures.controller;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.exception.ApiResponseCode;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.exception.ResponseMapper;
import com.S209.yobi.measures.service.MeasureService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/health")
public class MeasureController {

    private final MeasureService measureService;

    @Operation(summary = "피트러스 필수 데이터 저장 (체성분/혈압)", description = "피트러스 필수 데이터를 저장합니다(체성분/혈압)")
    @PutMapping(value = "/base/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveBaseElement(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody BaseRequestDTO requestDTO
    ) throws IOException{

        ApiResponseDTO<?> response = measureService.saveBaseElement(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);

    }

    @Operation(summary = "피트러스 심박 측정 저장", description = "피트러스 심박 데이터를 저장합니다")
    @PutMapping(value = "/heart-rate/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveHeartRate(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody HeartRateDTO requestDTO
    ) throws IOException{
        ApiResponseDTO<?> response = measureService.saveHeartRate(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 스트레스 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PutMapping(value = "/stress/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody StressDTO requestDTO
    ) throws IOException{
        ApiResponseDTO<?> response = measureService.saveStress(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 체온 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PutMapping(value = "/temperature/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody TemperatureDTO requestDTO
    ) throws IOException{
        ApiResponseDTO<?> response = measureService.saveTemperature(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "피트러스 체성분 데이터 저장(재측정)", description = "재측정된 피트러스 체성분 데이터를 저장합니다")
    @PutMapping(value = "/body/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody ReBodyCompositionDTO requestDTO
    ) throws IOException{
        ApiResponseDTO<?> response = measureService.saveBodyComposition(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }


    @Operation(summary = "피트러스 혈압 데이터 저장(재측정)", description = "재측정된 피트러스 혈압 데이터를 저장합니다")
    @PutMapping(value = "/blood-pressure/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody ReBloodPressureDTO requestDTO
    ) throws IOException{
        ApiResponseDTO<?> response = measureService.saveBloodPressure(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }



}
