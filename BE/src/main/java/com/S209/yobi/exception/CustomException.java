package com.S209.yobi.exception;

import lombok.Getter;

@Getter

// 공통 예외 클래스
public class CustomException extends RuntimeException{

    private final HttpStatusCode statusCode;
    private final String message;
    private final Enum<?> errorCode;

    public CustomException(Enum<?> errorCode, HttpStatusCode statusCode, String message){
        super(message);
        this.statusCode = statusCode;
        this.message = message;
        this.errorCode = errorCode;
    }

}
