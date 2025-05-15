package com.S209.yobi.domain.measures.repository;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.users.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
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
            @Param("date") Long date
    );


    List<Measure> findByClient( Client client, Pageable pageable);


    @Query(value = """
        SELECT  m.date, b.bfp, b.bmr, b.ecf, b.protein,
                bp.sbp, bp.dbp,
                s.stress_value
        FROM measure m
        JOIN body_composition b on m.composition_id = b.composition_id
        JOIN blood_pressure bp on m.blood_id = bp.blood_id
        Left JOIN stress s on m.stress_id = s.stress_id
        WHERE m.client_id = :clientId
        AND (:cursorDate IS NULL OR :cursorDate = 0 OR m.date < :cursorDate)
        ORDER BY m.date DESC 
        LIMIT :size
    """, nativeQuery = true)
    List<Object[]> findHealthTrendsNative(@Param("clientId") int clientId,
                                          @Param("cursorDate") Long cursorDate,
                                          @Param("size") int size);



    @Query("""
        SELECT  m
        FROM Measure m
        WHERE m.client = :client
        AND m.date < :cursorDate
        ORDER BY m.date DESC
""")
    List<Measure> findByClientBeforeDate(@Param("client") Client client,
                                         @Param("cursorDate") LocalDate cursorDate,
                                         Pageable pageable);



    Optional<Measure> findTopByUserAndClientOrderByDateDesc(User user, Client client);


}
