package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.StressRequestDTO;
import com.S209.yobi.measures.Enum.StressLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stress")
public class Stress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stress_id", nullable = false)
    private Long id;

    @Column(name = "stress_value")
    private Short stressValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "stress_level", length = 10)
    private StressLevel stressLevel;

    @Column(name = "oxygen")
    private Short oxygen;

    @Column(name = "bpm")
    private Short bpm;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
    }

    public static Stress fromDTO(StressRequestDTO dto){
        return Stress.builder()
                .stressValue(dto.getStressValue())
                .stressLevel(dto.getStressLevel())
                .oxygen(dto.getOxygen())
                .bpm(dto.getBpm())
                .build();
    }


}