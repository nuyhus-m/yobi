package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.measures.Enum.StressLevel;
import com.S209.yobi.measures.Mapper.StressLevelMapper;
import com.S209.yobi.measures.entity.Stress;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StressResponseDTO {
    private Long stressId;
    private int stressValue;
    private String stressLevel;

    public static StressResponseDTO of(Stress stress){
        return StressResponseDTO.builder()
                .stressId(stress.getId())
                .stressValue(stress.getStressValue())
                .stressLevel(StressLevelMapper.toClient(stress.getStressLevel()))
                .build();
    }
}
