package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BaseRequestDTO {

    @NotNull(message = "clientId는 필수값입니다.")
    private Integer clientId;

    @NotNull(message = "bodyComposition은 필수값입니다.")
    private BodyCompositionDTO bodyCompositionDTO;

    @NotNull(message = "bloodPressure은 필수값입니다.")
    private BloodPressureDTO bloodPressureDTO;

}
