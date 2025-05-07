package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.TemperatureRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
    }

    public static Temperature fromDTO(TemperatureRequestDTO dto){
        return Temperature.builder()
                .temperature(dto.getTemperature())
                .build();
    }

}