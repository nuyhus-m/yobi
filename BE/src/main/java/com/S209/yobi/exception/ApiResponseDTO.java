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
    private int httpStatus;

    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>("200", "success", data, HttpStatus.OK.value());
    }

    public static <T> ApiResponseDTO<T> fail(String code, String message, HttpStatus status) {
        return new ApiResponseDTO<>(code, message, null, status.value());
    }


}
