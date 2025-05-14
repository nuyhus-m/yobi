package com.S209.yobi.config;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authHeader = request.getHeader("Authorization");
            logger.info("Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }



            // 토큰 추출 및 공백 제거
            final String jwt;
            if (authHeader.startsWith("Bearer Bearer ")) {
                // "Bearer Bearer " 형태로 중복된 경우
                jwt = authHeader.substring(14).replaceAll("\\s+", "");
            } else {
                // 일반적인 "Bearer " 형태인 경우
                jwt = authHeader.substring(7).replaceAll("\\s+", "");
            }
            logger.info("Processed Token (first 20 chars): " + (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt));

            // 토큰 형식 기본 검증
            if (!isValidJwtFormat(jwt)) {
                logger.warning("Invalid JWT format: " + jwt);
                filterChain.doFilter(request, response);
                return;
            }

            try {
                // 토큰 정보 추출
                final Integer employeeNumber = jwtProvider.extractEmployeeNumber(jwt);
                final Integer userId = jwtProvider.extractUserId(jwt);

                if (employeeNumber != null && userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(String.valueOf(employeeNumber));

                    if (jwtProvider.validateToken(jwt, employeeNumber, userId)) {
                        // 유효한 토큰인 경우 인증 정보 설정
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                jwt,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authentication successful for user: " + employeeNumber);
                    } else {
                        // Access token이 만료된 경우 - Refresh Token 처리
                        logger.info("Access token expired, checking refresh token");
                        String refreshToken = request.getHeader("Refresh-Token");
                        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
                            refreshToken = refreshToken.substring(7).replaceAll("\\s+", "");

                            if (!isValidJwtFormat(refreshToken)) {
                                logger.warning("Invalid refresh token format");
                                filterChain.doFilter(request, response);
                                return;
                            }

                            if (jwtProvider.validateRefreshToken(refreshToken, userDetails, employeeNumber, userId)) {
                                // 새로운 access token 발급
                                String newAccessToken = jwtProvider.generateToken(employeeNumber, userId);
                                response.setHeader("New-Access-Token", newAccessToken);
                                logger.info("New access token generated");

                                // 인증 정보 설정
                                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                            } else {
                                logger.warning("Refresh token validation failed");
                            }
                        } else {
                            logger.info("No refresh token available");
                        }
                    }
                }
            } catch (Exception e) {
                logger.severe("Error processing JWT token: " + e.getMessage());
                // 디버깅 정보 로깅
                StringBuilder sb = new StringBuilder("Token characters: ");
                for (int i = 0; i < Math.min(jwt.length(), 30); i++) {
                    char c = jwt.charAt(i);
                    sb.append("[").append(i).append("]:").append((int) c).append(" ");
                }
                logger.info(sb.toString());
            }
        } catch (Exception e) {
            logger.severe("Unexpected error in JWT filter: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    // JWT 토큰 형식 기본 검증 메서드
    private boolean isValidJwtFormat(String token) {
        // JWT는 header.payload.signature 형식으로 .으로 구분된 3개의 부분으로 구성됨
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldNotFilter = (path.equals("/api/users") && request.getMethod().equals("POST")) ||
                path.equals("/api/users/login") ||
                path.equals("/api/users/refresh") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs");

        if (shouldNotFilter) {
            logger.info("Skipping JWT filter for path: " + path);
        }

        return shouldNotFilter;
    }
}