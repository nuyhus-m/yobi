package com.S209.yobi.exceptionFinal;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


public enum ApiResponseCode {

    // 성공
    SUCCESS("200", "요청이 성공했습니다.", HttpStatus.OK),

    // 실패
    NOT_FOUND_MEASURE("404-1", "먼저 체성분과 혈압을 측정해야 합니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_USER("404-2", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CLIENT("404-4", "해당하는 클라이언트가 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_REPORT("404-5", "해당하는 리포트가 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_MEASURE("409", "이미 오늘 측정이 완료되었습니다.", HttpStatus.CONFLICT),
    IMAGE_SERVER_ERROR("500-1", "이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_CLIENT_ERROR("500-2", "고객 생성 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVER_ERROR("500", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PERIOD_NO_INPUT("400-1", "시작일과 종료일을 입력해주세요.", HttpStatus.BAD_REQUEST),
    START_END_ERROR("400-2", "종료일이 시작일보다 빠를 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ApiResponseCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }


    public static ApiResponseCode fromCode(String code) {
        return Arrays.stream(ApiResponseCode.values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(ApiResponseCode.SERVER_ERROR); // 기본 fallback
    }

}
