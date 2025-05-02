package com.S209.yobi.measures.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "heart_rate")
public class HeartRate {
    @Id
    @Column(name = "heart_id", nullable = false)
    private Long id;

    @Column(name = "bpm")
    private Short bpm;

    @Column(name = "oxygen")
    private Short oxygen;

    @Column(name = "created_at")
    private Instant createdAt;

}