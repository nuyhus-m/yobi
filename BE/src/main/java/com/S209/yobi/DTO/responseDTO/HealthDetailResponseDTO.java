package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.DTO.requestDTO.TemperatureRequestDTO;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.entity.Temperature;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class HealthDetailResponseDTO implements ApiResult {
    private Integer clientId;
    private LocalDate today;
    private BodyCompositionResponseDTO bodyComposition;
    private TemperatureResponseDTO temperature;
    private BloodResponseDTO bloodPressure;
    private HeartRateResponseDTO heartRate;
    private StressResponseDTO stress;

    public static HealthDetailResponseDTO of (Measure measure, int clientId, LocalDate today){

        // 오늘 측정 값이 업는 경우
        if(measure == null){
            return HealthDetailResponseDTO.builder()
                    .clientId(clientId)
                    .today(today)
                    .bodyComposition(null)
                    .temperature(null)
                    .bloodPressure(null)
                    .heartRate(null)
                    .stress(null)
                    .build();
        }

        return HealthDetailResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .today(measure.getDate())
                .bodyComposition(BodyCompositionResponseDTO.of(measure.getBody())) // 필수 값
                .temperature(measure.getTemperature() != null ? TemperatureResponseDTO.of(measure.getTemperature()): null)
                .bloodPressure(BloodResponseDTO.of(measure.getBlood())) // 필수 값
                .heartRate(measure.getHeart() != null ? HeartRateResponseDTO.of(measure.getHeart()): null)
                .stress(measure.getStress() != null ? StressResponseDTO.of(measure.getStress()): null)
                .build();
    }

}
