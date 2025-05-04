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
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "composition_id", nullable = false)
    private BodyComposition body;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blood_id", nullable = false)
    private BloodPressure blood;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heart_id")
    private HeartRate heart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stress_id")
    private Stress stress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temperature_id")
    private Temperature temperature;

    @PrePersist
    protected void onCreate(){
        LocalDate now = LocalDate.now();
        this.date = now;
    }


    public static Measure fromBase(User user, Client client, BodyCompositionDTO bodyDTO, BloodPressureDTO bloodDTO){
        Measure measure = new Measure();

        BodyComposition body = BodyComposition.fromDTO(bodyDTO);
        BloodPressure blood =  BloodPressure.fromDTO(bloodDTO);

        measure.setUser(user);
        measure.setClient(client);
        measure.setBody(body);
        measure.setBlood(blood);

        return measure;

    }

}