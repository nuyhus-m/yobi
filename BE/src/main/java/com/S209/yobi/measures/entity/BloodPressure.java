package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.BloodRequestDTO;
import com.S209.yobi.DTO.requestDTO.ReBloodRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "blood_pressure")
public class BloodPressure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_id", nullable = false)
    private Long id;

    @Column(name = "sbp")
    private Float  sbp;

    @Column(name = "dbp")
    private Float  dbp;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
    }

    public static BloodPressure fromDTO(BloodRequestDTO dto) {
        return BloodPressure.builder()
                .sbp(dto.getSbp())
                .dbp(dto.getDbp())
                .build();
    }

    public static BloodPressure fromReDTO(ReBloodRequestDTO dto) {
        return BloodPressure.builder()
                .sbp(dto.getSbp())
                .dbp(dto.getDbp())
                .build();
    }


}