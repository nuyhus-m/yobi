package com.S209.yobi.domain.measures.service;

public class HealthCalculatorService {
        /**
         * 수축기 혈압(SBP) 레벨 계산
         */
        public static String calculateSbpLevel(float sbp) {
        if (sbp < 90) return "낮음";       // 저혈압
        if (sbp < 130) return "보통";      // 정상 ~ 상승
        return "높음";                     // 고혈압
    }

        /**
         * 이완기 혈압(DBP) 레벨 계산
         */
        public static String calculateDbpLevel(float dbp) {
        if (dbp < 60) return "낮음";       // 저혈압
        if (dbp < 85) return "보통";       // 정상 ~ 상승
        return "높음";                     // 고혈압
    }
}
