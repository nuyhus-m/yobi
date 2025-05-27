package com.S209.yobi.domain.measures.entity;

import com.S209.yobi.DTO.requestDTO.HeartRateRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "heart_rate")
public class HeartRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heart_id", nullable = false)
    private Long id;

    @Column(name = "bpm")
    private Short bpm;

    @Column(name = "oxygen")
    private Short oxygen;

    @Column(name = "created_at")
    private Long createdAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = System.currentTimeMillis();  // 현재 시각의 epoch millis
    }

    public static HeartRate fromDTO(HeartRateRequestDTO dto) {
        return HeartRate.builder()
                .bpm(dto.getBpm())
                .oxygen(dto.getOxygen())
                .build();
    }

}