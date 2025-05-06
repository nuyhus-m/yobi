package com.S209.yobi.schedules.repository;

import com.S209.yobi.schedules.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByUserIdOrderByVisitedDateAscStartAtAsc(Integer userId);
}
