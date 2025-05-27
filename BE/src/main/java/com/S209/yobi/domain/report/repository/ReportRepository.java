package com.S209.yobi.domain.report.repository;

import com.S209.yobi.domain.report.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<WeeklyReport, Long> {

    List<WeeklyReport> findByClientId(int clientId);
}
