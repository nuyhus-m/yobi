package com.S209.yobi.schedules.entity;

import com.S209.yobi.users.entity.User;
import com.S209.yobi.clients.entity.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "visited_date", nullable = false)
    private LocalDate visitedDate;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private LocalTime endAt;

    @Size(max = 150)
    @Column(name = "log_content", length = 150)
    private String logContent;

    @Column(name = "log_created_at")
    private Instant logCreatedAt;

    @Column(name = "log_updated_at")
    private Instant logUpdatedAt;

    public Integer getId() {
        return id;
    }

    public @NotNull User getUser() {
        return user;
    }

    public @NotNull Client getClient() {
        return client;
    }

    public @NotNull LocalDate getVisitedDate() {
        return visitedDate;
    }

    public @NotNull LocalTime getStartAt() {
        return startAt;
    }

    public @NotNull LocalTime getEndAt() {
        return endAt;
    }

    public @Size(max = 150) String getLogContent() {
        return logContent;
    }

    public Instant getLogCreatedAt() {
        return logCreatedAt;
    }

    public Instant getLogUpdatedAt() {
        return logUpdatedAt;
    }
}