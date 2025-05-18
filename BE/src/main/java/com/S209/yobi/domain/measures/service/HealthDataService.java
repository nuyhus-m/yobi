package com.S209.yobi.domain.measures.service;

import com.S209.yobi.DTO.responseDTO.*;
import com.S209.yobi.domain.measures.entity.*;
import com.S209.yobi.domain.measures.repository.*;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthDataService {

    private final BodyCompositionRepository bodyCompositionRepository;
    private final MeasureRepository measureRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BloodPressureRepository bloodPressureRepository;
    private final HeartRateRepository heartRateRepository;
    private final StressRepository stressRepository;
    private final TemperatureRepository temperatureRepository;
    private final HealthLevelService healthLevelService;

    /**
     * 체성분 데이터 ID로 조회
     */
    public ApiResult getBodyComposition(Integer userId, Long bodyId) {
        // 사용자 확인
        User user = getUser(userId);

        // 체성분 데이터 조회
        Optional<BodyComposition> bodyCompositionOptional = bodyCompositionRepository.findById(bodyId);
        if (bodyCompositionOptional.isEmpty()) {
            log.info("체성분 데이터를 찾을 수 없음 [bodyId: {}]", bodyId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        BodyComposition bodyComposition = bodyCompositionOptional.get();

        // 해당 체성분이 특정 Measure에 속하는지 확인하여 클라이언트 ID 찾기
        Optional<Measure> measureOptional = measureRepository.findByBody(bodyComposition);
        if (measureOptional.isEmpty()) {
            log.info("체성분에 연결된 측정 데이터를 찾을 수 없음 [bodyId: {}]", bodyId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        Measure measure = measureOptional.get();
        Integer clientId = measure.getClient().getId();

        // Redis에서 건강 범위 정보 조회
        Map<String, String> healthLevels = healthLevelService.getBodyCompositionLevels(userId, clientId);

        // DTO 변환 및 ApiResponseDTO로 래핑하여 반환
        BodyCompositionResponseDTO responseDTO = BodyCompositionResponseDTO.of(bodyComposition, healthLevels);
        return ApiResponseDTO.success(responseDTO);
    }

    /**
     * 혈압 데이터 ID로 조회
     */
    public ApiResult getBloodPressure(Integer userId, Long bloodId) {
        // 사용자 확인
        User user = getUser(userId);

        // 혈압 데이터 조회
        Optional<BloodPressure> bloodPressureOptional = bloodPressureRepository.findById(bloodId);
        if (bloodPressureOptional.isEmpty()) {
            log.info("혈압 데이터를 찾을 수 없음 [bloodId: {}]", bloodId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        BloodPressure bloodPressure = bloodPressureOptional.get();

        // BloodResponseDTO.of 메소드 호출 - 내부에서 직접 계산함
        BloodResponseDTO responseDTO = BloodResponseDTO.of(bloodPressure);

        // 성공 응답 생성
        return ApiResponseDTO.success(responseDTO);
    }

    /**
     * 심박 데이터 ID로 조회
     */
    public ApiResult getHeartRate(Integer userId, Long heartRateId) {
        // 사용자 확인
        User user = getUser(userId);

        // 심박 데이터 조회
        Optional<HeartRate> heartRateOptional = heartRateRepository.findById(heartRateId);
        if (heartRateOptional.isEmpty()) {
            log.info("심박 데이터를 찾을 수 없음 [heartRateId: {}]", heartRateId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        HeartRate heartRate = heartRateOptional.get();

        // 해당 심박이 특정 Measure에 속하는지 확인
        Optional<Measure> measureOptional = measureRepository.findByHeart(heartRate);
        if (measureOptional.isEmpty()) {
            log.info("심박에 연결된 측정 데이터를 찾을 수 없음 [heartRateId: {}]", heartRateId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        HeartRateResponseDTO responseDTO = HeartRateResponseDTO.of(heartRate);

        return ApiResponseDTO.success(responseDTO);
    }

    /**
     * 스트레스 데이터 ID로 조회
     */
    public ApiResult getStress(Integer userId, Long stressId) {
        // 사용자 확인
        User user = getUser(userId);

        // 스트레스 데이터 조회
        Optional<Stress> stressOptional = stressRepository.findById(stressId);
        if (stressOptional.isEmpty()) {
            log.info("스트레스 데이터를 찾을 수 없음 [stressId: {}]", stressId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        Stress stress = stressOptional.get();

        // 해당 스트레스가 특정 Measure에 속하는지 확인
        Optional<Measure> measureOptional = measureRepository.findByStress(stress);
        if (measureOptional.isEmpty()) {
            log.info("스트레스에 연결된 측정 데이터를 찾을 수 없음 [stressId: {}]", stressId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        StressResponseDTO responseDTO = StressResponseDTO.of(stress);

        return ApiResponseDTO.success(responseDTO);
    }

    /**
     * 체온 데이터 ID로 조회
     */
    public ApiResult getTemperature(Integer userId, Long temperatureId) {
        // 사용자 확인
        User user = getUser(userId);

        // 체온 데이터 조회
        Optional<Temperature> temperatureOptional = temperatureRepository.findById(temperatureId);
        if (temperatureOptional.isEmpty()) {
            log.info("체온 데이터를 찾을 수 없음 [temperatureId: {}]", temperatureId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        Temperature temperature = temperatureOptional.get();

        // 해당 체온이 특정 Measure에 속하는지 확인
        Optional<Measure> measureOptional = measureRepository.findByTemperature(temperature);
        if (measureOptional.isEmpty()) {
            log.info("체온에 연결된 측정 데이터를 찾을 수 없음 [temperatureId: {}]", temperatureId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        TemperatureResponseDTO responseDTO = TemperatureResponseDTO.of(temperature);

        // 성공 응답 생성
        return ApiResponseDTO.success(responseDTO);
    }

    // 사용자 조회 유틸리티 메서드
    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}