package com.S209.yobi.domain.measures.controller;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.domain.measures.service.MeasureService;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/health")
public class MeasureController {

    private final MeasureService measureService;

    @Operation(summary = "피트러스 필수 데이터 저장 (체성분/혈압)", description = "피트러스 필수 데이터를 저장합니다(체성분/혈압)")
    @PostMapping(value = "/base/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "필수 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> saveBaseElement(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody BaseRequestDTO requestDTO
    ){

        ApiResult result = measureService.saveBaseElement(userId, clientId, requestDTO);
        if(result instanceof  com.S209.yobi.exceptionFinal.ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/base/" + clientId + userId);
        return ResponseEntity.created(location).build();

    }

    @Operation(summary = "피트러스 심박 측정 저장", description = "피트러스 심박 데이터를 저장합니다")
    @PostMapping(value = "/heart-rate/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "심박 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public  ResponseEntity<?> saveHeartRate(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody HeartRateRequestDTO requestDTO
    ){
        ApiResult result = measureService.saveHeartRate(userId, clientId, requestDTO);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/heart-rate/" + clientId + userId);
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "피트러스 스트레스 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PostMapping(value = "/stress/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "스트레스 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody StressRequestDTO requestDTO
    ){
        ApiResult result = measureService.saveStress(userId, clientId, requestDTO);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/stress/" + clientId + userId);
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "피트러스 체온 데이터 저장", description = "피트러스 스트레스 데이터를 저장합니다")
    @PostMapping(value = "/temperature/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "체온 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody TemperatureRequestDTO requestDTO
    ){
        ApiResult result = measureService.saveTemperature(userId, clientId, requestDTO);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/temperature/"+ clientId + userId);
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "피트러스 체성분 데이터 저장(재측정)", description = "재측정된 피트러스 체성분 데이터를 저장합니다")
    @PostMapping(value = "/body/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "체성분 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody ReBodyRequestDTO requestDTO
    ){
        ApiResult result = measureService.saveBodyComposition(userId, clientId, requestDTO);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/body/" + clientId + userId);
        return ResponseEntity.created(location).build();
    }


    @Operation(summary = "피트러스 혈압 데이터 저장(재측정)", description = "재측정된 피트러스 혈압 데이터를 저장합니다")
    @PostMapping(value = "/blood-pressure/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "혈압 데이터 저장 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> saveStress(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId,
            @RequestBody ReBloodRequestDTO requestDTO
    ){
        ApiResult result = measureService.saveBloodPressure(userId, clientId, requestDTO);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        URI location = URI.create("/blood-pressure/" + clientId + userId);
        return ResponseEntity.created(location).build();
    }


    @Operation(summary = "오늘 필수 데이터 측정했는지 여부(T/F)", description = "당일 필수 데이터 측정여부를 확인합니다.")
    @GetMapping(value = "/check/{clientId}/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "측정 여부 확인 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"measured\":true}")))
    })
    public ResponseEntity<?>  checkBase(
//            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable int clientId,
            @PathVariable int userId
    ){
        ApiResult result = measureService.checkBase(userId, clientId);
        if(result instanceof ApiResponseDTO<?> errorResult){
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }




}


