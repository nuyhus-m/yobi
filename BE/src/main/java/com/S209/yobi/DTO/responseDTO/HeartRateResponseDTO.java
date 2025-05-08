package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.HeartRate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartRateResponseDTO {
    private Long heartId;
    private Short bpm;
    private Short oxygen;

    public static HeartRateResponseDTO of(HeartRate heartRate){
        return HeartRateResponseDTO.builder()
                .bpm(heartRate.getBpm())
                .oxygen(heartRate.getOxygen())
                .build();
    }
}
