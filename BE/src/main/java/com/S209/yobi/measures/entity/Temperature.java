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
@Table(name = "temperature")
public class Temperature {
    @Id
    @Column(name = "temperature_id", nullable = false)
    private Long id;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "created_at")
    private Instant createdAt;

}