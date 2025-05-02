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
@Table(name = "body_composition")
public class BodyComposition {
    @Id
    @Column(name = "composition_id", nullable = false)
    private Long id;

    @Column(name = "bfp")
    private Double bfp;

    @Column(name = "bfm")
    private Double bfm;

    @Column(name = "smm")
    private Double smm;

    @Column(name = "bmr")
    private Double bmr;

    @Column(name = "icw")
    private Double icw;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "mineral")
    private Double mineral;

    @Column(name = "bodyage")
    private Short bodyage;

    @Column(name = "created_at")
    private Instant createdAt;

}