package com.S209.yobi.DTO.requestDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BloodPressureDTO {
    private Float sbp;
    private Float dbp;

}
