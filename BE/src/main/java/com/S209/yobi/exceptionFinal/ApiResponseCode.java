package com.S209.yobi.exceptionFinal;

import org.apache.tomcat.util.http.parser.HttpParser;
import org.springframework.http.HttpStatus;

import java.util.Arrays;


public enum ApiResponseCode {

    // 성공
    SUCCESS("200", "요청이 성공했습니다.", HttpStatus.OK),

    // 실패
    BAD_REQUEST("400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND_MEASURE("404-1", "먼저 체성분과 혈압을 측정해야 합니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_USER("404-2", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_CLIENT("404-4", "해당하는 클라이언트가 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_REPORT("404-5", "해당하는 리포트가 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_BODY_COMPOSITION("404-6", "체성분 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_FOUND_RESOURCE("404-7", "연결된 측정 데이터를 찾을 수 없음", HttpStatus.NOT_FOUND),
    DUPLICATE_MEASURE("409", "이미 오늘 측정이 완료되었습니다.", HttpStatus.CONFLICT),
    IMAGE_SERVER_ERROR("500-1", "이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    CREATE_CLIENT_ERROR("500-2", "고객 생성 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVER_ERROR("500", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PERIOD_NO_INPUT("400-1", "시작일과 종료일을 입력해주세요.", HttpStatus.BAD_REQUEST),
    START_END_ERROR("400-2", "종료일이 시작일보다 빠를 수 없습니다.", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_WRONG("400-3", "이전 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD("400-4", "새 비밀번호와 이전 비밀번호가 동일합니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_NO_INPUT("400-5", "비밀번호가 비어 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT("400-6", "비밀번호에는 @$!%*#?& 특수문자만 사용할 수 있습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMPLOYEE_NUMBER("400-7", "이미 존재하는 사번입니다.", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD_FORMAT("400-8", "비밀번호에는 영문, 숫자, 특수문자가 1개 이상 포함되어야 합니다.", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD_LENGTH("400-9", "비밀번호는 8자 이상 15자 이하만 입력 가능합니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_SCHEDULE_TIME("400-10", "해당 시간에 이미 일정이 있습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_DATE_CLIENT("400-11", "해당 날짜, 해당 클라이언트 조합의 일정이 이미 있습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("403-1", "권한이 없습니다.", HttpStatus.UNAUTHORIZED),
    NOT_USERS_CLIENT("403-2", "사용자의 담당 클라이언트가 아닙니다.", HttpStatus.UNAUTHORIZED);

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
