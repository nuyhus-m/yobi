package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ReBloodPressureDTO {

    @NotNull(message = "clientId 은 필수값입니다.")
    private Integer clientId;

    @NotNull(message = "sbp 은 필수값입니다.")
    private Float sbp;

    @NotNull(message = "dbp 은 필수값입니다.")
    private Float dbp;
}
