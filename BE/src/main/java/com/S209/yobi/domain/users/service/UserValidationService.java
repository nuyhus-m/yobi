package com.S209.yobi.domain.users.service;

import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final UserRepository userRepository;

    public User validateUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException( userId + ": 유저를 찾을 수 없습니다."));
    }
}
