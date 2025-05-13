package com.S209.yobi.domain.clients.controller;

import com.S209.yobi.DTO.requestDTO.ClientRequestDTO;
import com.S209.yobi.DTO.responseDTO.ClientResponseDTO;
import com.S209.yobi.domain.clients.service.ClientService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "client 등록", description = "서비스에서 직접 구현하는 기능은 아닙니다.")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "고객 등록 성공",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> createClient(
            @Parameter(description = "고객 이름") @RequestParam("name") String name,
            @Parameter(description = "생년월일") @RequestParam("birth") LocalDate birth,
            @Parameter(description = "성별") @RequestParam("gender") Integer gender,
            @Parameter(description = "키") @RequestParam("height") Double height,
            @Parameter(description = "몸무게") @RequestParam("weight") Double weight,
            @Parameter(description = "주소") @RequestParam("address") String address,
            @Parameter(description = "프로필 이미지", content = @Content(mediaType = "multipart/form-data"))
            @RequestPart(value = "image", required = false) MultipartFile image) {
        // 요청 파라미터로 ClientRequestDTO 객체 생성
        //userID 하드코딩
        ClientRequestDTO requestDTO = ClientRequestDTO.builder()
                .userId(1)
                .name(name)
                .birth(birth)
                .gender(gender)
                .height(height)
                .weight(weight)
                .address(address)
                .image(image)
                .build();

        ApiResult result = clientService.createClient(requestDTO);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 요양보호사의 돌봄 대상 리스트", description = "로그인한 사용자의 client 목록을 반환합니다.")
    @GetMapping("/list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "client 리스트 조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[\n" +
                                    "  {\n" +
                                    "    \"id\": 1,\n" +
                                    "    \"name\": \"홍길동\",\n" +
                                    "    \"birth\": \"1960-08-09\",\n" +
                                    "    \"gender\": 0,\n" +
                                    "    \"height\": 170,\n" +
                                    "    \"weight\": 70,\n" +
                                    "    \"image\": null,\n" +
                                    "    \"address\": \"서울시 강남구\"\n" +
                                    "  },\n" +
                                    "  {\n" +
                                    "    \"id\": 2,\n" +
                                    "    \"name\": \"김철수\",\n" +
                                    "    \"birth\": \"1950-08-09\",\n" +
                                    "    \"gender\": 0,\n" +
                                    "    \"height\": 175,\n" +
                                    "    \"weight\": 75,\n" +
                                    "    \"image\": null,\n" +
                                    "    \"address\": \"서울시 서초구\"\n" +
                                    "  }\n" +
                                    "]")))
    })
    public ResponseEntity<?> getClientsList() {
        Integer userId = 1;
        ApiResult result = clientService.getClientsList(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        if (result instanceof ClientResponseDTO clientResponseDTO) {
            return ResponseEntity.ok(clientResponseDTO.getClients());
        }

        return ResponseEntity.ok(result);
    }


    @Operation(summary = "특정 요양보호사의 특정 돌봄 대상 상세보기", description = "cliendId에 해당하는 client의 상세 정보를 반환합니다.")
    @GetMapping("/detail/{clientId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "client 불러오기 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"id\": 3, \"name\": \"박지성\", \"birth\": \"1960-04-03\", \"gender\": 0, \"height\": 160, \"weight\": 60, \"image\": \"https://s209-yobi.s3.ap-southeast-2.amazonaws.com/8e4f956b-cb6e-4845-bc1a-ffa78a740949.jpg\", \"address\": \"경상북도 구미시 주소\" }")
                    )
            )
    })
    public ResponseEntity<?> getClientDetail(@PathVariable Integer clientId) {
        ApiResult result = clientService.getClientDetail(clientId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}