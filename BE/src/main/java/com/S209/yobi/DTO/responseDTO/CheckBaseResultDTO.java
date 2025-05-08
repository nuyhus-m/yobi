package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class CheckBaseResultDTO implements ApiResult {
    private boolean measured;

    public static CheckBaseResultDTO of(Boolean result){
        return CheckBaseResultDTO.builder().measured(result).build();
    }
}
