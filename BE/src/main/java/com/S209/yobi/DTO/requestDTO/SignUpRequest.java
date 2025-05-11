package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "사번은 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]{5}$", message = "사번은 5자리 숫자여야 합니다.")
    private String employeeId;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상의 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;
} 