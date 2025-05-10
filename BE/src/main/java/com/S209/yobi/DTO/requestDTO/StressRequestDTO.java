package com.S209.yobi.DTO.requestDTO;

import com.S209.yobi.domain.measures.Enum.StressLevel;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class StressRequestDTO {


    @NotNull(message = "stressValue 은 필수값입니다.")
    private Short stressValue;

    @NotNull(message = "stressLevel 은 필수값입니다.")
    private StressLevel stressLevel;

    @NotNull(message = "oxygen 은 필수값입니다.")
    private Short oxygen;

    @NotNull(message = "bpm 은 필수값입니다.")
    private Short bpm;
}
