package com.S209.yobi.domain.measures.service;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.repository.MeasureRepository;
import com.S209.yobi.domain.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

/**
 *   측정 데이터 불러오기
 */
public class MeasureQueryService {
    private final MeasureRepository measureRepository;

    /**
     *   오늘자 측정 데이터 불러오기
     */
    public Optional<Measure> getTodayMeasure(User user, Client client){
        long todayEpochMilli = getTodayEpochMilli(); //오늘날짜 Long 타입으로 변환
        Optional<Measure> measure =  measureRepository.findByUserAndClientAndDate(user, client, todayEpochMilli);
        if(measure.isEmpty()){
            log.info("해당 유저의 오늘 측정 값 없음, clientId:{}", client.getId());
        }
        return measure;
    }

    /**
     *  건강 추이 데이터 조회 (페이징)
     */
    public List<Object[]> getHealthTrends(int clientId, long cursorDate, int size){
        return measureRepository.findHealthTrendsNative(clientId, cursorDate, size);
    }

    /**
     *  오늘날짜 Long 타입으로 변환
     */

    private long getTodayEpochMilli(){
        return LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

}
