package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BodyRequestDTO {

    @NotNull(message = "sbp 은 필수값입니다.")
    private Float bfp;

    @NotNull(message = "bfm 은 필수값입니다.")
    private Float bfm;

    @NotNull(message = "smm 은 필수값입니다.")
    private Float smm;

    @NotNull(message = "bmr 은 필수값입니다.")
    private Float bmr;

    @NotNull(message = "icw 은 필수값입니다.")
    private Float icw;

    @NotNull(message = "ecw 은 필수값입니다.")
    private Float ecw;

    @NotNull(message = "ecf 은 필수값입니다.")
    private Float ecf;

    @NotNull(message = "protein 은 필수값입니다.")
    private Float protein;

    @NotNull(message = "mineral 은 필수값입니다.")
    private Float mineral;

    @NotNull(message = "bodyAge 은 필수값입니다.")
    private Short bodyAge;

}
