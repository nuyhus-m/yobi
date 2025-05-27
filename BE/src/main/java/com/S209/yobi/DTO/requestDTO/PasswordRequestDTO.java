package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordRequestDTO {

    @NotNull
    String oldPassword;

    @NotNull
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    String newPassword;
}
