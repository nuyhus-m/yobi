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

    /**
     * 심박수(BPM) 레벨 계산
     */
    public static String calculateBpmLevel(float bpm) {
        if (bpm < 60) return "낮음";      // 서맥
        if (bpm <= 100) return "보통";    // 정상
        return "높음";                   // 빈맥
    }

    /**
     * 산소포화도(Oxygen) 레벨 계산
     */
    public static String calculateOxygenLevel(float oxygen) {
        if (oxygen < 90) return "낮음";      // 위험 ~ 낮은 수준
        if (oxygen < 98) return "보통";      // 정상 범위 내 낮은 수준
        return "높음";                      // 정상 범위 내 높은 수준
    }

    /**
     * 체온(Temperature) 레벨 계산
     */
    public static String calculateTemperatureLevel(float temp) {
        if (temp < 36.0) return "낮음";    // 저체온
        if (temp < 37.2) return "보통";   // 정상 체온
        return "높음";                    // 고열
    }
}
