package com.S209.yobi.measures.service;

import com.S209.yobi.DTO.requestDTO.CheckBaseRequestDTO;
import com.S209.yobi.DTO.responseDTO.HealthDetailResponseDTO;
import com.S209.yobi.DTO.responseDTO.MainHealthResponseDTO;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.clients.repository.ClientRepository;
import com.S209.yobi.exception.ApiResponseCode;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.measures.entity.Measure;
import com.S209.yobi.measures.repository.MeasureRepository;
import com.S209.yobi.users.entity.User;
import com.S209.yobi.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final MeasureRepository measureRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    /**
     * 단건 데이터 조회 (주요 데이터)
     */
    public ApiResponseDTO<MainHealthResponseDTO> getMainHealth (int userId, CheckBaseRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        log.info("오늘 날짜:{}", today);
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // Measure 객체를 가지고 MainHealthResponseDTO 생성
        MainHealthResponseDTO dto = MainHealthResponseDTO.of(measure);
        return ApiResponseDTO.success(dto);
    }

    /**
     * 단건 데이터 조회 (자세히보기)
     */
    public ApiResponseDTO<HealthDetailResponseDTO> getHealthDetail (int userId, CheckBaseRequestDTO requestDTO) {
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        log.info("오늘 날짜:{}", today);
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // Measure 객체를 가지고 HealthDetailResponseDTO 생성
        HealthDetailResponseDTO dto = HealthDetailResponseDTO.of(measure);
        return ApiResponseDTO.success(dto);
    }


}
