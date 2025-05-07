package com.S209.yobi.DTO.requestDTO;

import lombok.*;

import java.util.List;

public class OcrDTO {

    @Getter
    @Setter
    public static class OcrResponseDTO {
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
    public static class OcrResultDTO  {
        private Integer count;
    }
}

