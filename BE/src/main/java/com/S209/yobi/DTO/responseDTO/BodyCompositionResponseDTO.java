package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.BodyComposition;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class BodyCompositionResponseDTO {
    private Long compositionId;
    private MeasureWithLevel bfp;
    private MeasureWithLevel  bfm;
    private MeasureWithLevel  smm;
    private MeasureWithLevel  bmr;
    private MeasureWithLevel  ecf;
    private MeasureWithLevel  protein;
    private MeasureWithLevel  mineral;
    private short  bodyAge;

    public static BodyCompositionResponseDTO of(BodyComposition body, Map<String, String> redisLevels){

        // 소수점 첫째자리까지 반올림
        float roundedBfp = Math.round(body.getBfp() * 10) / 10.0f;
        float roundedBfm = Math.round(body.getBfm() * 10) / 10.0f;
        float roundedSmm = Math.round(body.getSmm() * 10) / 10.0f;
        float roundedEcf = Math.round(body.getEcf() * 10) / 10.0f;
        float roundedProtein = Math.round(body.getProtein() * 10) / 10.0f;
        float roundedMineral = Math.round(body.getMineral() * 10) / 10.0f;

        // 정수에서 반올림
        int roundedBmr = Math.round(body.getBmr());

        return BodyCompositionResponseDTO.builder()
                .compositionId(body.getId())
                .bfp(new MeasureWithLevel(roundedBfp, redisLevels.get("bfp")))
                .bfm(new MeasureWithLevel(roundedBfm, redisLevels.get("bfm")))
                .smm(new MeasureWithLevel(roundedSmm, redisLevels.get("smm")))
                .bmr(new MeasureWithLevel((float) roundedBmr, redisLevels.get("bmr")))
                .ecf(new MeasureWithLevel(roundedEcf, redisLevels.get("ecf")))
                .protein(new MeasureWithLevel(roundedProtein, redisLevels.get("protein")))
                .mineral(new MeasureWithLevel(roundedMineral, redisLevels.get("mineral")))
                .bodyAge(body.getBodyAge())
                .build();
    }



}
