package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class HeartRateRequestDTO {

    @NotNull(message = "clientId 은 필수값입니다.")
    private Integer clientId;

    @NotNull(message = "bpm 은 필수값입니다.")
    private Short bpm;

    @NotNull(message = "oxygen 은 필수값입니다.")
    private Short oxygen;

}
