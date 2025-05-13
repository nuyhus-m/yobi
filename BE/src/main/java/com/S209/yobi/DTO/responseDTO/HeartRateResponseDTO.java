package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.HeartRate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartRateResponseDTO {
    private Long heartId;
    private MeasureWithLevel bpm;
    private MeasureWithLevel oxygen;

    public static HeartRateResponseDTO of(HeartRate heartRate){
        return HeartRateResponseDTO.builder()
                .heartId(heartRate.getId())
                .bpm(new MeasureWithLevel(heartRate.getBpm(), "높음"))
                .oxygen(new MeasureWithLevel(heartRate.getOxygen(), "높음"))
                .build();
    }
}
