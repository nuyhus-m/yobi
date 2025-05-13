package com.S209.yobi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtConfig {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private Key getSigningKey() {
        byte[] keyBytes = "default_secret_key_for_initial_setup".getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Integer extractEmployeeNumber(String token) {
        return Integer.parseInt(extractClaim(token, claims -> claims.get("employeeNumber", String.class)));
    }

    public Integer extractUserId(String token) {
        return Integer.parseInt(extractClaim(token, claims -> claims.get("userId", String.class)));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(Integer employeeNumber, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeNumber", String.valueOf(employeeNumber));
        claims.put("userId", String.valueOf(userId));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Integer employeeNumber, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeNumber", String.valueOf(employeeNumber));
        claims.put("userId", String.valueOf(userId));

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7일
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // Redis에 refresh token 저장 (userId를 키로 사용)
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                7,
                TimeUnit.DAYS
        );

        return refreshToken;
    }

    public boolean validateToken(String token, Integer employeeNumber, Integer userId) {
        final Integer extractedEmployeeNumber = extractEmployeeNumber(token);
        final Integer extractedUserId = extractUserId(token);
        return (extractedEmployeeNumber.equals(employeeNumber) && 
                extractedUserId.equals(userId) && 
                !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String refreshToken, UserDetails userDetails, Integer employeeNumber, Integer userId) {
        final Integer tokenEmployeeNumber = extractEmployeeNumber(refreshToken);
        final Integer tokenUserId = extractUserId(refreshToken);
        return (tokenEmployeeNumber.equals(employeeNumber) && 
                tokenUserId.equals(userId) && 
                !isTokenExpired(refreshToken) && 
                validateRefreshTokenInRedis(userId, refreshToken));
    }

    // Redis 관련 메서드들
    private void saveRefreshToken(Integer userId, String refreshToken, long expirationTime) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }

    private String getRefreshToken(Integer userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    private void deleteRefreshToken(Integer userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    private Boolean validateRefreshTokenInRedis(Integer userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
} 