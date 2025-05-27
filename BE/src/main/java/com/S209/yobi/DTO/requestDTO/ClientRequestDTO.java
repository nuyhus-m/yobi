package com.S209.yobi.DTO.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequestDTO {

    @NotNull
    private Integer userId;

    @Size(max = 10)
    @NotNull
    private String name;

    @NotNull
    private LocalDate birth;

    @NotNull
    private Integer gender;

    @NotNull
    private Float height;

    @NotNull
    private Float weight;

    @Size(max = 100)
    @NotNull
    private String address;

    // 이미지 파일은 MultipartFile로 받음
    private MultipartFile image;
}