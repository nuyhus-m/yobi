package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.entity.BodyComposition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BodyMainResponseDTO {
    private Long compositionId;
    private Float bfp;
    private int bmr;
    private Float tbw;

    public static BodyMainResponseDTO of(BodyComposition body){

        // 소수점 첫째자리로 반올림
        float roundedBfp = Math.round(body.getBfp() * 10) / 10.0f;
        float roundedTbw = Math.round((body.getIcw() + body.getEcw()) * 10) / 10.0f;

        return BodyMainResponseDTO.builder()
                .compositionId(body.getId())
                .bfp(roundedBfp)
                .bmr(Math.round(body.getBmr()))
                .tbw(roundedTbw)
                .build();
    }


}
