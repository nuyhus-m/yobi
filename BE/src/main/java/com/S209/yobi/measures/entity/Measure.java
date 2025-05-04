package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.BloodPressureDTO;
import com.S209.yobi.DTO.requestDTO.BodyCompositionDTO;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.measures.entity.BloodPressure;
import com.S209.yobi.measures.entity.BodyComposition;
import com.S209.yobi.measures.entity.HeartRate;
import com.S209.yobi.measures.service.MeasureService;
import com.S209.yobi.users.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "measure")
public class Measure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measure_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "composition_id", nullable = false)
    private BodyComposition body;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "blood_id", nullable = false)
    private BloodPressure blood;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "heart_id")
    private HeartRate heart;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "stress_id")
    private Stress stress;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "temperature_id")
    private Temperature temperature;

    @PrePersist
    protected void onCreate(){
        LocalDate now = LocalDate.now();
        this.date = now;
    }


    public static Measure fromBase(User user, Client client, BodyCompositionDTO bodyDTO, BloodPressureDTO bloodDTO){
        return Measure.builder()
                .user(user)
                .client(client)
                .body(BodyComposition.fromDTO(bodyDTO))
                .blood(BloodPressure.fromDTO(bloodDTO))
                .date(LocalDate.now())
                .build();

    }

    // 추가 측정값 세터
    public void setHeartRate(HeartRate heart){
        this.heart = heart;
    }

    public void setStress(Stress stress) {
        this.stress = stress;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }



}