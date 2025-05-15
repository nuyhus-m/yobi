package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeasureResponseDTO implements ApiResult {
    private Map<String, Integer> ids;
}