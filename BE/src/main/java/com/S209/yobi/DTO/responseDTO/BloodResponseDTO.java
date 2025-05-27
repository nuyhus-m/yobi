package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.BloodPressure;
import com.S209.yobi.domain.measures.service.HealthCalculatorService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodResponseDTO {
    private Long bloodId;
    private MeasureWithLevel sbp;
    private MeasureWithLevel dbp;

    public static BloodResponseDTO of(BloodPressure blood) {
        if (blood == null) {
            return null;
        }

        // 소수점 첫째자리로 반올림
        float roundedSbp = Math.round(blood.getSbp() * 10) / 10.0f;
        float roundedDbp = Math.round(blood.getDbp() * 10) / 10.0f;

        // 직접 계산 로직 사용
        String sbpLevel = HealthCalculatorService.calculateSbpLevel(roundedSbp);
        String dbpLevel = HealthCalculatorService.calculateDbpLevel(roundedDbp);

        return BloodResponseDTO.builder()
                .bloodId(blood.getId())
                .sbp(new MeasureWithLevel(roundedSbp, sbpLevel))
                .dbp(new MeasureWithLevel(roundedDbp, dbpLevel))
                .build();
    }
}