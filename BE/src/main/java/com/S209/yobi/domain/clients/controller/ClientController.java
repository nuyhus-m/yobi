package com.S209.yobi.domain.clients.controller;

import com.S209.yobi.DTO.requestDTO.ClientRequestDTO;
import com.S209.yobi.domain.clients.service.ClientService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "고객 정보 등록", description = "새로운 고객 정보를 등록합니다. 프로필 이미지는 S3에 저장됩니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "고객 등록 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> createClient(
            @Valid @RequestPart("clientData") ClientRequestDTO requestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 이미지가 전달된 경우에만 DTO에 설정
        if (image != null && !image.isEmpty()) {
            requestDTO = ClientRequestDTO.builder()
                    .name(requestDTO.getName())
                    .birth(requestDTO.getBirth())
                    .gender(requestDTO.getGender())
                    .height(requestDTO.getHeight())
                    .weight(requestDTO.getWeight())
                    .address(requestDTO.getAddress())
                    .image(image)
                    .build();
        }

        ApiResult result = clientService.createClient(requestDTO);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}