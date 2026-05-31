package com.tamdao.web_film_backend.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String AI_CHAT_PATH = "/api/v1/ai/chat";

    private static final int LOGIN_LIMIT = 5;
    private static final int AI_CHAT_LIMIT = 10;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        int limit = 0;
        String limitKeyType = "";

        if (LOGIN_PATH.equals(requestURI)) {
            limit = LOGIN_LIMIT;
            limitKeyType = "login";
        } else if (AI_CHAT_PATH.equals(requestURI)) {
            limit = AI_CHAT_LIMIT;
            limitKeyType = "ai-chat";
        }

        if (limit > 0) {
            String clientIp = getClientIp(request);
            String redisKey = "rate_limit:" + limitKeyType + ":" + clientIp;

            try {
                Long currentCount = redisTemplate.opsForValue().increment(redisKey);
                if (currentCount != null) {
                    if (currentCount == 1) {
                        redisTemplate.expire(redisKey, WINDOW_DURATION);
                    }

                    if (currentCount > limit) {
                        log.warn("Rate limit exceeded for IP: {} on URI: {}. Current count: {}", clientIp, requestURI, currentCount);
                        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please try again later.\"}");
                        return;
                    }
                }
            } catch (Exception e) {
                // If Redis is down, we fail-open (allow the request) so rate limiting doesn't break the application.
                log.error("Redis error in RateLimitingFilter: {}. Permitting request to proceed.", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            // In case of multiple proxies, X-Forwarded-For contains a list of IPs: "clientIP, proxy1, proxy2"
            int commaIndex = ip.indexOf(',');
            if (commaIndex != -1) {
                ip = ip.substring(0, commaIndex).trim();
            }
        }
        return ip;
    }
}
