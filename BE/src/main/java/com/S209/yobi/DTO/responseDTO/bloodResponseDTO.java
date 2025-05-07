package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.entity.BloodPressure;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class bloodResponseDTO {
    private Long bloodId;
    private float sbp;
    private float dbp;

    public static bloodResponseDTO of(BloodPressure blood){

        // 소수점 첫째자리로 반올림
        float roundedSbp = Math.round(blood.getSbp() * 10) / 10.0f;
        float roundedDbp = Math.round(blood.getDbp() * 10) / 10.0f;

        return bloodResponseDTO.builder()
                .bloodId(blood.getId())
                .sbp(roundedSbp)
                .dbp(roundedDbp)
                .build();
    }

}
