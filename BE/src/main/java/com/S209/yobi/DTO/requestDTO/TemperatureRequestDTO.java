package com.S209.yobi.DTO.requestDTO;

import com.S209.yobi.domain.measures.entity.Temperature;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class TemperatureRequestDTO {

    @NotNull(message = "temperature 은 필수값입니다.")
    private Float temperature;


}
