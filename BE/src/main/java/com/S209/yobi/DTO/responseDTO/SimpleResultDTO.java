package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleResultDTO<T> implements ApiResult {
    private T data;
}
