package com.S209.yobi.domain.users.controller;

import com.S209.yobi.DTO.requestDTO.LoginRequestDTO;
import com.S209.yobi.DTO.requestDTO.PasswordRequestDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.requestDTO.SignUpRequest;
import com.S209.yobi.domain.users.service.UserService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 회원가입", description = "이름, ")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Void> signUp(
            @Parameter(description = "사용자 이름") @RequestParam("name") String name,
            @Parameter(description = "사원 번호") @RequestParam("employeeNumber") Integer employeeNumber,
            @Parameter(description = "비밀번호") @RequestParam("password") String password,
            @Parameter(description = "프로필 이미지", content = @Content(mediaType = "multipart/form-data"))
            @RequestPart(value = "image", required = false) MultipartFile image) {

        SignUpRequest request = SignUpRequest.builder()
                .name(name)
                .employeeNumber(employeeNumber)
                .password(password)
                .image(image)
                .build();

        userService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> getUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
        var userInfo = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "약관 동의", description = "로그인한 사용자의 약관(consent) 동의 여부를 true로 전환합니다.")
    @PatchMapping("/users/consent")
    public ResponseEntity<?> updateConsent(@AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails) {
//        Integer userId = userService.getUserInfo(userDetails.getUsername()).getUserId();

        Integer userId = 1;
        ApiResult result = userService.updateConsent(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    @PatchMapping("/users/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails,
            @RequestBody PasswordRequestDTO request
            ) {
//        Integer userId = userService.getUserInfo(userDetails.getUsername()).getUserId();

        Integer userId = 1;
        ApiResult result = userService.updatePassword(userId, request);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }
}
