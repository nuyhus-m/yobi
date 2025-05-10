package com.S209.yobi.domain.measures.helper;

import lombok.*;

@Getter
@Setter
@Builder
public class BodyCompResultVo {
    private int genBfp;       // 체지방률
    private int genBfm;       // 체지방량
    private int genSmm;       // 근육량
    private int genBmr;       // 기초대사량
//    private int genBmi;       // BMI
//    private int genWeight;    // 체중
//    private int genBwp;       // 체수분 비율
    private int genProtein;   // 단백질
    private int genMinerals;  // 무기질
    private int genEcf;       // 세포외수분비
}
