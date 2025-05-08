package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.DTO.requestDTO.TemperatureRequestDTO;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.entity.Temperature;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HealthDetailResponseDTO implements ApiResult {
    private Integer clientId;
    private BodyCompositionResponseDTO bodyComposition;
    private TemperatureResponseDTO temperature;
    private BloodResponseDTO bloodPressure;
    private HeartRateResponseDTO heartRate;
    private StressResponseDTO stress;

    public static HealthDetailResponseDTO of (Measure measure){
        return HealthDetailResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .bodyComposition(BodyCompositionResponseDTO.of(measure.getBody()))
                .temperature(TemperatureResponseDTO.of(measure.getTemperature()))
                .bloodPressure(BloodResponseDTO.of(measure.getBlood()))
                .heartRate(HeartRateResponseDTO.of(measure.getHeart()))
                .stress(StressResponseDTO.of(measure.getStress()))
                .build();
    }

}
