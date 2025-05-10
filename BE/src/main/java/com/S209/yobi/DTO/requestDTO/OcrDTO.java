package com.S209.yobi.DTO.requestDTO;

import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.*;

import java.util.List;

public class OcrDTO {

    @Getter
    @Setter
    public static class OcrResponseDTO implements ApiResult {
        private List<ScheduleItem> schedules;

        @Getter
        @Setter
        public static class ScheduleItem {
            private Integer day;
            private String startAt;
            private String endAt;
            private String clientName;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrResultDTO implements ApiResult {
        private Integer count;

        public static OcrResultDTO from(int count) {
            return OcrResultDTO.builder()
                    .count(count)
                    .build();
        }
    }
}

