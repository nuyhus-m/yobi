package com.S209.yobi.domain.measures.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthLevelCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_RANGE_KEY_PREFIX = "range";

    public Map<String, String> getHealthLevels(int userId, int clientId, LocalDate date){
        String redisKey = buildRedisKey(userId, clientId, date);
        Map<Object,Object> redisData = redisTemplate.opsForHash().entries(redisKey);
        return redisData.entrySet().stream()
                .collect(Collectors.toMap(
                        e ->(String) e.getKey(),
                        e ->(String) e.getValue()
                ));
    }

    public String buildRedisKey(int userId, int clientId, LocalDate date){
        return String.format("%s%d:%d:%s", REDIS_RANGE_KEY_PREFIX, userId, clientId, date);
    }

}
