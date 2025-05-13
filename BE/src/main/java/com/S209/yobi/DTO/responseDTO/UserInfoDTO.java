package com.S209.yobi.DTO.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Integer userId;
    private String name;
    private Integer employeeNumber;
    private String image;
    private Boolean consent;
} 