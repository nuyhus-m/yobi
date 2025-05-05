package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequestDto {

    @NotNull(message = "clientId는 필수값입니다.")
    private Integer clientId;

    @NotNull(message = "visitedDate는 필수값입니다.")
    private LocalDate visitedDate;

    @NotNull(message = "startAt은 필수값입니다.")
    private LocalTime startAt;

    @NotNull(message = "endAt은 필수값입니다.")
    private LocalTime endAt;

}
