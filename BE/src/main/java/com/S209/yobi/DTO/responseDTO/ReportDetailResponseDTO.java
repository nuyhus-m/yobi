package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.report.entity.WeeklyReport;
import com.S209.yobi.exceptionFinal.ApiResult;

import static com.S209.yobi.Mapper.DateTimeUtils.toEpochMilli;

import lombok.Builder;
import lombok.Getter;
import java.time.ZoneId;

@Getter
@Builder
public class ReportDetailResponseDTO implements ApiResult {

    private Long reportId;
    private String reportContent;
    private String logSummery;
    private long  createdAt;

    public static ReportDetailResponseDTO of (WeeklyReport weeklyReport){


        return ReportDetailResponseDTO.builder()
                .reportId(weeklyReport.getId())
                .reportContent(weeklyReport.getReportContent())
                .logSummery(weeklyReport.getLogSummary())
                .createdAt(weeklyReport.getCreatedAt())
                .build();
    }

}
