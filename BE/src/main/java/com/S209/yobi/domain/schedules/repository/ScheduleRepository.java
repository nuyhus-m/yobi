package com.S209.yobi.domain.schedules.repository;

import com.S209.yobi.domain.schedules.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByUserIdOrderByVisitedDateAscStartAtAsc(Integer userId);
    List<Schedule> findByUserIdOrderByVisitedDateDescStartAtDesc(Integer userId);
    List<Schedule> findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
            Integer userId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByUserIdAndVisitedDateOrderByStartAtAsc(Integer userId, LocalDate visitedDate);
    List<Schedule> findByUserIdAndClientIdOrderByVisitedDateDesc(Integer userId, Integer clientId);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client JOIN FETCH s.user WHERE s.id = :scheduleId")
    Optional<Schedule> findByIdWithClientAndUser(@Param("scheduleId") Integer scheduleId);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.visitedDate = :date")
    List<Schedule> findByUserIdAndVisitedDateWithClient(
            @Param("userId") Integer userId,
            @Param("date") LocalDate date);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.visitedDate = :date " +
            "AND ((s.startAt < :endAt AND s.endAt > :startAt))")
    List<Schedule> findByUserIdAndVisitedDateAndTimeOverlapping(
            @Param("userId") Integer userId,
            @Param("date") LocalDate date,
            @Param("startAt") LocalTime startAt,
            @Param("endAt") LocalTime endAt);


}
