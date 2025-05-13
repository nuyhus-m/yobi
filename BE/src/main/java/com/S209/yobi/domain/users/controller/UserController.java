package com.S209.yobi.domain.users.controller;

import com.S209.yobi.DTO.requestDTO.LoginRequestDTO;
import com.S209.yobi.DTO.requestDTO.PasswordRequestDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.requestDTO.SignUpRequest;
import com.S209.yobi.DTO.responseDTO.UserInfoDTO;
import com.S209.yobi.exceptionFinal.GlobalExceptionHandler;
import com.S209.yobi.domain.users.service.UserService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.config.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Operation(summary = "사용자 회원가입", description = "이름, 사원번호, 비밀번호를 입력하여 회원가입을 진행합니다.")
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

    @Operation(summary = "현재 사용자 정보 조회", description = "인가된 사용자인지 확인 후 사용자 정보를 반환합니다.")
    @GetMapping
    public ResponseEntity<GlobalExceptionHandler<?>> getUserProfile() {
        try {
            // 1. SecurityContext에서 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GlobalExceptionHandler.fail("401", "인증되지 않은 사용자입니다."));
            }

            // 2. JWT 토큰에서 userId 추출
            String token = authentication.getCredentials().toString();
            Integer userId = jwtProvider.extractUserId(token);

            // 3. 사용자 정보 조회
            UserInfoDTO userInfo = userService.getUserInfoById(userId);
            return ResponseEntity.ok(ApiResponseDTO.success(userInfo));

        } catch (EntityNotFoundException e) {
            log.error("사용자 정보 조회 실패 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증되지 않은 요청입니다.");
        }
    }

    @Operation(summary = "로그인", description = "사번과 비밀번호를 입력하여 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "토큰 갱신", description = "Refresh token을 사용하여 새로운 Access token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.fail("401", "유효하지 않은 refresh token입니다."));
            }

            String token = refreshToken.substring(7);
            Integer employeeNumber = jwtProvider.extractEmployeeNumber(token);
            Integer userId = jwtProvider.extractUserId(token);

            if (employeeNumber == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.fail("401", "유효하지 않은 refresh token입니다."));
            }

            UserDetails userDetails = userService.loadUserByUsername(String.valueOf(employeeNumber));
            
            if (jwtProvider.validateRefreshToken(token, userDetails, employeeNumber, userId)) {
                String newAccessToken = jwtProvider.generateToken(employeeNumber, userId);
                return ResponseEntity.ok(ApiResponseDTO.success(new TokenDTO(newAccessToken, token, "Bearer")));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.fail("401", "유효하지 않은 refresh token입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.fail("401", "토큰 갱신에 실패했습니다."));
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
