package com.S209.yobi.DTO.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        @Schema(description = "방문 날짜 타임스탬프(밀리초)", example = "1735660800000", type = "number", format = "int64")
        private long visitedDate;

        @NotNull(message = "startAt은 필수값입니다.")
        @Schema(description = "시작 시간 타임스탬프(밀리초)", example = "1735696800000", type = "number", format = "int64")
        private long startAt;

        @NotNull(message = "endAt은 필수값입니다.")
        @Schema(description = "종료 시간 타임스탬프(밀리초)", example = "1735707600000", type = "number", format = "int64")
        private long endAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScheduleUpdateRequestDTO {
        private Integer clientId;

        @Schema(description = "방문 날짜 타임스탬프(밀리초)", example = "1735660800000", type = "number", format = "int64")
        private Long visitedDate;

        @Schema(description = "시작 시간 타임스탬프(밀리초)", example = "1735696800000", type = "number", format = "int64")
        private Long startAt;

        @Schema(description = "종료 시간 타임스탬프(밀리초)", example = "1735707600000", type = "number", format = "int64")
        private Long endAt;
    }
}