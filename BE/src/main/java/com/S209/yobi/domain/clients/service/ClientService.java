package com.S209.yobi.domain.clients.service;

import com.S209.yobi.DTO.requestDTO.ClientRequestDTO;
import com.S209.yobi.DTO.responseDTO.ClientResponseDTO;
import com.S209.yobi.S3Service;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public ApiResult createClient(ClientRequestDTO requestDTO) {
        try {
            // 이미지 파일 업로드 및 URL 획득
            String imageUrl = null;
            if (requestDTO.getImage() != null && !requestDTO.getImage().isEmpty()) {
                imageUrl = s3Service.uploadFile(requestDTO.getImage());
            }

            User user = userRepository.findById(requestDTO.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

            Client client = Client.builder()
                    .user(user)
                    .name(requestDTO.getName())
                    .birth(requestDTO.getBirth())
                    .gender(requestDTO.getGender())
                    .height(requestDTO.getHeight())
                    .weight(requestDTO.getWeight())
                    .image(imageUrl)
                    .address(requestDTO.getAddress())
                    .build();

            clientRepository.save(client);

            return null;
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            return ApiResponseDTO.fail(ApiResponseCode.IMAGE_SERVER_ERROR);
        } catch (Exception e) {
            log.error("고객 생성 중 오류 발생: {}", e.getMessage());
            return ApiResponseDTO.fail(ApiResponseCode.CREATE_CLIENT_ERROR);
        }
    }

    // 특정 요양보호사의 돌봄 대상 리스트
    public ApiResult getClientsList(Integer userId) {
        List<Client> clients = clientRepository.findByUserId(userId);

        return ClientResponseDTO.fromList(clients);
    }

    // 특정 요양보호사의 특정 돌봄 대상 상세보기
    public ApiResult getClientDetail(Integer clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("client를 찾을 수 없습니다."));

        return ClientResponseDTO.ClientDTO.from(client);
    }
}