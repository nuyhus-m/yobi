package com.S209.yobi.DTO.responseDTO;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GraphPointDTO {
    private LocalDate date;
    private Object value;
}
