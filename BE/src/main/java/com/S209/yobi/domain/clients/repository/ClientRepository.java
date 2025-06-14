package com.S209.yobi.domain.clients.repository;

import com.S209.yobi.domain.clients.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByName(String name);
    List<Client> findByUserId(Integer userId);
    Optional<Client> findByUserIdAndName(Integer userId, String name);
}
