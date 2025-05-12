package com.S209.yobi.domain.schedules.entity;

import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.clients.entity.Client;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schedule")
@BatchSize(size = 100)
@Schema
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
    private long visitedDate;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private long startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private long endAt;

    @Setter
    @Size(max = 150)
    @Column(name = "log_content", length = 150)
    private String logContent;

    @Setter
    @Column(name = "log_created_at")
    private Instant logCreatedAt;

    @Setter
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

    public long getVisitedDate() {
        return visitedDate;
    }

    public long getStartAt() {
        return startAt;
    }

    public long getEndAt() {
        return endAt;
    }

    public @Size(max = 150) String getLogContent() {
        return logContent;
    }

    public void setClient(@NotNull Client client) {
        this.client = client;
    }

    public void setVisitedDate(long visitedDate) {
        this.visitedDate = visitedDate;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(long endAt) {
        this.endAt = endAt;
    }
}