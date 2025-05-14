package com.S209.yobi.exceptionFinal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDTO<T> implements ApiResult {

    private String code;
    private String message;
    private T data;

    public ApiResponseDTO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<T>("200", "success", data);
    }

    // 예외처리 응답
    public static <T> ApiResponseDTO<T> fail(String code, String message) {
        return new ApiResponseDTO<T>(code, message, null);
    }

    // 실패 응답 (Enum version)
    public static <T> ApiResponseDTO<T> fail(ApiResponseCode code) {
        return new ApiResponseDTO<T>(code.getCode(), code.getMessage(), null);
    }
}
