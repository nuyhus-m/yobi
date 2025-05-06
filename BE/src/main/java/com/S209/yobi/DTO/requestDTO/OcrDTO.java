package com.S209.yobi.DTO.requestDTO;

import lombok.*;

import java.util.List;

public class OcrDTO {

    @Getter
    @Setter
    public static class OcrResponseDTO {
        private Integer userId;
        private Integer year;
        private Integer month;
        private List<ScheduleItem> schedules;

        @Getter
        @Setter
        public static class ScheduleItem {
            private String date;
            private String startAt;
            private String endAt;
            private Integer clientId;
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

