package com.S209.yobi.domain.measures.service;

import com.S209.yobi.DTO.responseDTO.BloodResponseDTO;
import com.S209.yobi.DTO.responseDTO.BodyCompositionResponseDTO; // 패키지 경로 확인
import com.S209.yobi.DTO.responseDTO.HeartRateResponseDTO;
import com.S209.yobi.domain.measures.entity.BloodPressure;
import com.S209.yobi.domain.measures.entity.BodyComposition;
import com.S209.yobi.domain.measures.entity.HeartRate;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.repository.BloodPressureRepository;
import com.S209.yobi.domain.measures.repository.BodyCompositionRepository;
import com.S209.yobi.domain.measures.repository.HeartRateRepository;
import com.S209.yobi.domain.measures.repository.MeasureRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
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
        Map<String, String> healthLevels = getHealthLevelsFromRedis(userId, clientId);

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

        // 해당 혈압이 특정 Measure에 속하는지 확인하여 클라이언트 ID 찾기
        Optional<Measure> measureOptional = measureRepository.findByBlood(bloodPressure);
        if (measureOptional.isEmpty()) {
            log.info("혈압에 연결된 측정 데이터를 찾을 수 없음 [bloodId: {}]", bloodId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }

        // 혈압 데이터를 DTO로 변환
        BloodResponseDTO responseDTO = BloodResponseDTO.of(bloodPressure);

        // 성공 응답 생성
        return ApiResponseDTO.success(responseDTO);
    }

    /**
     * 심박 데이터 ID로 조회
     */
    public ApiResult getHeartRateById(Integer userId, Long heartRateId) {
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

        // 심박 데이터를 DTO로 변환
        HeartRateResponseDTO responseDTO = HeartRateResponseDTO.of(heartRate);

        // 성공 응답 생성
        return ApiResponseDTO.success(responseDTO);
    }




    /**
     * Redis에서 건강 범위 정보 조회
     */
    private Map<String, String> getHealthLevelsFromRedis(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성 (HealthRangeAsyncService와 동일한 형식)
            String redisKey = "range" + userId + ":" + clientId + ":" + LocalDate.now();

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 건강 범위 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                return createDefaultLevels();
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 건강 범위 정보를 가져오는 중 오류 발생", e);
            return createDefaultLevels();
        }
    }

    /**
     * 기본 건강 범위 정보 생성 (Redis에서 데이터를 찾을 수 없는 경우 사용)
     */
    private Map<String, String> createDefaultLevels() {
        Map<String, String> defaultLevels = new HashMap<>();
        defaultLevels.put("bfp", "보통");
        defaultLevels.put("bfm", "보통");
        defaultLevels.put("smm", "보통");
        defaultLevels.put("bmr", "보통");
        defaultLevels.put("ecf", "보통");
        defaultLevels.put("protein", "보통");
        defaultLevels.put("mineral", "보통");
        return defaultLevels;
    }

    // 사용자 조회 유틸리티 메서드
    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}