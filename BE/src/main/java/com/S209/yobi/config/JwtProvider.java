package com.S209.yobi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
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
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private static final Logger logger = Logger.getLogger(JwtProvider.class.getName());
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.secret:default_secret_key_for_initial_setup}")
    private String secret;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Integer extractEmployeeNumber(String token) {
        try {
            return Integer.parseInt(extractClaim(token, claims -> claims.get("employeeNumber", String.class)));
        } catch (Exception e) {
            logger.severe("Failed to extract employeeNumber: " + e.getMessage());
            throw e;
        }
    }

    public Integer extractUserId(String token) {
        try {
            return Integer.parseInt(extractClaim(token, claims -> claims.get("userId", String.class)));
        } catch (Exception e) {
            logger.severe("Failed to extract userId: " + e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            // 공백 제거
            token = token.replaceAll("\\s+", "");

            // 기본 형식 검증
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new MalformedJwtException("Invalid JWT token format");
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            logger.severe("Invalid JWT signature: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.severe("Invalid JWT token: " + e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            logger.info("JWT token is expired: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.severe("JWT token is unsupported: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.severe("JWT claims string is empty: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("Error parsing JWT: " + e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public String generateToken(Integer employeeNumber, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeNumber", String.valueOf(employeeNumber));
        claims.put("userId", String.valueOf(userId));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
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
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // Redis에 refresh token 저장
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public boolean validateToken(String token, Integer employeeNumber, Integer userId) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            final Integer extractedEmployeeNumber = extractEmployeeNumber(token);
            final Integer extractedUserId = extractUserId(token);
            return (extractedEmployeeNumber.equals(employeeNumber) &&
                    extractedUserId.equals(userId) &&
                    !isTokenExpired(token));
        } catch (Exception e) {
            logger.warning("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public Boolean validateRefreshToken(String refreshToken, UserDetails userDetails, Integer employeeNumber, Integer userId) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return false;
            }

            final Integer tokenEmployeeNumber = extractEmployeeNumber(refreshToken);
            final Integer tokenUserId = extractUserId(refreshToken);
            return (tokenEmployeeNumber.equals(employeeNumber) &&
                    tokenUserId.equals(userId) &&
                    !isTokenExpired(refreshToken) &&
                    validateRefreshTokenInRedis(userId, refreshToken));
        } catch (Exception e) {
            logger.warning("Refresh token validation failed: " + e.getMessage());
            return false;
        }
    }

    private Boolean validateRefreshTokenInRedis(Integer userId, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(Integer userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}