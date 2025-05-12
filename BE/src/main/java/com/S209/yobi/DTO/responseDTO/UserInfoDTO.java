package com.S209.yobi.DTO.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long userId;
    private String name;
    private String employeeNumber;
    private String image;
    private Boolean consent;
} 