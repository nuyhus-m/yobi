package com.S209.yobi.domain.measures.repository;

import com.S209.yobi.domain.measures.entity.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {
}
