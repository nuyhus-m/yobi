package com.S209.yobi.measures.controller;

import com.S209.yobi.DTO.requestDTO.BaseRequestDTO;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.measures.service.MeasureService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<ApiResponseDTO<Void>> saveBaseElement(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int userId,
            @RequestBody BaseRequestDTO requestDTO
//            HttpServletRequest request
    ) throws IOException{

        measureService.saveBaseElement(userId, requestDTO);
        return ResponseEntity.ok(ApiResponseDTO.success(null));

    }
}
