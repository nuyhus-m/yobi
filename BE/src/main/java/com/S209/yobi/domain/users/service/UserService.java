package com.S209.yobi.domain.users.service;

import com.S209.yobi.DTO.requestDTO.LoginRequestDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.requestDTO.SignUpRequest;
import com.S209.yobi.S3Service;
import com.S209.yobi.config.JwtProvider;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
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
    public void signUp(SignUpRequest request) {
        try {// 사번 중복 체크
            if (userRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
                throw new IllegalArgumentException("이미 존재하는 사번입니다.");
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
} 