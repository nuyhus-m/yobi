package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.BodyComposition;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class BodyMainResponseDTO {
    private Long compositionId;
    private MeasureWithLevel bfp;
    private MeasureWithLevel bmr;
    private MeasureWithLevel ecf;

    public static BodyMainResponseDTO of(BodyComposition body, Map<String, String> redisLevels){

        // 소수점 첫째자리까지 반올림
        float roundedBfp = Math.round(body.getBfp() * 10) / 10.0f;
        float roundedEcf = Math.round(body.getEcf() * 10) / 10.0f;

        // 소수점 첫째자리에서 반올림
        int roundedBmr = Math.round(body.getBmr());

        return BodyMainResponseDTO.builder()
                .compositionId(body.getId())
                .bfp(new MeasureWithLevel(roundedBfp, redisLevels.get("bfp")))
                .bmr(new MeasureWithLevel(roundedBmr, redisLevels.get("bmr")))
                .ecf(new MeasureWithLevel(roundedEcf,redisLevels.get("ecf")))
                .build();
    }


}
