package com.S209.yobi.measures.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "temperature")
public class Temperature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temperature_id", nullable = false)
    private Long id;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "created_at")
    private Instant createdAt;

}