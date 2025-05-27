package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.Stress;
import com.S209.yobi.domain.measures.service.HealthCalculatorService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StressResponseDTO {
    private Long stressId;
    private Short stressValue;
    private String stressLevel;

    public static StressResponseDTO of(Stress stress) {
        if (stress == null) {
            return null;
        }

        // 직접 계산 로직으로 완전 대체
        String translatedLevel = HealthCalculatorService.translateStressLevel(stress.getStressLevel());

        return StressResponseDTO.builder()
                .stressId(stress.getId())
                .stressValue(stress.getStressValue())
                .stressLevel(translatedLevel)
                .build();
    }
}
