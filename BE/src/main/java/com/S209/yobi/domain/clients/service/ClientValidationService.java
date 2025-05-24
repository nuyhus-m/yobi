package com.S209.yobi.domain.clients.service;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientValidationService {

    private final ClientRepository clientRepository;

    public Optional<Client> validateClient(int clientId){

        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            log.info("해당하는 클라이언트 없음, [clientId:{}]", clientId);
        }
        return client;
    }
}
