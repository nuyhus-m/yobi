package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.BodyCompositionDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
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


    public static BodyComposition fromDTO(BodyCompositionDTO dto){
        BodyComposition body = new BodyComposition();
        body.setBfp(dto.getBfp());
        body.setBfm(dto.getBfm());
        body.setSmm(dto.getSmm());
        body.setBmr(dto.getBmr());
        body.setIcw(dto.getIcw());
        body.setProtein(dto.getProtein());
        body.setMineral(dto.getMineral());
        body.setBodyage(dto.getBodyage());
        return body;
    }


}


