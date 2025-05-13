package com.S209.yobi.domain.users.service;

import com.S209.yobi.DTO.requestDTO.LoginRequestDTO;
import com.S209.yobi.DTO.responseDTO.LoginResponseDTO;
import com.S209.yobi.DTO.requestDTO.SignUpRequest;
import com.S209.yobi.config.JwtConfig;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.S209.yobi.DTO.responseDTO.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

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
        // 사번 중복 체크
        if (userRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new IllegalArgumentException("이미 존재하는 사번입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // User 엔티티 생성
        User user = new User();
        user.setName(request.getName());
        user.setEmployeeNumber(request.getEmployeeNumber());
        user.setPassword(encodedPassword);

        // DB에 저장
        userRepository.save(user);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 사용자 조회
        User user = userRepository.findByEmployeeNumber(request.getEmployeeNumber())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // UserDetails 생성
        UserDetails userDetails = loadUserByUsername(String.valueOf(request.getEmployeeNumber()));

        // 토큰 생성
        String accessToken = jwtConfig.generateToken(userDetails);
        String refreshToken = jwtConfig.generateRefreshToken(userDetails);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .employeeId(String.valueOf(user.getEmployeeNumber()))
                .build();
    }

    public UserInfoDTO getUserInfo(String employeeNumber) {
        User user = userRepository.findByEmployeeNumber(Integer.parseInt(employeeNumber))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new UserInfoDTO(
            user.getId() != null ? user.getId().longValue() : null,
            user.getName(),
            String.valueOf(user.getEmployeeNumber()),
            user.getImage(),
            user.getConsent()
        );
    }
} 