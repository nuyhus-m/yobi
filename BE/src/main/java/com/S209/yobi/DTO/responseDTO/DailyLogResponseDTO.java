package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class DailyLogResponseDTO implements ApiResult {
    // 여러 일지 정보를 담는 리스트
    private List<SimpleDailyLogDTO> dailyLogs;

    /*
    * 간략한 일지 정보를 담는 내부 클래스
    * */
    @Getter
    @Builder
    @Schema
    public static class SimpleDailyLogDTO implements ApiResult {
        private Integer scheduleId;
        private String clientName;
        private long visitedDate;
    }

    /*
    * 상세 일지를 담는 내부 클래스
    * 개별 일지 */
    @Getter
    @Builder
    @Schema
    public static class DailyLogDetailDTO implements ApiResult {
        private String logContent;
        private String clientName;
        private long visitedDate;
    }
}
