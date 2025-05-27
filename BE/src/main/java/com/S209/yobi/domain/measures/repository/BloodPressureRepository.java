package com.S209.yobi.domain.measures.repository;

import com.S209.yobi.domain.measures.entity.BloodPressure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodPressureRepository extends JpaRepository<BloodPressure, Long> {
}
