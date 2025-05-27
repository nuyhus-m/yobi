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
    // 여러 클라이언트의 정보를 담는 리스트
    private List<ClientDTO> clients;

    /*
    * 개별 클라이언트 정보를 담는 내부 클래스
    * 각 클라이언트의 상세 정보를 포함
    * */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ClientDTO implements ApiResult {
        private Integer clientId  ;
        private String name;
        private LocalDate birth;
        private Integer gender;
        private Float height;
        private Float weight;
        private String image;
        private String address;

        /*
        * Client 엔티티를 ClientDTO로 변환하는 정적 메서드
        * 단일 클라이언트 정보 변환시 사용
        * */
        public static ClientDTO from(Client client) {
            return ClientDTO.builder()
                    .clientId(client.getId())
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


    /*
    * 클라이언트 엔티티 리스트를 DTO 형태로 변환하는 메서드
    * 여러 클라이언트 저보를 한번에 변환시 사용
    * */
    public static ClientResponseDTO fromList(List<Client> clients) {
        // 클라이언트 엔티티 리스트를 DTO 리스트로 스트림 변환
        List<ClientDTO> clientDTOs = clients.stream()
                .map(client -> {
                    // 개별 클라이언트 엔티티를 DTO로 변환
                    return ClientDTO.builder()
                            .clientId(client.getId())
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