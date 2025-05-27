package com.S209.yobi.domain.measures.helper;

public class Utils {

    /**
     * 성별 문자열을 기준으로 남자인지 판별
     * @param gender 성별 (male/femal)
     * @return true: 남성 / false: 여성 또는 null
     */
    public static boolean isMale(String gender) {
        if (gender == null) return false;
        return "male".equals(gender);
    }
}
