package com.S209.yobi.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponseDTO<T> success(T data){
        return new ApiResponseDTO<>("200","success",data);
    }

    public static <T> ApiResponseDTO<T> fail(String code, String message){
        return new ApiResponseDTO<>(code, message, null);
    }


}
