package com.S209.yobi.exception;

import org.springframework.http.HttpStatus;

public class ResponseMapper {

    public static HttpStatus getStatusFrom(ApiResponseDTO<?> response) {
        return switch (response.getCode()) {
            case "200" -> HttpStatus.OK;
            case "409" -> HttpStatus.CONFLICT;
            case "404" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}

