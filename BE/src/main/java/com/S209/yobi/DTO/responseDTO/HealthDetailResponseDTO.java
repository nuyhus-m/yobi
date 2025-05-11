package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

import static com.S209.yobi.Mapper.DateTimeUtils.toEpochMilli;


@Getter
@Builder
public class HealthDetailResponseDTO implements ApiResult {
    private Integer clientId;
    private Long today;
    private BodyCompositionResponseDTO bodyComposition;
    private TemperatureResponseDTO temperature;
    private BloodResponseDTO bloodPressure;
    private HeartRateResponseDTO heartRate;
    private StressResponseDTO stress;

    public static HealthDetailResponseDTO of (Measure measure, int clientId, LocalDate today, Map<String, String> redisLevels){

        // 오늘 측정 값이 업는 경우
        if(measure == null){

            // 시간 Long 타입으로 변환
            long epochMilli = toEpochMilli(today);

            return HealthDetailResponseDTO.builder()
                    .clientId(clientId)
                    .today(epochMilli)
                    .bodyComposition(null)
                    .temperature(null)
                    .bloodPressure(null)
                    .heartRate(null)
                    .stress(null)
                    .build();
        }

        // 시간 Long 타입으로 변환
        long epochMilli = toEpochMilli(measure.getDate());

        return HealthDetailResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .today(epochMilli)
                .bodyComposition(BodyCompositionResponseDTO.of(measure.getBody(),redisLevels)) // 필수 값
                .temperature(measure.getTemperature() != null ? TemperatureResponseDTO.of(measure.getTemperature()): null)
                .bloodPressure(BloodResponseDTO.of(measure.getBlood())) // 필수 값
                .heartRate(measure.getHeart() != null ? HeartRateResponseDTO.of(measure.getHeart()): null)
                .stress(measure.getStress() != null ? StressResponseDTO.of(measure.getStress()): null)
                .build();
    }

}
