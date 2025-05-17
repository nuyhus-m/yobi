package com.S209.yobi.domain.schedules.service;

import com.S209.yobi.DTO.requestDTO.OcrDTO;
import com.S209.yobi.DTO.requestDTO.OcrDTO.FastApiResponseDTO;
import com.S209.yobi.DTO.requestDTO.OcrDTO.OcrResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrFastApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Spring Boot는 이 빈을 자동으로 제공합니다

    @Value("${fastapi.url}")
    private String fastApiUrl;

    public FastApiResponseDTO processImage(MultipartFile image) {
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

            // 먼저 응답을 문자열로 받아 로깅
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class
            );

            log.info("FastAPI 서버 응답 원본: {}", rawResponse.getBody());

            // JSON을 직접 파싱하여 필요한 정보 추출
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> jsonMap = mapper.readValue(rawResponse.getBody(), Map.class);

                // 새로운 FastApiResponseDTO 객체 생성
                FastApiResponseDTO dto = new FastApiResponseDTO();

                // schedules 필드 설정
                if (jsonMap.containsKey("schedules")) {
                    List<Map<String, Object>> schedulesMaps = (List<Map<String, Object>>) jsonMap.get("schedules");
                    List<FastApiResponseDTO.FastApiScheduleItem> scheduleItems = new ArrayList<>();

                    for (Map<String, Object> scheduleMap : schedulesMaps) {
                        FastApiResponseDTO.FastApiScheduleItem item = new FastApiResponseDTO.FastApiScheduleItem();

                        if (scheduleMap.containsKey("day")) {
                            item.setDay(((Number) scheduleMap.get("day")).intValue());
                        }

                        if (scheduleMap.containsKey("startAt")) {
                            item.setStartAt((String) scheduleMap.get("startAt"));
                        }

                        if (scheduleMap.containsKey("endAt")) {
                            item.setEndAt((String) scheduleMap.get("endAt"));
                        }

                        if (scheduleMap.containsKey("clientName")) {
                            item.setClientName((String) scheduleMap.get("clientName"));
                        }

                        scheduleItems.add(item);
                    }

                    dto.setSchedules(scheduleItems);
                }

                // whichDay 필드 읽고 저장
                if (jsonMap.containsKey("whichDay")) {
                    Object whichDayObj = jsonMap.get("whichDay");
                    if (whichDayObj instanceof Number) {
                        dto.setWhichDay(((Number) whichDayObj).intValue());
                        log.info("FastAPI 응답에서 whichDay 값 읽음: {}", dto.getWhichDay());
                    }
                } else {
                    // whichDay 필드가 없으면 null로 두고, 상위 코드에서 처리
                    log.info("FastAPI 응답에 whichDay 필드가 없습니다.");
                }

                // formMatch 필드 읽고 저장
                if (jsonMap.containsKey("formMatch")) {
                    dto.setFormMatch((Boolean) jsonMap.get("formMatch"));
                }

                return dto;
            } catch (Exception e) {
                log.error("JSON 파싱 중 오류 발생: {}", e.getMessage(), e);

                // 오류 발생 시 빈 객체 반환하지 말고 예외 발생시키기
                throw new RuntimeException("FastAPI 응답 파싱 중 오류가 발생했습니다: " + e.getMessage(), e);
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