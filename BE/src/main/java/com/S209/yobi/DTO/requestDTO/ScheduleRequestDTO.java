package com.S209.yobi.DTO.requestDTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequestDTO {


    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScheduleCreateRequestDTO {
        @NotNull(message = "clientId는 필수값입니다.")
        private Integer clientId;

        @NotNull(message = "visitedDate는 필수값입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "방문 날짜", example = "2025-05-01", type = "string")
        private LocalDate visitedDate;

        @NotNull(message = "startAt은 필수값입니다.")
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(description = "시작 시간", example = "09:00:00", type = "string")
        private LocalTime startAt;

        @NotNull(message = "endAt은 필수값입니다.")
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(description = "종료 시간", example = "10:00:00", type = "string")
        private LocalTime endAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScheduleUpdateRequestDTO {
        private Integer clientId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "방문 날짜", example = "2025-05-01", type = "string")
        private LocalDate visitedDate;

        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(description = "시작 시간", example = "09:00:00", type = "string")
        private LocalTime startAt;

        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(description = "종료 시간", example = "10:00:00", type = "string")
        private LocalTime endAt;
    }


}
