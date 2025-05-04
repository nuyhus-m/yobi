package com.S209.yobi.measures.entity;

import com.S209.yobi.DTO.requestDTO.BloodPressureDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
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

    public static BloodPressure fromDTO(BloodPressureDTO dto){
        BloodPressure blood = new BloodPressure();
        blood.setSbp(dto.getSbp());
        blood.setDbp(dto.getDbp());
        return blood;
    }


}