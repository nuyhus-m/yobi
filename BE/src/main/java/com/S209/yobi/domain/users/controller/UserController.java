package com.S209.yobi.domain.users.controller;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.DTO.responseDTO.TokenResponseDTO;
import com.S209.yobi.DTO.responseDTO.NewTokenResponseDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.responseDTO.UserInfoDTO;
import com.S209.yobi.Mapper.AuthUtils;
import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.users.service.UserService;
import com.S209.yobi.exceptionFinal.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final AuthUtils authUtils;

    @Operation(summary = "사용자 회원가입", description = "이름, 사원번호, 비밀번호를 입력하여 회원가입을 진행합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패 (중복된 사번, 비밀번호 형식 오류 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"400-1\",\"message\":\"이미 존재하는 사번입니다\",\"data\":null}"
                            )
                    )
            )
    })
    public ResponseEntity<?> signUp(
        @Parameter(description = "사용자 이름") @RequestParam("name") String name,
        @Parameter(description = "사원 번호") @RequestParam("employeeNumber") Integer employeeNumber,
        @Parameter(description = "비밀번호(특수문자 포함 8~15자)") @RequestParam("password") String password,
        @Parameter(description = "프로필 이미지", content = @Content(mediaType = "multipart/form-data"))
        @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        SignUpRequest request = new SignUpRequest(name, employeeNumber, password, image);
        ApiResult result = userService.signUp(request);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            String code = errorResult.getCode();
            HttpStatus status = ApiResponseCode.fromCode(code).getHttpStatus();
            return ResponseEntity.status(status).body(errorResult);
        }

        return handleApiResult(result);
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "인가된 사용자인지 확인 후 사용자 정보를 반환합니다.")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"userId\":1,\"name\":\"홍길동\",\"employeeNumber\":123456,\"image\":\"https://example.com/profile.jpg\",\"consent\":true}"
                            )
                    )
            )
    })
    public ResponseEntity<?> getUserProfile() throws CustomException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
            }
            String token = authentication.getCredentials().toString();
            log.info("원본 토큰: {}", token);

            // 토큰 처리 - "Bearer" 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            token = token.trim();
            log.info("처리된 토큰: {}", token);

            Integer userId = jwtProvider.extractUserId(token);
            UserInfoDTO userInfo = userService.getUserInfoById(userId);
            return handleApiResult(ApiResponseDTO.success(userInfo));
        } catch (EntityNotFoundException e) {
            log.error("사용자 정보 조회 실패 : {}", e.getMessage());
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생 : {}", e.getMessage());
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }
    }

    @Operation(summary = "로그인", description = "사번과 비밀번호를 입력하여 로그인을 진행합니다.")
    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"userId\":1,\"name\":\"홍길동\",\"employeeId\":\"123456\"}"
                            )
                    )
            )
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 갱신", description = "Refresh token을 사용하여 새로운 Access token을 발급받습니다.")
    @PostMapping("/refresh")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"tokenType\":\"Bearer\"}"
                            )
                    )
            )
    })
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.fail("401", "Refresh token이 필요합니다."));
            }

            Integer employeeNumber = jwtProvider.extractEmployeeNumber(refreshToken);
            Integer userId = jwtProvider.extractUserId(refreshToken);

            if (employeeNumber == null || userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.fail("401", "유효하지 않은 refresh token입니다."));
            }

            UserDetails userDetails = userService.loadUserByUsername(String.valueOf(employeeNumber));
            
            if (jwtProvider.validateRefreshToken(refreshToken, userDetails, employeeNumber, userId)) {
                String newAccessToken = jwtProvider.generateToken(employeeNumber, userId);
                // String newRefreshToken = jwtProvider.generateRefreshToken(employeeNumber, userId);
                // return handleApiResult(ApiResponseDTO.success(new TokenDTO(newAccessToken, newRefreshToken, "Bearer")));
                return handleApiResult(ApiResponseDTO.success(new NewTokenResponseDTO(newAccessToken)));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.fail("401", "유효하지 않은 refresh token입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.fail("401", "토큰 갱신에 실패했습니다."));
        }
    }
        
    @Operation(summary = "약관 동의", description = "로그인한 사용자의 약관(consent) 동의 여부를 true로 전환합니다.")
    @PatchMapping("/consent")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "약관 동의 성공",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<?> updateConsent(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);
        ApiResult result = userService.updateConsent(userId);

        if (result instanceof ApiResponseDTO<?> errorResult) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경하고 새로운 토큰을 발급합니다.")
    @PatchMapping("/password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공 및 새 토큰 발급",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"refreshToken\":\"eyJhbGciOiJIUzI1NiJ9...\",\"tokenType\":\"Bearer\"}"
                            )
                    )
            )
    })
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer userId = authUtils.getUserIdFromUserDetails(userDetails);
        ApiResult result = userService.updatePassword(userId, request);
        return handleApiResult(result);
    }

    @Operation(summary = "로그아웃", description = "사용자의 refresh 토큰을 삭제하여 로그아웃을 처리합니다.")
    @PostMapping("/logout")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"200\",\"message\":\"로그아웃이 완료되었습니다.\",\"data\":null}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseDTO.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"401\",\"message\":\"인증되지 않은 요청입니다.\",\"data\":null}"
                            )
                    )
            )
    })
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        try {
            Integer userId = authUtils.getUserIdFromUserDetails(userDetails);
            
            // Refresh 토큰 삭제
            jwtProvider.deleteRefreshToken(userId);
            
            // Access 토큰 블랙리스트에 추가
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                jwtProvider.addToAccessTokenBlacklist(accessToken);
            }
            
            return ResponseEntity.ok(ApiResponseDTO.success("로그아웃이 완료되었습니다."));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDTO.fail("401", "인증되지 않은 요청입니다."));
        }
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 사용자의 계정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @DeleteMapping("/deleteaccount")
    public ResponseEntity<ApiResponseDTO> deleteAccount(
            @Valid @RequestBody DeleteAccountRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            String employeeNumber = userDetails.getUsername();
            String accessToken = jwtProvider.getAccessTokenFromRequest();
            
            // 1. 비밀번호 확인
            if (!userService.verifyPassword(Integer.parseInt(employeeNumber), request.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseDTO.fail("401", "비밀번호가 일치하지 않습니다."));
            }
            
            // 2. 사용자 정보 삭제
            userService.deleteUser(Integer.parseInt(employeeNumber));
            
            // 3. Refresh Token 삭제
            jwtProvider.deleteRefreshToken(Integer.parseInt(employeeNumber));
            
            // 4. Access Token 블랙리스트에 추가
            if (accessToken != null) {
                jwtProvider.addToAccessTokenBlacklist(accessToken);
            }
            
            return ResponseEntity.ok(ApiResponseDTO.success("회원 탈퇴가 완료되었습니다."));
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.fail("500", "회원 탈퇴 중 오류가 발생했습니다."));
        }
    }

    /**
     * ApiResponseDTO에서 data 필드만 추출하여 반환하는 유틸리티 메서드
     * 성공 응답(code=200)인 경우 data 필드만 반환하고,
     * 오류 응답(code!=200)인 경우 원래 ApiResponseDTO를 그대로 반환
     */
    private ResponseEntity<?> handleApiResult(ApiResult result) {
        if (result instanceof ApiResponseDTO<?> responseDTO) {
            String code = responseDTO.getCode();

            // 성공 응답인 경우
            if ("200".equals(code)) {
                Object data = responseDTO.getData();
                // data가 null이 아닌 경우에만 data만 반환
                if (data != null) {
                    return ResponseEntity.ok(data);
                }
            }

            // 오류 응답이거나 data가 null인 경우
            HttpStatus httpStatus = HttpStatus.OK;
            try {
                httpStatus = ApiResponseCode.fromCode(code).getHttpStatus();
            } catch (Exception e) {
                // 코드 변환 실패 시 기본값 유지
            }
            return ResponseEntity.status(httpStatus).body(responseDTO);
        }

        // ApiResponseDTO가 아닌 경우 (비정상적 상황)
        return ResponseEntity.ok(result);
    }
}
