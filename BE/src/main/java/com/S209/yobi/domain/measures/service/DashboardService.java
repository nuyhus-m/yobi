package com.S209.yobi.domain.measures.service;

import com.S209.yobi.DTO.responseDTO.HealthDetailResponseDTO;
import com.S209.yobi.DTO.responseDTO.MainHealthResponseDTO;
import com.S209.yobi.DTO.responseDTO.TotalHealthResponseDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.clients.service.ClientValidationService;
import com.S209.yobi.domain.measures.Mapper.HealthMapper;
import com.S209.yobi.domain.measures.Mapper.HealthMapperNative;
import com.S209.yobi.domain.users.service.UserValidationService;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.repository.MeasureRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.sun.tools.javac.Main;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SimpleTimeZone;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DashboardService {
    private final ClientRepository clientRepository;

    private final UserValidationService userValidationService;
    private final ClientValidationService clientValidationService;
    private final MeasureQueryService measureQueryService;
    private final HealthLevelCacheService healthLevelCacheService;
    private final HealthMapperNative healthMapperNative;


    /**
     * 단건 데이터 조회 (주요 데이터)
     */
    public ApiResult getMainHealth (int userId, int clientId){

        // 유효한 유저인지 점검
        User user = userValidationService.validateUser(userId);

        // 유효한 클라이언트인지 점검
        Optional<Client> getClient = clientValidationService.validateClient(clientId);
        if(getClient.isEmpty()){
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);
        }
        Client client = getClient.get();

        // 유효한 측정값인지 점검
        Optional<Measure> todayMeasure =measureQueryService.getTodayMeasure(user, client);
        if(todayMeasure.isEmpty()){
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }
        Measure measure = todayMeasure.get();

        // 레디스에서 측정값 범위 조회
        LocalDate measureDate = convertToLocalDate(measure.getDate());
        Map<String, String> redisLevel = healthLevelCacheService.getHealthLevels(
                userId, clientId, measure.getDate());

        // 측정값 결과
        MainHealthResponseDTO result = MainHealthResponseDTO.of(measure,client.getId(),measureDate, redisLevel);
        return result;

    }

    /**
     * 단건 데이터 조회 (자세히보기)
     */
    public ApiResult getHealthDetail (int userId, int clientId) {

        // 유효한 유저인지 점검
        User user = userValidationService.validateUser(userId);

        // 유효한 클라이언트인지 점검
        Optional<Client> getClient = clientValidationService.validateClient(clientId);
        if(getClient.isEmpty()){
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);
        }
        Client client = getClient.get();

        // 유효한 측정값인지 점검
        Optional<Measure> todayMeasure =measureQueryService.getTodayMeasure(user, client);
        if(todayMeasure.isEmpty()){
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_RESOURCE);
        }
        Measure measure = todayMeasure.get();

        // 레디스에서 측정값 범위 조회
        LocalDate measureDate = convertToLocalDate(measure.getDate());
        Map<String, String> redisLevel = healthLevelCacheService.getHealthLevels(
                userId, clientId, measure.getDate());
        // Measure 객체를 가지고 HealthDetailResponseDTO 생성

        HealthDetailResponseDTO result = HealthDetailResponseDTO.of(measure, client.getId(), measureDate, redisLevel);
        return result;
    }

    /**
     * 건강 추이 전체 조회
     */
    public ApiResult getTotalHealth (int userId, int clientId, int size, Long cursorDate){
        // 유효한 유저인지 점검
        userValidationService.validateUser(userId);

        // 유효한 클라이언트인지 점검
        Optional<Client> getClient = clientValidationService.validateClient(clientId);
        if(getClient.isEmpty()){
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);
        }
        // cursorDate 가 null 이면 0L로 기본값 설정
        long effectiveCursorDate = Optional.ofNullable(cursorDate).orElse(0L);

        // cursorDate 부터 size 까지의 데이터 불러오기
        List<Object[]> measures = measureQueryService.getHealthTrends(clientId, effectiveCursorDate, size);

        // healthMapperNative를 통해 DTO변환
        TotalHealthResponseDTO result = healthMapperNative.totalHealthResponseDTO(clientId, measures);

        return result;

    }



    /**
     *  공통 메서드
     */


    // ===== Epoch 밀리초를 LocalDate로 변환 =====
    private LocalDate convertToLocalDate(long epochMilli){
        return Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    // ===== 오늘 날짜 Long 으로 반환 =====
    private long getTodayEpochMilli() {
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }



//
//    public ApiResult getTotalHealth (int userId, int clientId, int size, LocalDate cursorDate){
//        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
//        Client client = clientRepository.findById(clientId)
//                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));
//
//        // 페이징 설정
//        //최근 날짜 순으로 데이터를 "size" 개만 가져오도록 요청하는 페이징 설정
//        Pageable pageable = PageRequest.of(0, size, Sort.by("date").descending());
//
//        // 측정값 조회(cursorDate 유무에 따라 분기)
//        List<Measure> measures;
//        if(cursorDate == null){
//            measures = measureRepository.findByClient(client, pageable);
//        } else{
//            measures = measureRepository.findByClientBeforeDate(client, cursorDate, pageable);
//        }
//
//        // 연관 엔티티 접근 트리거 (BatchSize 로 최적화)
//        measures.forEach(m->{
//            BodyComposition body = m.getBody();
//            if(body != null) {
//                float bfp = body.getBfp();
//                float bfm = body.getBfm();
//                float icw = body.getIcw();
//                float ecw = body.getEcw();
//                float Protein = body.getProtein();
//            }
//
//            BloodPressure blood = m.getBlood();
//            if(blood != null) {
//                float sbp = blood.getSbp();
//                float dbp = blood.getDbp();
//            }
//
//            Stress stress = m.getStress();
//            if(stress != null) {
//                short stressValue = stress.getStressValue();
//            }
//        });
//
//        // DTO 변환
//        TotalHealthResponseDTO result = healthMapper.toTotalHealthDTO(user, client, measures);
//        return result;
//
//
//    }


}
