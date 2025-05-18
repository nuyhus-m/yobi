package com.S209.yobi.domain.measures.service;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.*;
import com.S209.yobi.domain.measures.helper.BodyCompResultVo;
import com.S209.yobi.domain.measures.helper.tbl_bodycomp;
import com.S209.yobi.domain.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRangeAsyncService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Async
    public void calculateAndSaveToRedis(User user, Client client, BodyComposition body){
        log.info("범위 계산하러 들어옴 [userId: {}, clientId: {}]",
                user.getId(), client.getId());
        String continent = "AS";

        tbl_bodycomp bodycomp = tbl_bodycomp.of(client, body);

        BodyCompResultVo vo = BodyCompResultVo.builder().build();;

        vo = BodyRangeCalculator.setGenValues(continent, bodycomp, vo);

        String redisKey = "range" + user.getId() + ":" +  client.getId() + ":" + LocalDate.now();

        // 기존 데이터 삭제
        redisTemplate.delete(redisKey);

        Map<String, String> result = new HashMap<>();
        result.put("bfp", levelFromScore(vo.getGenBfp()));
        result.put("bfm", levelFromScore(vo.getGenBfm()));
        result.put("bmr", levelFromScore(vo.getGenBmr()));
        result.put("smm", levelFromScore(vo.getGenSmm()));
        result.put("ecf", levelFromScore(vo.getGenEcf()));
        result.put("protein", levelFromScore(vo.getGenProtein()));
        result.put("mineral", levelFromScore(vo.getGenMinerals()));


        redisTemplate.opsForHash().putAll(redisKey, result);

    }

    private  String levelFromScore(int score){
        if(score < 90) return "낮음";
        if(score <= 130) return "보통";
        return "높음";
    }


    /**
     * 혈압 데이터에 대한 level을 계산하고 Redis에 저장
     */
    @Async
    public void calculateAndSaveBloodPressureLevels(User user, Client client, BloodPressure blood) {
        log.info("혈압 범위 계산하러 들어옴 [userId: {}, clientId: {}]",
                user.getId(), client.getId());

        // 소수점 첫째자리로 반올림
        float roundedSbp = Math.round(blood.getSbp() * 10) / 10.0f;
        float roundedDbp = Math.round(blood.getDbp() * 10) / 10.0f;

        // 혈압에 따라 level 계산
        String sbpLevel = calculateBpLevel(roundedSbp, true);
        String dbpLevel = calculateBpLevel(roundedDbp, false);

        // Redis 키 생성
        String redisKey = "bp:" + user.getId() + ":" + client.getId() + ":" + LocalDate.now();

        // 기존 데이터 삭제
        redisTemplate.delete(redisKey);

        // 결과 맵 생성
        Map<String, String> result = new HashMap<>();
        result.put("sbp", sbpLevel);
        result.put("dbp", dbpLevel);

        // Redis에 저장
        redisTemplate.opsForHash().putAll(redisKey, result);
    }

    /**
     * 혈압 값에 따라 level 계산
     */
    private String calculateBpLevel(float bp, boolean isSystolic) {
        if (isSystolic) {  // 수축기 혈압 (SBP)
            if (bp < 90) return "낮음";       // 저혈압
            if (bp < 130) return "보통";      // 정상 ~ 상승
            return "높음";                   // 고혈압
        } else {  // 이완기 혈압 (DBP)
            if (bp < 60) return "낮음";       // 저혈압
            if (bp < 85) return "보통";       // 정상 ~ 상승
            return "높음";                   // 고혈압
        }
    }


    /**
     * 스트레스 데이터에 대한 level을 계산하고 Redis에 저장
     */
    @Async
    public void calculateAndSaveStressLevels(User user, Client client, Stress stress) {
        log.info("스트레스 범위 계산하러 들어옴 [userId: {}, clientId: {}]",
                user.getId(), client.getId());

        // 스트레스 값
        float stressValue = stress.getStressValue();

        // 스트레스 값에 따라 level 계산
        String stressValueLevel = calculateStressValueLevel(stressValue);

        // Redis 키 생성
        String redisKey = "stress:" + user.getId() + ":" + client.getId() + ":" + LocalDate.now();

        // 기존 데이터 삭제
        redisTemplate.delete(redisKey);

        // 결과 맵 생성
        Map<String, String> result = new HashMap<>();
        result.put("stressValue", stressValueLevel);

        // Redis에 저장
        redisTemplate.opsForHash().putAll(redisKey, result);
    }

    /**
     * 스트레스 값에 따라 level 계산
     */
    private String calculateStressValueLevel(float stressValue) {
        if (stressValue < 30) return "낮음";     // 낮은 스트레스
        if (stressValue < 70) return "보통";     // 보통 스트레스
        return "높음";                          // 높은 스트레스
    }



    /**
     * 심박수 데이터에 대한 level을 계산하고 Redis에 저장
     * // 사용하지 아니함.
     */
    @Async
    public void calculateAndSaveHeartRateLevels(User user, Client client, HeartRate heartRate) {
        log.info("심박수 범위 계산하러 들어옴 [userId: {}, clientId: {}]",
                user.getId(), client.getId());

        // 심박수 값
        float bpm = heartRate.getBpm();
        float oxygen = heartRate.getOxygen();

        // 심박수 및 산소포화도에 따라 level 계산
        String bpmLevel = calculateBpmLevel(bpm);
        String oxygenLevel = calculateOxygenLevel(oxygen);

        // Redis 키 생성
        String redisKey = "hr:" + user.getId() + ":" + client.getId() + ":" + LocalDate.now();

        // 기존 데이터 삭제
        redisTemplate.delete(redisKey);

        // 결과 맵 생성
        Map<String, String> result = new HashMap<>();
        result.put("bpm", bpmLevel);
        result.put("oxygen", oxygenLevel);

        // Redis에 저장
        redisTemplate.opsForHash().putAll(redisKey, result);
    }

    /**
     * 심박수에 따라 level 계산
     */
    private String calculateBpmLevel(float bpm) {
        if (bpm < 60) return "낮음";      // 서맥
        if (bpm <= 100) return "보통";    // 정상
        return "높음";                   // 빈맥
    }

    /**
     * 산소포화도에 따라 level 계산
     */
    private String calculateOxygenLevel(float oxygen) {
        if (oxygen < 90) return "낮음";      // 위험 ~ 낮은 수준
        if (oxygen < 98) return "보통";      // 정상 범위 내 낮은 수준
        return "높음";                      // 정상 범위 내 높은 수준
    }

    /**
     * 체온 데이터에 대한 level을 계산하고 Redis에 저장
     */
    @Async
    public void calculateAndSaveTemperatureLevels(User user, Client client, Temperature temperature) {
        log.info("체온 범위 계산하러 들어옴 [userId: {}, clientId: {}, 온도: {}]",
                user.getId(), client.getId(), temperature.getTemperature());

        // 체온 값
        float temp = temperature.getTemperature();

        // 체온 값에 따라 level 계산
        String tempLevel = calculateTemperatureLevel(temp);
        log.info("계산된 체온 레벨: {}", tempLevel);

        // Redis 키 생성
        String redisKey = "temp:" + user.getId() + ":" + client.getId() + ":" + LocalDate.now();
        log.info("Redis 키: {}", redisKey);

        // 기존 데이터 삭제
        redisTemplate.delete(redisKey);

        // 결과 맵 생성
        Map<String, String> result = new HashMap<>();
        result.put("temperature", tempLevel);

        // Redis에 저장
        redisTemplate.opsForHash().putAll(redisKey, result);
        log.info("Redis에 저장 완료: key={}, value={}", redisKey, result);
    }

    /**
     * 체온 값에 따라 level 계산
     */
    private String calculateTemperatureLevel(float temp) {
        if (temp < 36.0) return "낮음";    // 저체온
        if (temp <= 37.5) return "보통";   // 정상 체온
        return "높음";                    // 고열
    }

}
