package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.HeartRate;
import com.S209.yobi.domain.measures.service.HealthCalculatorService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartRateResponseDTO {
    private Long heartId;
    private MeasureWithLevel bpm;
    private MeasureWithLevel oxygen;

    public static HeartRateResponseDTO of(HeartRate heartRate) {
        if (heartRate == null) {
            return null;
        }

        // 직접 계산 로직 사용
        String bpmLevel = HealthCalculatorService.calculateBpmLevel(heartRate.getBpm());
        String oxygenLevel = HealthCalculatorService.calculateOxygenLevel(heartRate.getOxygen());

        return HeartRateResponseDTO.builder()
                .heartId(heartRate.getId())
                .bpm(new MeasureWithLevel(heartRate.getBpm(), bpmLevel))
                .oxygen(new MeasureWithLevel(heartRate.getOxygen(), oxygenLevel))
                .build();
    }
}