package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Builder
public class MainHealthResponseDTO implements ApiResult {

    private Integer clientId;
    private LocalDate today;
    private BodyMainResponseDTO bodyComposition;
    private StressResponseDTO stress;
    private HeartRateResponseDTO heartRate;
    private BloodResponseDTO bloodPressure;

    public static MainHealthResponseDTO of(Measure measure, int clientId, LocalDate today){

        // 오늘 측정 값이 업는 경우
        if(measure == null){
            return MainHealthResponseDTO.builder()
                    .clientId(clientId)
                    .today(today)
                    .bodyComposition(null)
                    .stress(null)
                    .heartRate(null)
                    .bloodPressure(null)
                    .build();
        }

        // 측정값이 있는 경우
        return MainHealthResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .today(measure.getDate())
                .bodyComposition(BodyMainResponseDTO.of(measure.getBody()))
                .stress(measure.getStress() != null ? StressResponseDTO.of(measure.getStress()): null)
                .heartRate(measure.getHeart() != null ?HeartRateResponseDTO.of(measure.getHeart()): null)
                .bloodPressure(BloodResponseDTO.of(measure.getBlood()))
                .build();
    }

}
