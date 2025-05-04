package com.S209.yobi.measures.repository;

import com.S209.yobi.measures.entity.BloodPressure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodPressureRepository extends JpaRepository<BloodPressure, Long> {
}
