package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CheckBaseDTO {

    @NotNull(message = "clientId 은 필수값입니다.")
    private Integer clientId;
}
