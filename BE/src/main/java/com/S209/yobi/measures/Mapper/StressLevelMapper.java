package com.S209.yobi.measures.Mapper;

import com.S209.yobi.measures.Enum.StressLevel;

public class StressLevelMapper {

    /**
     *  영어 -> 한글로 번역
     *
     */
    public static String toClient(StressLevel stressLevel){
        if (stressLevel == null) return null;

        return  switch(stressLevel){
            case LOW -> "낮음";
            case MID -> "보통";
            case HIGH -> "높음";
        };
    }
}
