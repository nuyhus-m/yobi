package com.S209.yobi.measures.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "stress")
public class Stress {
    @Id
    @Column(name = "stress_id", nullable = false)
    private Long id;

    @Column(name = "stress_value")
    private Short stressValue;

    @Size(max = 10)
    @Column(name = "stress_level", length = 10)
    private String stressLevel;

    @Column(name = "oxygen")
    private Short oxygen;

    @Column(name = "bpm")
    private Short bpm;

    @Column(name = "created_at")
    private Instant createdAt;

}