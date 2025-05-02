package com.S209.yobi.report.entity;

import com.S209.yobi.clients.entity.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "weekly_report")
public class WeeklyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "report_content", nullable = false, length = Integer.MAX_VALUE)
    private String reportContent;

    @NotNull
    @Column(name = "log_summary", nullable = false, length = Integer.MAX_VALUE)
    private String logSummary;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}