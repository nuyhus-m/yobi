package com.S209.yobi.domain.measures.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 건강 측정 데이터의 레벨 정보를 관리하는 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthLevelService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis에서 체성분 level 정보 조회
     */
    public Map<String, String> getBodyCompositionLevels(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성
            String redisKey = "range" + userId + ":" + clientId + ":" + LocalDate.now();

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 체성분 level 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                return createDefaultBodyCompositionLevels();
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 체성분 level 정보를 가져오는 중 오류 발생", e);
            return createDefaultBodyCompositionLevels();
        }
    }

    /**
     * Redis에서 혈압 level 정보 조회
     */
    public Map<String, String> getBloodPressureLevels(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성
            String redisKey = "bp:" + userId + ":" + clientId + ":" + LocalDate.now();

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 혈압 level 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                return createDefaultBloodPressureLevels();
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 혈압 level 정보를 가져오는 중 오류 발생", e);
            return createDefaultBloodPressureLevels();
        }
    }

    /**
     * Redis에서 심박수 level 정보 조회
     */
    public Map<String, String> getHeartRateLevels(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성
            String redisKey = "hr:" + userId + ":" + clientId + ":" + LocalDate.now();

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 심박수 level 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                return createDefaultHeartRateLevels();
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 심박수 level 정보를 가져오는 중 오류 발생", e);
            return createDefaultHeartRateLevels();
        }
    }

    /**
     * Redis에서 스트레스 level 정보 조회
     */
    public Map<String, String> getStressLevels(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성
            String redisKey = "stress:" + userId + ":" + clientId + ":" + LocalDate.now();

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 스트레스 level 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                return createDefaultStressLevels();
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 스트레스 level 정보를 가져오는 중 오류 발생", e);
            return createDefaultStressLevels();
        }
    }

    /**
     * Redis에서 체온 level 정보 조회
     */
    public Map<String, String> getTemperatureLevels(Integer userId, Integer clientId) {
        try {
            // Redis 키 생성
            String redisKey = "temp:" + userId + ":" + clientId + ":" + LocalDate.now();
            log.info("체온 레벨 조회 Redis 키: {}", redisKey);

            // Redis에서 데이터 조회
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
            log.info("Redis에서 조회된 데이터: {}", entries);

            if (entries == null || entries.isEmpty()) {
                log.info("Redis에서 체온 level 정보를 찾을 수 없음 [userId: {}, clientId: {}]", userId, clientId);
                Map<String, String> defaultValues = createDefaultTemperatureLevels();
                log.info("기본값 반환: {}", defaultValues);
                return defaultValues;
            }

            // Object 타입 맵을 String 타입 맵으로 변환
            Map<String, String> levels = new HashMap<>();
            entries.forEach((k, v) -> levels.put(k.toString(), v.toString()));
            log.info("최종 반환 값: {}", levels);

            return levels;
        } catch (Exception e) {
            log.error("Redis에서 체온 level 정보를 가져오는 중 오류 발생", e);
            Map<String, String> defaultValues = createDefaultTemperatureLevels();
            log.info("예외 발생으로 기본값 반환: {}", defaultValues);
            return defaultValues;
        }
    }

    // 기본 level 정보 생성 메서드들

    /**
     * 기본 체성분 level 정보 생성
     */
    private Map<String, String> createDefaultBodyCompositionLevels() {
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

    /**
     * 기본 혈압 level 정보 생성
     */
    private Map<String, String> createDefaultBloodPressureLevels() {
        Map<String, String> defaultLevels = new HashMap<>();
        defaultLevels.put("sbp", "정상");
        defaultLevels.put("dbp", "정상");
        return defaultLevels;
    }

    /**
     * 기본 심박수 level 정보 생성
     */
    private Map<String, String> createDefaultHeartRateLevels() {
        Map<String, String> defaultLevels = new HashMap<>();
        defaultLevels.put("bpm", "정상");
        defaultLevels.put("oxygen", "정상");
        return defaultLevels;
    }

    /**
     * 기본 스트레스 level 정보 생성
     */
    private Map<String, String> createDefaultStressLevels() {
        Map<String, String> defaultLevels = new HashMap<>();
        defaultLevels.put("stressValue", "보통");
        return defaultLevels;
    }

    /**
     * 기본 체온 level 정보 생성
     */
    private Map<String, String> createDefaultTemperatureLevels() {
        Map<String, String> defaultLevels = new HashMap<>();
        defaultLevels.put("temperature", "정상");
        return defaultLevels;
    }

}