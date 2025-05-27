package com.S209.yobi.domain.measures.entity;

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
    private Long createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = System.currentTimeMillis();  // 현재 시각의 epoch millis
    }

    public static Temperature fromDTO(TemperatureRequestDTO dto){
        return Temperature.builder()
                .temperature(dto.getTemperature())
                .build();
    }

}