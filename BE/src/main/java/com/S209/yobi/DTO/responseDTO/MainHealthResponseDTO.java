package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import static com.S209.yobi.Mapper.DateTimeUtils.toEpochMilli;

@Getter
@Builder
public class MainHealthResponseDTO implements ApiResult {

    private Integer clientId;
    private Long today;
    private BodyMainResponseDTO bodyComposition;
    private StressResponseDTO stress;
    private HeartRateResponseDTO heartRate;
    private BloodResponseDTO bloodPressure;

    public static MainHealthResponseDTO of(Measure measure, int clientId, LocalDate today, Map<String, String>redisLevels){

        // 오늘 측정 값이 업는 경우
        if(measure == null){
            // 시간 Long 타입으로 변환
            long epochMilli = toEpochMilli(today);

            return MainHealthResponseDTO.builder()
                    .clientId(clientId)
                    .today(epochMilli)
                    .bodyComposition(null)
                    .stress(null)
                    .heartRate(null)
                    .bloodPressure(null)
                    .build();
        }

        // 측정값이 있는 경우

        // 시간 Long 타입으로 변환
        long epochMilli = toEpochMilli(measure.getDate());

        return MainHealthResponseDTO.builder()
                .clientId(measure.getClient().getId())
                .today(epochMilli)
                .bodyComposition(BodyMainResponseDTO.of(measure.getBody(), redisLevels))
                .stress(measure.getStress() != null ? StressResponseDTO.of(measure.getStress()): null)
                .heartRate(measure.getHeart() != null ?HeartRateResponseDTO.of(measure.getHeart()): null)
                .bloodPressure(BloodResponseDTO.of(measure.getBlood()))
                .build();
    }

}
