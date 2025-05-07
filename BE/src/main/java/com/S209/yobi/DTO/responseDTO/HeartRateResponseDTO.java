package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.entity.HeartRate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartRateResponseDTO {
    private Long heartId;
    private Short bpm;

    public static HeartRateResponseDTO of(HeartRate heartRate){
        return HeartRateResponseDTO.builder()
                .heartId(heartRate.getId())
                .bpm(heartRate.getBpm())
                .build();
    }
}
