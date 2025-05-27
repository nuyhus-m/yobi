package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.measures.entity.Temperature;
import com.S209.yobi.domain.measures.service.HealthCalculatorService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemperatureResponseDTO {
    private Long temperatureId;
    private MeasureWithLevel temperature;

    public static TemperatureResponseDTO of(Temperature temperature) {
        if (temperature == null) {
            return null;
        }

        String tempLevel = HealthCalculatorService.calculateTemperatureLevel(temperature.getTemperature());

        return TemperatureResponseDTO.builder()
                .temperatureId(temperature.getId())
                .temperature(new MeasureWithLevel(temperature.getTemperature(), tempLevel))
                .build();
    }
}