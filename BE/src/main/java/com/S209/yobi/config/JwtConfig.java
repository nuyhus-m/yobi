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

    public String extractEmployeeId(String token) {
        return extractClaim(token, Claims::getSubject);
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

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String refreshToken = createToken(claims, userDetails.getUsername(), refreshExpiration);
        // Redis에 Refresh Token 저장
        saveRefreshToken(userDetails.getUsername(), refreshToken, refreshExpiration);
        return refreshToken;
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String employeeId = extractEmployeeId(token);
        return (employeeId.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String refreshToken, UserDetails userDetails) {
        final String employeeId = extractEmployeeId(refreshToken);
        return (employeeId.equals(userDetails.getUsername()) && 
                !isTokenExpired(refreshToken) && 
                validateRefreshTokenInRedis(employeeId, refreshToken));
    }

    // Redis 관련 메서드들
    private void saveRefreshToken(String employeeNumber, String refreshToken, long expirationTime) {
        String key = REFRESH_TOKEN_PREFIX + employeeNumber;
        redisTemplate.opsForValue().set(key, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }

    private String getRefreshToken(String employeeNumber) {
        String key = REFRESH_TOKEN_PREFIX + employeeNumber;
        return redisTemplate.opsForValue().get(key);
    }

    private void deleteRefreshToken(String employeeNumber) {
        String key = REFRESH_TOKEN_PREFIX + employeeNumber;
        redisTemplate.delete(key);
    }

    private Boolean validateRefreshTokenInRedis(String employeeNumber, String refreshToken) {
        String storedToken = getRefreshToken(employeeNumber);
        return storedToken != null && storedToken.equals(refreshToken);
    }
} 