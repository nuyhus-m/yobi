package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.entity.HeartRate;
import com.S209.yobi.measures.entity.Measure;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainHealthResponseDTO {

    private Integer clientId;
    private BodyMainResponseDTO bodyMain;
    private StressResponseDTO stress;
    private HeartRateResponseDTO heartRate;
    private bloodResponseDTO blood;

    public static MainHealthResponseDTO of(Measure m){
        return MainHealthResponseDTO.builder()
                .clientId(m.getClient().getId())
                .bodyMain(BodyMainResponseDTO.of(m.getBody()))
                .stress(m.getStress() != null ? StressResponseDTO.of(m.getStress()): null)
                .heartRate(m.getHeart() != null ?HeartRateResponseDTO.of(m.getHeart()): null)
                .blood(bloodResponseDTO.of(m.getBlood()))
                .build();
    }

}
