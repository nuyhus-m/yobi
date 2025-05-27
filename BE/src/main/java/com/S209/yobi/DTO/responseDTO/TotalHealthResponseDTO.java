package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class TotalHealthResponseDTO implements ApiResult {
    private Integer clientId;
    private Map<String, List<GraphPointDTO>> bodyComposition;
    private Map<String, List<GraphPointDTO>> bloodPressure;
    private Map<String, List<GraphPointDTO>> stress;

}
