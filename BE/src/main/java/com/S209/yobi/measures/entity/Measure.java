package com.S209.yobi.measures.entity;

import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.measures.entity.BloodPressure;
import com.S209.yobi.measures.entity.BodyComposition;
import com.S209.yobi.measures.entity.HeartRate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "measure")
public class Measure {
    @Id
    @Column(name = "measure_id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "composition_id", nullable = false)
    private BodyComposition composition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blood_id", nullable = false)
    private BloodPressure blood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heart_id")
    private HeartRate heart;

}