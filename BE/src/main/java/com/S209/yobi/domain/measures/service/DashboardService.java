package com.S209.yobi.domain.measures.service;

import com.S209.yobi.DTO.requestDTO.ClientRequestDTO;
import com.S209.yobi.DTO.responseDTO.HealthDetailResponseDTO;
import com.S209.yobi.DTO.responseDTO.MainHealthResponseDTO;
import com.S209.yobi.DTO.responseDTO.TotalHealthResponseDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.measures.Mapper.HealthMapper;
import com.S209.yobi.domain.measures.Mapper.HealthMapperNative;
import com.S209.yobi.domain.measures.entity.BloodPressure;
import com.S209.yobi.domain.measures.entity.BodyComposition;
import com.S209.yobi.domain.measures.entity.Stress;
import com.S209.yobi.exceptionFinal.ApiResult;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.repository.MeasureRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final MeasureRepository measureRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final HealthMapper healthMapper;
    private final HealthMapperNative healthMapperNative;

    /**
     * 단건 데이터 조회 (주요 데이터)
     */
    public ApiResult getMainHealth (int userId, int clientId){

        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 측정 데이터 Optional 로 조회
        LocalDate today = LocalDate.now();
//        LocalDate today = LocalDate.parse("2025-05-08");
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        Measure measure = optionalMeasure.orElse(null); // null 가능

        MainHealthResponseDTO result = MainHealthResponseDTO.of(measure, client.getId(), today);

        return result;
    }

    /**
     * 단건 데이터 조회 (자세히보기)
     */
    public ApiResult getHealthDetail (int userId, int clientId) {
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));

        // 당일 측정 데이터 Optional 로 조회
        LocalDate today = LocalDate.now();
//        LocalDate today = LocalDate.parse("2025-05-08");
        Optional<Measure> optionalMeasure = measureRepository.findByUserAndClientAndDate(user, client, today);
        Measure measure = optionalMeasure.orElse(null); // null 가능

        // Measure 객체를 가지고 HealthDetailResponseDTO 생성
        HealthDetailResponseDTO result = HealthDetailResponseDTO.of(measure, client.getId(), today);
        return result;
    }

    /**
     * 건강 추이 전체 조회
     */
    public ApiResult getTotalHealth (int userId, int clientId, int size, LocalDate cursorDate){
        // 존재하는 유저인지 & 존재하는 돌봄대상인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));


        List<Object[]> measures = measureRepository.findHealthTrendsNative(clientId, cursorDate, size);
        TotalHealthResponseDTO result = healthMapperNative.totalHealthResponseDTO(clientId, measures);
        return result;

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
