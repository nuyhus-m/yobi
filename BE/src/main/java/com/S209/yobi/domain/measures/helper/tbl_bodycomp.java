package com.S209.yobi.domain.measures.helper;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.BodyComposition;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class tbl_bodycomp {
    private int uAge;
    private String uGender; // 예: "male" 또는 "female"
    private float uHeight;
    private float uWeight;
    private double bfp;       // 체지방률
    private double bfm;
    private double bmr;       // 기초대사량
    private double smm;       // 근육량
    private double protein;
    private double minerals;
    private double ecf;
//    private double bwp;       // 체수분 비율
//    private double bmi;

    public static tbl_bodycomp of(Client client, BodyComposition body){
        return tbl_bodycomp.builder()
                .uGender(client.getGender() != 0 ? "male" : "female")
                .uHeight(client.getHeight())
                .uWeight(client.getWeight())
                .bfp(body.getBfp())
                .bfm(body.getBfm())
                .bmr(body.getBmr())
                .smm(body.getSmm())
                .protein(body.getProtein())
                .minerals(body.getMineral())
                .ecf(body.getEcf())
                .build();
    }


}
