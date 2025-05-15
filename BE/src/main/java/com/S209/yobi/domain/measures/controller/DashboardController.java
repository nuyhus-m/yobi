package com.S209.yobi.domain.measures.controller;

import com.S209.yobi.Mapper.AuthUtils;
import com.S209.yobi.domain.measures.service.DashboardService;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthUtils authUtils;

    @Operation(summary = "단건 데이터 조회 (주요 데이터)",
            description = "건강 주요 데이터를 조회합니다(체지방률/기초대사량/체내수분/스트레스/심박/혈압)")
    @GetMapping(value = "/main/{clientId}")
    public ResponseEntity<?> getMainHealth(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int clientId
    ){
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = dashboardService.getMainHealth(userId, clientId);

        if(result instanceof  ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "단건 데이터 조회 (자세히보기)", description = "건강 데이터를 전체를 조회합니다")
    @GetMapping(value = "/detail/{clientId}")
    public ResponseEntity<?> getHealthDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int clientId
    ){
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = dashboardService.getHealthDetail(userId, clientId);

        if(result instanceof  ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "건강 추이 전체 조회", description = "건강 추이 전체를 조회합니다.")
    @GetMapping(value = "/{clientId}/total-health")
    public ResponseEntity<?> getTotalHealth(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer clientId,
            @RequestParam int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Long cursorDate
    ) {
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);

        ApiResult result = dashboardService.getTotalHealth(userId, clientId, size, cursorDate);

        if(result instanceof  ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);

    }






}
