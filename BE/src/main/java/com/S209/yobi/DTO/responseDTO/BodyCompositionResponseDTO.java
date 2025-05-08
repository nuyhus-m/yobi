package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.BodyComposition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BodyCompositionResponseDTO {
    private Long compositionId;
    private Float bfp;
    private Float bfm;
    private Float smm;
    private int bmr;
    private Float tbw;
    private Float protein;
    private Float mineral;
    private short bodyAge;

    public static BodyCompositionResponseDTO of(BodyComposition body){

        // 소수점 첫째자리까지 반올림
        float roundedBfp = Math.round(body.getBfp() * 10) / 10.0f;
        float roundedBfm = Math.round(body.getBfm() * 10) / 10.0f;
        float roundedSmm = Math.round(body.getSmm() * 10) / 10.0f;
        float roundedTbm = Math.round((body.getIcw() + body.getEcw()) * 10) / 10.0f;
        float roundedProtein = Math.round(body.getProtein() * 10) / 10.0f;
        float roundedMineral = Math.round(body.getMineral() * 10) / 10.0f;

        // 정수에서 반올림
        int roundedBmr = Math.round(body.getBmr());

        return BodyCompositionResponseDTO.builder()
//                .compositionId(body.getId())
                .bfp(roundedBfp)
                .bfm(roundedBfm)
                .smm(roundedSmm)
                .bmr(roundedBmr)
                .tbw(roundedTbm)
                .protein(roundedProtein)
                .mineral(roundedMineral)
                .bodyAge(body.getBodyage())
                .build();
    }

}
