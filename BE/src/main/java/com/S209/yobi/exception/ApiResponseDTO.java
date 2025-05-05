package com.S209.yobi.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {

    private String code;
    private String message;
    private T data;

    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>("200", "success", data);
    }

    public static <T> ApiResponseDTO<T> fail(String code, String message) {
        return new ApiResponseDTO<>(code, message, null);
    }

    // 실패 응답 (Enum version)
    public static <T> ApiResponseDTO<T> fail(ApiResponseCode code) {
        return new ApiResponseDTO<>(code.getCode(), code.getMessage(), null);
    }


}
