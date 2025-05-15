package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 다양한 측정 데이터 저장 API에 사용되는 통합 응답 DTO
 * - 피트러스 필수 데이터 저장 (체성분/혈압): {"bodyId": 3, "bloodId": 3}
 * - 피트러스 심박 측정 저장: {"heartRateId": 3}
 * - 피트러스 스트레스 데이터: {"stressId": 3}
 * - 피트러스 체온 데이터 저장: {"temperatureId": 3}
 * - 피트러스 체성분 데이터 저장(재측정): {"bodyId": 3}
 * - 피트러스 혈압 데이터 저장(재측정): {"bloodId": 3}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MeasureResponseDTO implements ApiResult {
    private Map<String, Long> ids;

    /**
     * 단일 ID를 포함하는 응답 생성 (심박, 스트레스, 체온, 재측정 등)
     * @param idName 저장된 ID의 키 이름 (예: "heartRateId", "stressId", 등)
     * @param id 저장된 ID 값
     * @return 생성된 응답 DTO
     */
    public static MeasureResponseDTO createWithSingleId(String idName, Long id) {
        Map<String, Long> idMap = new HashMap<>();
        log.debug("Creating single ID response. idName: {}, id: {}", idName, id);
        idMap.put(idName, id);
        return new MeasureResponseDTO(idMap);
    }

    /**
     * 체성분 및 혈압 ID를 포함하는 응답 생성 (필수 데이터 저장용)
     * @param bodyId 체성분 ID
     * @param bloodId 혈압 ID
     * @return 생성된 응답 DTO
     */
    public static MeasureResponseDTO createWithBaseIds(Long bodyId, Long bloodId) {
        Map<String, Long> idMap = new HashMap<>();
        log.debug("Creating base IDs response. bodyId: {}, bloodId: {}", bodyId, bloodId);
        idMap.put("bodyId", bodyId);
        idMap.put("bloodId", bloodId);
        return new MeasureResponseDTO(idMap);
    }
}