package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
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
public class DailyLogResponseDTO implements ApiResult {
    private List<SimpleDailyLogDTO> dailyLogs;

    @Getter
    @Builder
    public static class SimpleDailyLogDTO implements ApiResult {
        private Integer scheduleId;
        private String clientName;
        private LocalDate visitedDate;
    }

    @Getter
    @Builder
    public static class DailyLogDetailDTO implements ApiResult {
        private String logContent;
        private String clientName;
        private LocalDate visitedDate;
    }
}
