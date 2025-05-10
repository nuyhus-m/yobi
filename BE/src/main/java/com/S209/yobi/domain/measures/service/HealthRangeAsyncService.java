package com.S209.yobi.domain.measures.service;

import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.BodyComposition;
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

}
