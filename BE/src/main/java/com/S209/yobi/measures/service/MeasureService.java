package com.S209.yobi.measures.service;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.clients.repository.ClientRepository;
import com.S209.yobi.exception.ApiResponseCode;
import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.measures.entity.*;
import com.S209.yobi.measures.repository.BloodPressureRepository;
import com.S209.yobi.measures.repository.BodyCompositionRepository;
import com.S209.yobi.measures.repository.HeartRateRepository;
import com.S209.yobi.measures.repository.MeasureRepository;
import com.S209.yobi.users.entity.User;
import com.S209.yobi.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeasureService {

    private final MeasureRepository measureRepository;
    private final BloodPressureRepository bloodPressureRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final HeartRateRepository heartRateRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    /**
     * 피트러스 필수 데이터 저장 (체성분/혈압)
     */
    @Transactional
    public ApiResponseDTO<Void> saveBaseElement(int userId, BaseRequestDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 오늘 날짜 기준으로 측정 여부 확인
        LocalDate today = LocalDate.now();
        boolean alreadyMeasured = measureRepository.findByUserAndClientAndDate(user, client, today).isPresent();
        if (alreadyMeasured) {
            log.info("이미 측정된 기록 있음 [userId: {}, clientId: {}, date: {}]", userId, client.getId(), today);
            return ApiResponseDTO.fail(ApiResponseCode.DUPLICATE_MEASURE);
        }

        // measure 저장
        Measure measure = Measure.fromBase(user, client, requestDTO.getBodyCompositionDTO(), requestDTO.getBloodPressureDTO());

        bodyCompositionRepository.save(measure.getBody());
        bloodPressureRepository.save(measure.getBlood());
        measureRepository.save(measure);

        return ApiResponseDTO.success(null);

    }

    /**
     * 피트러스 심박 측정
     */
    @Transactional
    public ApiResponseDTO<Void> saveHeartRate(int userId, HeartRateDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // HeartRate 엔티티 생성 및 저장
        HeartRate heart = HeartRate.fromDTO(requestDTO);
        measure.setHeartRate(heart);

        return ApiResponseDTO.success(null);
    }

    /**
     * 피트러스 스트레스 측정
     */
    @Transactional
    public ApiResponseDTO<Void> saveStress(int userId, StressDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // Stress 엔티티 생성 및 저장
        Stress stress = Stress.fromDTO(requestDTO);
        measure.setStress(stress);

        return ApiResponseDTO.success(null);

    }


    /**
     * 피트러스 체온 측정
     */
    @Transactional
    public ApiResponseDTO<Void> saveTemperature(int userId, TemperatureDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // Temperature 엔티티 생성 및 저장
        Temperature temperature = Temperature.fromDTO(requestDTO);
        measure.setTemperature(temperature);

        return ApiResponseDTO.success(null);

    }

    /**
     * 피트러스 체성분 데이터 저장(재측정)
     */
    @Transactional
    public ApiResponseDTO<Void> saveBodyComposition(int userId, ReBodyCompositionDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 필수 측정 데이터 확인
        LocalDate today = LocalDate.now();
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, requestDTO.getClientId());
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);
        }
        Measure measure = optionalMeasure.get();

        // Temperature 엔티티 생성 및 저장
        BodyComposition bodyComposition = BodyComposition.fromReDTO(requestDTO);
        measure.setBody(bodyComposition);

        return ApiResponseDTO.success(null);

    }


}
