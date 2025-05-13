package com.S209.yobi.DTO.responseDTO;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO implements ApiResult {
    private List<ClientDTO> clients;

    @Getter
    @Builder
    public static class ClientDTO implements ApiResult {
        private Integer id;
        private String name;
        private LocalDate birth;
        private Integer gender;
        private Double height;
        private Double weight;
        private String image;
        private String address;
    }


//    public static ClientResponseDTO from(Client client) {
//        return ClientResponseDTO.builder()
//                .id(client.getId())
//                .name(client.getName())
//                .birth(client.getBirth())
//                .gender(client.getGender())
//                .height(client.getHeight())
//                .weight(client.getWeight())
//                .image(client.getImage())
//                .address(client.getAddress())
//                .build();
//    }

    // 여러 client들을 반환하는 메소드
    public static ClientResponseDTO fromList(List<Client> clients) {
        List<ClientDTO> clientDTOs = clients.stream()
                .map(client -> {
                    return ClientDTO.builder()
                            .id(client.getId())
                            .name(client.getName())
                            .birth(client.getBirth())
                            .gender(client.getGender())
                            .height(client.getHeight())
                            .weight(client.getWeight())
                            .image(client.getImage())
                            .address(client.getAddress())
                            .build();
                })
                .collect(Collectors.toList());

        return new ClientResponseDTO(clientDTOs);
    }
}