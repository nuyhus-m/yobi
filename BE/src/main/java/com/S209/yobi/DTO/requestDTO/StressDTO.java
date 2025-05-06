package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class StressDTO {

    @NotNull(message = "clientId 은 필수값입니다.")
    private Integer clientId;

    @NotNull(message = "stressValue 은 필수값입니다.")
    private Short stressValue;

    @NotNull(message = "stressLevel 은 필수값입니다.")
    private String stressLevel;

    @NotNull(message = "oxygen 은 필수값입니다.")
    private Short oxygen;

    @NotNull(message = "bpm 은 필수값입니다.")
    private Short bpm;
}
