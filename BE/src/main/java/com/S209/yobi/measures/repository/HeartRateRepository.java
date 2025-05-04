package com.S209.yobi.measures.repository;

import com.S209.yobi.measures.entity.HeartRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeartRateRepository extends JpaRepository<HeartRate, Long> {
}
