package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.clients.entity.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO { // 클래스 이름도 DTO 접미사를 일관되게 적용
    private Integer id;
    private String name;
    private LocalDate birth;
    private Integer gender;
    private Double height;
    private Double weight;
    private String image;
    private String address;

    public static ClientResponseDTO from(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .birth(client.getBirth())
                .gender(client.getGender())
                .height(client.getHeight())
                .weight(client.getWeight())
                .image(client.getImage())
                .address(client.getAddress())
                .build();
    }
}