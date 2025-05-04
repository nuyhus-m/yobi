package com.S209.yobi.exception;



import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.BindException;
import java.nio.file.AccessDeniedException;

@ControllerAdvice
@Slf4j
// 전역 예외 처리기
public class GlobalExceptionHandler {


    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleCustomException(CustomException ex) {
        HttpStatus httpStatus = ex.getStatusCode().toHttpStatus();
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponseDTO.fail(
                        ex.getErrorCode().toString(),
                        ex.getMessage()
                ));
    }


    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponseDTO<?>> handleBadRequest(Exception ex,  HttpServletRequest request) {

        log.warn("Bad request at [{}] {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.fail("400", "요청이 올바르지 않습니다. 입력 값을 다시 확인해주세요."));
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleAccessDenied(AccessDeniedException ex) {

        log.warn("AccessDeniedException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.fail("403", "요청하신 작업에 대한 권한이 없습니다."));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleEntityNotFound(EntityNotFoundException ex) {

        log.warn("EntityNotFoundException: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.fail("404", "요청한 리소스를 찾을 수 없습니다."));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<?>> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}] {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.fail("500", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }



}
