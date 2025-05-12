package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {
    @NotNull(message = "사번은 필수 입력값입니다.")
    private String employeeNumber;

    @NotNull(message = "비밀번호는 필수 입력값입니다.")
    private String password;
} 