package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.report.entity.WeeklyReport;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportListDTO implements ApiResult {

    private List<ReportDTO> reports;

        public static ReportListDTO of(List<WeeklyReport> weeklyReports){

            List<ReportDTO> reportDTOs = weeklyReports.stream()
                    .map(ReportDTO::fromEntity)
                    .toList();
            return ReportListDTO.builder()
                    .reports(reportDTOs)
                    .build();

    }

    @Getter
    @Builder
    public static class ReportDTO{
            private Long reportId;
            private Long createdAt;

            public static ReportDTO fromEntity(WeeklyReport report){
                return ReportDTO.builder()
                        .reportId(report.getId())
                        .createdAt(report.getCreatedAt())
                        .build();
            }
    }

}
