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
    private List<SimpleDailyLogDTO> dailyLogs;

    @Getter
    @Builder
    @Schema
    public static class SimpleDailyLogDTO implements ApiResult {
        private Integer scheduleId;
        private String clientName;
        private LocalDate visitedDate;
    }

    @Getter
    @Builder
    @Schema
    public static class DailyLogDetailDTO implements ApiResult {
        private String logContent;
        private String clientName;
        private LocalDate visitedDate;
    }
}
