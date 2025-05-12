package com.S209.yobi.domain.report.controller;

import com.S209.yobi.domain.report.service.ReportService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;


    @Operation(summary = "주간 보고서 리스트 불러오기", description = "주간 보고서 리스트를 조회합니다.")
    @GetMapping(value = "/{clientId}/{userId}")
    public ResponseEntity<?> getReportList(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId
    ){
        ApiResult result = reportService.getReportList(userId, clientId);

        if(result instanceof  ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "주간 보고서 단건 조회", description = "주간 보고서 단건을 조회합니다.")
    @GetMapping(value = "/detail/{reportId}/{userId}")
    public ResponseEntity<?> getReportDetail(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long reportId,
            @PathVariable Integer userId
    ){
        ApiResult result = reportService.getReportDetail(userId, reportId);

        if(result instanceof  ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

}
