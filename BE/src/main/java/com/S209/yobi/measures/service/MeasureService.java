package com.S209.yobi.measures.service;

import com.S209.yobi.DTO.requestDTO.BaseRequestDTO;
import com.S209.yobi.DTO.requestDTO.BodyCompositionDTO;
import com.S209.yobi.clients.entity.Client;
import com.S209.yobi.clients.repository.ClientRepository;
import com.S209.yobi.measures.entity.BodyComposition;
import com.S209.yobi.measures.entity.Measure;
import com.S209.yobi.measures.repository.BloodPressureRepository;
import com.S209.yobi.measures.repository.BodyCompositionRepository;
import com.S209.yobi.measures.repository.MeasureRepository;
import com.S209.yobi.users.entity.User;
import com.S209.yobi.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeasureService {

    private final MeasureRepository measureRepository;
    private final BloodPressureRepository bloodPressureRepository;
    private final BodyCompositionRepository bodyCompositionRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    /**
     * 피트러스 필수 데이터 저장 (체성분/혈압)
     */
    @Transactional
    public void saveBaseElement(int userId, BaseRequestDTO requestDTO){
        // 존재하는 유저인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        // 존재하는 돌봄대상인지 확인
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new EntityNotFoundException("돌봄 대상을 찾을 수 없습니다."));


        // measure 저장
        Measure measure = Measure.fromBase(user, client, requestDTO.getBodyCompositionDTO(), requestDTO.getBloodPressureDTO());

        bodyCompositionRepository.save(measure.getBody());
        bloodPressureRepository.save(measure.getBlood());
        measureRepository.save(measure);

    }



}
