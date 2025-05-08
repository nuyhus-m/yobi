package com.S209.yobi.exceptionFinal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HttpStatusCode {
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private final int status;

    public HttpStatus toHttpStatus() {
        return HttpStatus.valueOf(this.status);
    }
}
