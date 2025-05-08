package com.S209.yobi.domain.measures.repository;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.users.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MeasureRepository extends JpaRepository<Measure, Long> {

    @Query("""
    SELECT  m
    FROM Measure m
    JOIN FETCH m.body
    LEFT JOIN FETCH m.stress
    LEFT JOIN FETCH m.heart
    JOIN FETCH m.blood
    WHERE m.user = :user
    AND m.client = :client
    AND m.date = :date
""")
    Optional<Measure> findByUserAndClientAndDate(
            @Param("user") User user,
            @Param("client") Client client,
            @Param("date") LocalDate date
    );


}
