package com.S209.yobi.domain.schedules.repository;

import com.S209.yobi.domain.schedules.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByUserIdOrderByVisitedDateAscStartAtAsc(Integer userId);
    List<Schedule> findByUserIdOrderByVisitedDateDescStartAtDesc(Integer userId);
    List<Schedule> findByUserIdAndVisitedDateBetweenOrderByVisitedDateAscStartAtAsc(
            Integer userId, LocalDate startDate, LocalDate endDate);
    List<Schedule> findByUserIdAndVisitedDateOrderByStartAtAsc(Integer userId, LocalDate visitedDate);
    List<Schedule> findByUserIdAndClientIdOrderByVisitedDateDesc(Integer userId, Integer clientId);
}
