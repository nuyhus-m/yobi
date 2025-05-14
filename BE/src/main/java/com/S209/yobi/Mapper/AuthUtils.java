package com.S209.yobi.Mapper;

import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.CustomException;
import com.S209.yobi.exceptionFinal.HttpStatusCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 인증 관련 유틸리티 메서드를 제공하는 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    /**
     * 현재 인증 정보에서 userId를 추출
     *
     * @return 사용자 ID
     * @throws CustomException 인증 정보가 없거나 처리 중 오류 발생 시
     */
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
        }

        try {
            String token = authentication.getCredentials().toString();

            // 토큰 처리 - "Bearer" 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            token = token.trim();

            return jwtProvider.extractUserId(token);
        } catch (Exception e) {
            log.error("사용자 ID 추출 오류: {}", e.getMessage());
            throw new CustomException(ApiResponseCode.UNAUTHORIZED, HttpStatusCode.UNAUTHORIZED, "인증 토큰에서 사용자 정보를 추출할 수 없습니다.");
        }
    }

    /**
     * UserDetails 객체에서 userId를 추출
     *
     * @param userDetails Spring Security UserDetails 객체
     * @return 사용자 ID
     * @throws CustomException 사용자 정보 추출 중 오류 발생 시
     */
    public Integer getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        try {
            Integer employeeNumber = Integer.parseInt(userDetails.getUsername());
            User user = userRepository.findByEmployeeNumber(employeeNumber)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));
            return user.getId();
        } catch (NumberFormatException e) {
            log.error("사원번호 변환 오류: {}", e.getMessage());
            throw new CustomException(ApiResponseCode.BAD_REQUEST, HttpStatusCode.BAD_REQUEST, "유효하지 않은 사원번호 형식입니다.");
        } catch (EntityNotFoundException e) {
            log.error("사용자 찾기 실패: {}", e.getMessage());
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("사용자 정보 추출 오류: {}", e.getMessage());
            throw new CustomException(ApiResponseCode.SERVER_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR, "사용자 정보를 추출할 수 없습니다.");
        }
    }

    /**
     * 인증 정보에서 employeeNumber를 추출
     *
     * @return 사용자의 사원번호
     * @throws CustomException 인증 정보가 없거나 처리 중 오류 발생 시
     */
    public Integer getCurrentEmployeeNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
        }

        try {
            String token = authentication.getCredentials().toString();

            // 토큰 처리 - "Bearer" 접두사 제거
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            token = token.trim();

            return jwtProvider.extractEmployeeNumber(token);
        } catch (Exception e) {
            log.error("사원번호 추출 오류: {}", e.getMessage());
            throw new CustomException(ApiResponseCode.UNAUTHORIZED, HttpStatusCode.UNAUTHORIZED, "인증 토큰에서 사용자 정보를 추출할 수 없습니다.");
        }
    }
}