package com.S209.yobi.domain.clients.service;

import com.S209.yobi.DTO.requestDTO.ClientRequestDTO;
import com.S209.yobi.S3Service;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final S3Service s3Service;

    @Transactional
    public ApiResult createClient(ClientRequestDTO requestDTO) {
        try {
            // 이미지 파일 업로드 및 URL 획득
            String imageUrl = null;
            if (requestDTO.getImage() != null && !requestDTO.getImage().isEmpty()) {
                imageUrl = s3Service.uploadFile(requestDTO.getImage());
            }

            Client client = Client.builder()
                    .name(requestDTO.getName())
                    .birth(requestDTO.getBirth())
                    .gender(requestDTO.getGender())
                    .height(requestDTO.getHeight())
                    .weight(requestDTO.getWeight())
                    .image(imageUrl)
                    .address(requestDTO.getAddress())
                    .build();

            clientRepository.save(client);

            // 성공 시 null 반환 (주어진 예시 코드와 동일하게)
            return null;
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            return ApiResponseDTO.fail(ApiResponseCode.IMAGE_SERVER_ERROR);
        } catch (Exception e) {
            log.error("고객 생성 중 오류 발생: {}", e.getMessage());
            return ApiResponseDTO.fail(ApiResponseCode.CREATE_CLIENT_ERROR);
        }
    }
}