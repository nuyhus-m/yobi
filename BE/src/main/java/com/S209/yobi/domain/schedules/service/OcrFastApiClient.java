package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.requestDTO.OcrDTO.OcrResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrFastApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.url}")
    private String fastApiUrl;

    public OcrResponseDTO processImage(MultipartFile image) {
        try {
            log.info("FastAPI 서버로 OCR 요청 시작 - URL: {}", fastApiUrl + "/api/schedules/ocr");
            
            //멀티파트 요청 생성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", createFileResource(image));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            log.info("FastAPI 요청 생성 완료");

            // FastAPI 서버에 요청 전송
            String url = fastApiUrl + "/api/schedules/ocr";
            log.info("FastAPI 서버로 요청 전송 - URL: {}", url);
            
            ResponseEntity<OcrResponseDTO> response = restTemplate.postForEntity(
                    url, requestEntity, OcrResponseDTO.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("FastAPI 서버 응답 성공 - schedules={}",
                        response.getBody().getSchedules().size());
                return response.getBody();
            } else {
                log.error("FastAPI 서버 오류 응답 - status: {}", response.getStatusCodeValue());
                throw new RuntimeException("FastAPI 서버 오류: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            log.error("FastAPI 서버 요청 중 오류 발생", e);
            throw new RuntimeException(e);
        }
    }

    private ByteArrayResource createFileResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }
}
