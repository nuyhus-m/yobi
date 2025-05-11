package com.S209.yobi.domain.users.service;

import com.S209.yobi.domain.users.dto.SignUpRequest;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Transactional
    public void signUp(SignUpRequest request) {
        // 사번 중복 체크
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("이미 존재하는 사번입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = new User();
        user.setName(request.getName());
        user.setEmployeeId(request.getEmployeeId());
        user.setPassword(encodedPassword);

        // DB에 저장
        userRepository.save(user);
    }

    // Refresh Token을 Redis에 저장
    public void saveRefreshToken(String employeeId, String refreshToken, long expirationTime) {
        String key = REFRESH_TOKEN_PREFIX + employeeId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }

    // Refresh Token 조회
    public String getRefreshToken(String employeeId) {
        String key = REFRESH_TOKEN_PREFIX + employeeId;
        return redisTemplate.opsForValue().get(key);
    }

    // Refresh Token 삭제 (로그아웃 시 사용)
    public void deleteRefreshToken(String employeeId) {
        String key = REFRESH_TOKEN_PREFIX + employeeId;
        redisTemplate.delete(key);
    }

    // Refresh Token 유효성 검증
    public boolean validateRefreshToken(String employeeId, String refreshToken) {
        String storedToken = getRefreshToken(employeeId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
} 