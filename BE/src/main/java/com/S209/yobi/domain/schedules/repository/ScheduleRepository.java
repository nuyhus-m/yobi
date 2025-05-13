package com.S209.yobi.domain.schedules.repository;

import com.S209.yobi.domain.schedules.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByUserIdOrderByVisitedDateAscStartAtAsc(Integer userId);
    List<Schedule> findByUserIdOrderByVisitedDateDescStartAtDesc(Integer userId);
    List<Schedule> findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
            Integer userId, long startDate, long endDate);
    List<Schedule> findByUserIdAndVisitedDateOrderByStartAtAsc(Integer userId, long visitedDate);
    List<Schedule> findByUserIdAndClientIdOrderByVisitedDateDesc(Integer userId, Integer clientId);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client JOIN FETCH s.user WHERE s.id = :scheduleId")
    Optional<Schedule> findByIdWithClientAndUser(@Param("scheduleId") Integer scheduleId);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.visitedDate >= :dayStart AND s.visitedDate <= :dayEnd")
    List<Schedule> findByUserIdAndVisitedDateWithClient(
            @Param("userId") Integer userId,
            @Param("dayStart") long dayStart,
            @Param("dayEnd") long dayEnd);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.visitedDate >= :dayStart AND s.visitedDate <= :dayEnd " +
            "AND ((s.startAt < :endAt AND s.endAt > :startAt))")
    List<Schedule> findByUserIdAndVisitedDateAndTimeOverlapping(
            @Param("userId") Integer userId,
            @Param("dayStart") long dayStart,
            @Param("dayEnd") long dayEnd,
            @Param("startAt") long startAt,
            @Param("endAt") long endAt);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.logContent IS NOT NULL ORDER BY s.visitedDate DESC, s.startAt DESC")
    List<Schedule> findByUserIdAndLogContentNotNullOrderByVisitedDateDescStartAtDesc(@Param("userId") Integer userId);

    @Query("SELECT s FROM Schedule s JOIN FETCH s.client WHERE s.user.id = :userId AND s.client.id = :clientId AND s.logContent IS NOT NULL ORDER BY s.visitedDate DESC")
    List<Schedule> findByUserIdAndClientIdAndLogContentNotNullOrderByVisitedDateDesc(
            @Param("userId") Integer userId,
            @Param("clientId") Integer clientId);
}