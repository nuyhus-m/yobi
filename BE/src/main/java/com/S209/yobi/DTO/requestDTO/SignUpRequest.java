package com.S209.yobi.DTO.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @Size(max = 10, message = "이름은 10자 이하여야 합니다.")
    @NotNull(message = "이름은 필수 입력값입니다.")
    private String name;

    @NotNull(message = "사번은 필수 입력값입니다.")
    private Integer employeeNumber;

    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    @NotNull(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    @Schema(description = "프로필 이미지")
    private MultipartFile image;
} 