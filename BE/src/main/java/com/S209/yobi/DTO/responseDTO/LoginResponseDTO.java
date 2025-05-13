package com.S209.yobi.DTO.responseDTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Integer userId;
    private String name;
    private String employeeId;
} 