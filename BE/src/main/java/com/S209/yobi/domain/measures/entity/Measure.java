package com.S209.yobi.domain.measures.entity;

import com.S209.yobi.DTO.requestDTO.BloodRequestDTO;
import com.S209.yobi.DTO.requestDTO.BodyRequestDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.users.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.ZoneId;


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
    private Long date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JoinColumn(name = "composition_id", nullable = false)
    private BodyComposition body;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JoinColumn(name = "blood_id", nullable = false)
    private BloodPressure blood;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "heart_id")
    private HeartRate heart;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @JoinColumn(name = "stress_id")
    private Stress stress;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "temperature_id")
    private Temperature temperature;

//    @PrePersist
//    protected void onCreate(){
//        LocalDate now = LocalDate.now();
//        this.date = now;
//    }


    public static Measure fromBase(User user, Client client, BodyRequestDTO bodyDTO, BloodRequestDTO bloodDTO){
        if (bodyDTO == null) {
            throw new IllegalArgumentException("BodyRequestDTO cannot be null");
        }

        // LocalDate를 Instant로 변환하고, epochMilli(밀리초)로 변환
        long epochMilli = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())  // 시작 시간을 시스템 기본 시간대로 설정
                .toInstant()  // Instant로 변환
                .toEpochMilli();  // 밀리초로 변환

        return Measure.builder()
                .user(user)
                .client(client)
                .body(BodyComposition.fromDTO(bodyDTO))
                .blood(BloodPressure.fromDTO(bloodDTO))
                .date(epochMilli)
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

    public void setBody(BodyComposition body) {
        this.body = body;
    }

    public void setBlood(BloodPressure blood) {
        this.blood = blood;
    }



}