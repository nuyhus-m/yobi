package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.BodyRequestDTO;
import com.S209.yobi.DTO.requestDTO.ReBodyRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "body_composition")
public class BodyComposition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "composition_id", nullable = false)
    private Long id;

    @Column(name = "bfp")
    private Float  bfp;

    @Column(name = "bfm")
    private Float  bfm;

    @Column(name = "smm")
    private Float  smm;

    @Column(name = "bmr")
    private Float  bmr;

    @Column(name = "icw")
    private Float  icw;

    @Column(name = "ecw")
    private Float  ecw;

    @Column(name = "protein")
    private Float  protein;

    @Column(name = "mineral")
    private Float  mineral;

    @Column(name = "bodyage")
    private Short bodyage;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate(){
        Instant now = Instant.now();
        this.createdAt = now;
    }


    public static BodyComposition fromDTO(BodyRequestDTO dto){
        return BodyComposition.builder()
                .bfp(dto.getBfp())
                .bfm(dto.getBfm())
                .smm(dto.getSmm())
                .bmr(dto.getBmr())
                .icw(dto.getIcw())
                .ecw(dto.getEcw())
                .protein(dto.getProtein())
                .mineral(dto.getMineral())
                .bodyage(dto.getBodyage())
                .build();
    }

    public static BodyComposition fromReDTO(ReBodyRequestDTO dto){
        return BodyComposition.builder()
                .bfp(dto.getBfp())
                .bfm(dto.getBfm())
                .smm(dto.getSmm())
                .bmr(dto.getBmr())
                .icw(dto.getIcw())
                .ecw(dto.getEcw())
                .protein(dto.getProtein())
                .mineral(dto.getMineral())
                .bodyage(dto.getBodyage())
                .build();
    }


}


