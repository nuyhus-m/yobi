package com.S209.yobi.domain.users.repository;

import com.S209.yobi.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmployeeNumber(Integer employeeNumber);
    boolean existsByEmployeeNumber(Integer employeeNumber);
}
