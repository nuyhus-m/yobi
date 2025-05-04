package com.S209.yobi.DTO.requestDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StressDTO {
    private Integer clientId;
    private Short stressValue;
    private String stressLevel;
    private Short oxygen;
    private Short bpm;
}
