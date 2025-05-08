package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.entity.Measure;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthDetailResponseDTO {
    private Integer clientId;
    private BodyCompositionResponseDTO bodyComposition;
    private BloodResponseDTO bloodPressure;
    private HeartRateResponseDTO heartRate;
    private StressResponseDTO stress;

    public static HealthDetailResponseDTO of (Measure measure){
        return HealthDetailResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .bodyComposition(BodyCompositionResponseDTO.of(measure.getBody()))
                .bloodPressure(BloodResponseDTO.of(measure.getBlood()))
                .heartRate(HeartRateResponseDTO.of(measure.getHeart()))
                .stress(StressResponseDTO.of(measure.getStress()))
                .build();
    }

}
