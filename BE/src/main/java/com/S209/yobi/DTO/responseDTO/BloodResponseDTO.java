package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.BloodPressure;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BloodResponseDTO {
    private Long bloodId;
    private MeasureWithLevel sbp;
    private MeasureWithLevel dbp;

    public static BloodResponseDTO of(BloodPressure blood){

        // 소수점 첫째자리로 반올림
        float roundedSbp = Math.round(blood.getSbp() * 10) / 10.0f;
        float roundedDbp = Math.round(blood.getDbp() * 10) / 10.0f;

        return BloodResponseDTO.builder()
                .bloodId(blood.getId())
                .sbp(new MeasureWithLevel(roundedSbp, "높음"))
                .dbp(new MeasureWithLevel(roundedDbp, "높음"))
                .build();
    }

}
