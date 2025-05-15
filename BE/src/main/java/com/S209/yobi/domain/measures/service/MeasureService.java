package com.S209.yobi.domain.measures.service;

import com.S209.yobi.DTO.requestDTO.*;
import com.S209.yobi.DTO.responseDTO.CheckBaseResultDTO;
import com.S209.yobi.DTO.responseDTO.MeasureResponseDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.measures.entity.*;
import com.S209.yobi.domain.measures.repository.*;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MeasureService {

    private final MeasureRepository measureRepository;
    private final BloodPressureRepository bloodPressureRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final HeartRateRepository heartRateRepository;
    private final StressRepository stressRepository;
    private final HealthRangeAsyncService healthRangeAsyncService;
    private final TemperatureRepository temperatureRepository;



    /**
     * 피트러스 필수 데이터 저장 (체성분/혈압)
     */
    public ApiResult saveBaseElement(int userId, int clientId, BaseRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        // 오늘 날짜 기준으로 측정 여부 확인
        long todayEpochMilli = getTodayEpochMilli();
        if (measureRepository.findByUserAndClientAndDate(user, client, todayEpochMilli).isPresent()) {
            log.info("이미 측정된 기록 있음 [userId: {}, clientId: {}, date: {}]", userId, clientId, LocalDate.now());
            return ApiResponseDTO.fail(ApiResponseCode.DUPLICATE_MEASURE);
        }

        // measure 저장
        Measure measure = Measure.fromBase(user, client, requestDTO.getBodyRequestDTO(), requestDTO.getBloodPressureDTO());
        measureRepository.save(measure);

        // 범위 계산 및 저장 로직 : 비동기로 호출
        log.info("범위 계산 전");
        healthRangeAsyncService.calculateAndSaveToRedis(user, client, measure.getBody());

        return MeasureResponseDTO.createWithBaseIds(
                measure.getBody().getId(),
                measure.getBlood().getId()
        );
    }

    /**
     * 피트러스 심박 측정
     */
    public ApiResult saveHeartRate(int userId, int clientId, HeartRateRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        // 당일 필수 측정 데이터 확인
        Measure measure = getTodayMeasureOrReturnFail(userId, clientId);
        if (measure == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);

        HeartRate oldHeart = measure.getHeart();
        measure.setHeartRate(null);  // 현재 연결 해제
        measureRepository.saveAndFlush(measure);  // 변경사항 즉시 저장

        // HeartRate 엔티티 생성
        HeartRate heart = HeartRate.fromDTO(requestDTO);

        // 별도의 저장소(Repository)를 사용하여 먼저 HeartRate 저장
        // HeartRateRepository heartRateRepository가 필요합니다
        heartRateRepository.save(heart);

        // 저장된 HeartRate 연결 및 measure 업데이트
        measure.setHeartRate(heart);
        measureRepository.save(measure);

        // 결과 반환
        return MeasureResponseDTO.createWithSingleId("heartRateId", heart.getId());
    }

    /**
     * 피트러스 스트레스 측정
     */
    public ApiResult saveStress(int userId, int clientId, StressRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        /// 당일 필수 측정 데이터 확인
        Measure measure = getTodayMeasureOrReturnFail(userId, clientId);
        if (measure == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);

        Stress oldStress = measure.getStress();
        measure.setStress(null);  // 현재 연결 해제
        measureRepository.saveAndFlush(measure);  // 변경사항 즉시 저장

        // Stress 엔티티 생성
        Stress stress = Stress.fromDTO(requestDTO);

        // 별도의 저장소(Repository)를 사용하여 먼저 Stress 저장 (추가 필요)
        stressRepository.save(stress);

        // 저장된 Stress 연결 및 measure 업데이트
        measure.setStress(stress);
        measureRepository.save(measure);

        // 결과 반환
        return MeasureResponseDTO.createWithSingleId("stressId", stress.getId());
    }


    /**
     * 피트러스 체온 측정
     */
    public ApiResult saveTemperature(int userId, int clientId, TemperatureRequestDTO requestDTO){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        /// 당일 필수 측정 데이터 확인
        Measure measure = getTodayMeasureOrReturnFail(userId, clientId);
        if (measure == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);

        Temperature oldTemperature = measure.getTemperature();
        measure.setTemperature(null);  // 현재 연결 해제
        measureRepository.saveAndFlush(measure);  // 변경사항 즉시 저장

        // Temperature 엔티티 생성
        Temperature temperature = Temperature.fromDTO(requestDTO);

        // 별도의 저장소(Repository)를 사용하여 먼저 Temperature 저장 (추가 필요)
        temperatureRepository.save(temperature);

        // 저장된 Temperature 연결 및 measure 업데이트
        measure.setTemperature(temperature);
        measureRepository.save(measure);

        // 결과 반환
        return MeasureResponseDTO.createWithSingleId("temperatureId", temperature.getId());
    }

    /**
     * 피트러스 체성분 데이터 저장(재측정)
     */
    public ApiResult saveBodyComposition(int userId, int clientId, ReBodyRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        /// 당일 필수 측정 데이터 확인
        Measure measure = getTodayMeasureOrReturnFail(userId, clientId);
        if (measure == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);

        // 새 BodyComposition 엔티티 생성
        BodyComposition newBodyComposition = BodyComposition.fromReDTO(requestDTO);

        // bodyCompositionRepository 저장
        bodyCompositionRepository.save(newBodyComposition);

        // 이전 BodyComposition ID 저장 (나중에 삭제용)
        Long oldBodyId = measure.getBody().getId();

        // 새 BodyComposition 연결
        measure.setBody(newBodyComposition);
        measureRepository.save(measure);

        // 이전 BodyComposition 삭제 (선택적)
        if (oldBodyId != null) {
            bodyCompositionRepository.deleteById(oldBodyId);
        }

        // 범위 계산 및 저장 로직 : 비동기로 호출
        log.info("범위 계산 전");
        healthRangeAsyncService.calculateAndSaveToRedis(user, client, newBodyComposition);

        // 결과 반환
        return MeasureResponseDTO.createWithSingleId("bodyId", newBodyComposition.getId());
    }

    /**
     * 피트러스 혈압 데이터 저장(재측정)
     */
    public ApiResult saveBloodPressure(int userId, int clientId, ReBloodRequestDTO requestDTO){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        /// 당일 필수 측정 데이터 확인
        Measure measure = getTodayMeasureOrReturnFail(userId, clientId);
        if (measure == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_MEASURE);

        // 새 BloodPressure 엔티티 생성
        BloodPressure newBloodPressure = BloodPressure.fromReDTO(requestDTO);

        // bloodPressureRepository 저장
        bloodPressureRepository.save(newBloodPressure);

        // 이전 BloodPressure ID 저장 (나중에 삭제용)
        Long oldBloodId = measure.getBlood().getId();

        // 새 BloodPressure 연결
        measure.setBlood(newBloodPressure);
        measureRepository.save(measure);

        // 이전 BloodPressure 삭제 (선택적)
        if (oldBloodId != null) {
            bloodPressureRepository.deleteById(oldBloodId);
        }

        // 결과 반환
        return MeasureResponseDTO.createWithSingleId("bloodId", newBloodPressure.getId());
    }

    /**
     * 오늘 필수 데이터 측정했는지 여부(T/F)
     */
    @Transactional(readOnly = true)
    public ApiResult checkBase(int userId, int clientId){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if (client == null) return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);

        // 당일 필수 측정 데이터 확인
        long epochMilli = getTodayEpochMilli();
        boolean exists = measureRepository.findByUserAndClientAndDate(user, client, epochMilli).isPresent();
        return CheckBaseResultDTO.of(exists);
    }


    /**
     *  공통 메서드
     */

    // ===== 유저 존재 확인 =====
    private User getUser(int userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
    }

    // ===== 클라이언트 존재 확인 =====
    //실패 시 null 반환 → 서비스 로직에서 FAIL 응답 처리
    // =============================
    private Client getClientOrReturnFail(int clientId){
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            log.info("해당하는 클라이언트 없음, [clientId:{}]", clientId);
            return null;
        }
        return optionalClient.get();
    }

    // ===== 오늘 필수 측정 데이터 존재 확인=====
    // 실패 시 null 반환 → 서비스 로직에서 FAIL 응답 처리
    // =============================
    private Measure getTodayMeasureOrReturnFail(int userId, int clientId){
        User user = getUser(userId);
        Client client = getClientOrReturnFail(clientId);
        if(client == null) return null;

        long epochMilli = getTodayEpochMilli();
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, epochMilli);
        if (optionalMeasure.isEmpty()) {
            log.info("당일 필수 측정 데이터 없음, [userId:{}, clientId:{}]", userId, clientId);
            return null;
        }
        return optionalMeasure.get();
    }

    // ===== 오늘 날짜 Long 으로 반환 =====
    private long getTodayEpochMilli() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }




}
