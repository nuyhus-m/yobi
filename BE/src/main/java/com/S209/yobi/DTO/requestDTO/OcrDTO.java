package com.S209.yobi.DTO.requestDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.*;

import java.util.List;

public class OcrDTO {

    // FastAPI 응답을 받기 위한 내부 DTO 클래스 (서비스 내부에서만 사용)
    @Getter
    @Setter
    public static class FastApiResponseDTO {
        private List<FastApiScheduleItem> schedules;
        private Boolean formMatch;
        private Integer whichDay;

        @Getter
        @Setter
        public static class FastApiScheduleItem {
            private Integer day;
            private String startAt;
            private String endAt;
            private String clientName;
        }
    }

    // 클라이언트에게 반환할 DTO (타임스탬프만 포함)
    @Getter
    @Setter
    public static class OcrResponseDTO implements ApiResult {
        private List<ScheduleItem> schedules;
        private Boolean formMatch;
        private Integer whichDay;

        @Getter
        @Setter
        public static class ScheduleItem {
            // 타임스탬프 필드
            private Long dateTimestamp;
            private Long startTimestamp;
            private Long endTimestamp;
            private String clientName;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrResultDTO implements ApiResult {
        private Integer successCount;
        private Integer failCount;
        private List<String> failureReasons;
        private Boolean formMatch;

        public static OcrResultDTO of(int successCount, int failCount, List<String> failureReasons) {
            return OcrResultDTO.builder()
                    .successCount(successCount)
                    .failCount(failCount)
                    .failureReasons(failureReasons)
                    .build();
        }

        public static OcrResultDTO of(int successCount, int failCount, List<String> failureReasons, Boolean formMatch) {
            return OcrResultDTO.builder()
                    .successCount(successCount)
                    .failCount(failCount)
                    .failureReasons(failureReasons)
                    .formMatch(formMatch)
                    .build();
        }
    }
}