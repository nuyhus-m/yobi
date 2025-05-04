package com.S209.yobi.DTO.requestDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TemperatureDTO {
    private Integer clientId;
    private Float temperature;
}
