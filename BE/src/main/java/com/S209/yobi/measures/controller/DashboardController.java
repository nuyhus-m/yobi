package com.S209.yobi.measures.controller;

import com.S209.yobi.DTO.requestDTO.CheckBaseRequestDTO;
import com.S209.yobi.exception.ApiResponseCode;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.measures.service.DashboardService;
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
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "단건 데이터 조회 (주요 데이터)",
            description = "건강 주요 데이터를 조회합니다(체지방률/기초대사량/체내수분/스트레스/심박/혈압)")
    @PostMapping(value = "/main/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> getMainHealth(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody CheckBaseRequestDTO requestDTO
    ){
        ApiResponseDTO<?>response = dashboardService.getMainHealth(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "단건 데이터 조회 (자세히보기)",
            description = "건강 데이터를 전체를 조회합니다")
    @PostMapping(value = "/detail/{userId}")
    public ResponseEntity<ApiResponseDTO<?>> getHealthDetail(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody CheckBaseRequestDTO requestDTO
    ){
        ApiResponseDTO<?>response = dashboardService.getHealthDetail(userId, requestDTO);
        HttpStatus status = ApiResponseCode.fromCode(response.getCode()).getHttpStatus();
        return ResponseEntity.status(status).body(response);
    }

}
