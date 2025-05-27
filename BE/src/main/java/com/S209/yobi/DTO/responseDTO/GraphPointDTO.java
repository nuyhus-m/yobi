package com.S209.yobi.DTO.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class GraphPointDTO {
    private long date;
    private Number value;
}
