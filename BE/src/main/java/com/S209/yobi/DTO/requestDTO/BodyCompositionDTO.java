package com.S209.yobi.DTO.requestDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BodyCompositionDTO {
    private Float bfp;
    private Float bfm;
    private Float smm;
    private Float bmr;
    private Float icw;
    private Float protein;
    private Float mineral;
    private Short bodyage;

}
