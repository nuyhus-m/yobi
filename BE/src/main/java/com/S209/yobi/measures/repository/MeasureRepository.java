package com.S209.yobi.measures.repository;

import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.measures.entity.Measure;
import com.S209.yobi.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MeasureRepository extends JpaRepository<Measure, Long> {


    Optional<Measure> findByUserAndClientAndDate(User user, Client client, LocalDate date);
}
