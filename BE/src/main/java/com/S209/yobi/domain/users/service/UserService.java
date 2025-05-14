package com.S209.yobi.domain.users.service;

import com.S209.yobi.DTO.TokenDTO;
import com.S209.yobi.DTO.requestDTO.LoginRequestDTO;
import com.S209.yobi.DTO.requestDTO.PasswordRequestDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.requestDTO.SignUpRequest;
import com.S209.yobi.S3Service;
import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.S209.yobi.DTO.responseDTO.UserInfoDTO;
import com.S209.yobi.exceptionFinal.CustomException;
import com.S209.yobi.exceptionFinal.HttpStatusCode;

import java.io.IOException;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final JwtProvider jwtProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, S3Service s3Service) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.s3Service = s3Service;
    }

    @Override
    public UserDetails loadUserByUsername(String employeeNumber) throws UsernameNotFoundException {
        User user = userRepository.findByEmployeeNumber(Integer.parseInt(employeeNumber))
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getEmployeeNumber()))
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    @Transactional
    public ApiResult signUp(SignUpRequest request) {
        try {// 사번 중복 체크
            if (userRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
                throw new IllegalArgumentException("이미 존재하는 사번입니다.");
            }

            // 비밀번호 유효성 검사
            String password = request.getPassword();
            if (password == null || password.isEmpty()) {
                return ApiResponseDTO.fail(ApiResponseCode.PASSWORD_NO_INPUT);
            }

            // 허용된 특수문자 목록
            String allowedSpecialChars = "@$!%*#?&";

            // 비밀번호에 허용되지 않은 특수문자가 포함되어 있는지 직접 검사
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (!Character.isLetterOrDigit(c) && allowedSpecialChars.indexOf(c) == -1) {
                    System.out.println("허용되지 않은 특수문자 발견: " + c); // 디버깅용
                    return ApiResponseDTO.fail(ApiResponseCode.INVALID_PASSWORD_FORMAT);
                }
            }

            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(request.getPassword());

            // 이미지 파일 업로드 및 url 획득
            String imageUrl = null;
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                imageUrl = s3Service.uploadFile(request.getImage());
            }

            // User 엔티티 생성
            User user = User.builder()
                    .name(request.getName())
                    .employeeNumber(request.getEmployeeNumber())
                    .password(encodedPassword)
                    .image(imageUrl)
                    .build();

            // DB에 저장
            userRepository.save(user);
            return ApiResponseDTO.success(null);
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 중 오류 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류 발생", e);
        }
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmployeeNumber(loginRequestDTO.getEmployeeNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtProvider.generateToken(user.getEmployeeNumber(), user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmployeeNumber(), user.getId());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .employeeId(String.valueOf(user.getEmployeeNumber()))
                .build();
    }


    public UserInfoDTO getUserInfoById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ApiResponseCode.NOT_FOUND_USER, HttpStatusCode.NOT_FOUND, "존재하지 않는 사용자입니다."));
        
        return new UserInfoDTO(
            user.getId(),
            user.getName(),
            user.getEmployeeNumber(),
            user.getImage(),
            user.getConsent()
        );
    }

    @Transactional
    public ApiResult updateConsent(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setConsent(true);

        return null;
    }

    @Transactional
    public ApiResult updatePassword(Integer userId, PasswordRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponseDTO.fail(ApiResponseCode.OLD_PASSWORD_WRONG);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return ApiResponseDTO.fail(ApiResponseCode.NEW_PASSWORD_SAME_AS_OLD);
        }

        // 비밀번호 유효성 검사
        ApiResult validationResult = validatePassword(request.getNewPassword());
        if (validationResult instanceof ApiResponseDTO<?>) {
            ApiResponseDTO<?> response = (ApiResponseDTO<?>) validationResult;
            if (!"200".equals(response.getCode())) {
                // 코드가 200이 아닐 경우 실패로 간주
                return validationResult;
            }
        } else {
            return ApiResponseDTO.fail(ApiResponseCode.SERVER_ERROR);
        }

        String newPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(newPassword);

        String accessToken = jwtProvider.generateToken(user.getEmployeeNumber(), user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmployeeNumber(), user.getId());

        TokenDTO tokenDTO = TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();

        return ApiResponseDTO.success(tokenDTO);

    }

    // 비밀번호 검증 로직 (특수문자 관련)
    private ApiResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ApiResponseDTO.fail(ApiResponseCode.PASSWORD_NO_INPUT);
        }

        String allowedSpecialChars = "@$!%*#?&";
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && allowedSpecialChars.indexOf(c) == -1) {
                return ApiResponseDTO.fail(ApiResponseCode.INVALID_PASSWORD_FORMAT);
            }
        }

        return ApiResponseDTO.success(null);
    }
} 