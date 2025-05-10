package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.Mapper.StressLevelMapper;
import com.S209.yobi.domain.measures.entity.Stress;
import com.S209.yobi.domain.measures.entity.Temperature;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemperatureResponseDTO {
    private Long temperatureId;
    private Float temperature;

    public static TemperatureResponseDTO of(Temperature temperature){
        return TemperatureResponseDTO.builder()
                .temperatureId(temperature.getId())
                .temperature(temperature.getTemperature())
                .build();
    }

}
