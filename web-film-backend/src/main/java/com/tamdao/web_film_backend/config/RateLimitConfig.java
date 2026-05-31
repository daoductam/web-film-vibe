package com.tamdao.web_film_backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean("rate-limit-login")
    public Cache<Object, Object> rateLimitLogin() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10000)
                .build();
    }

    @Bean("rate-limit-ai")
    public Cache<Object, Object> rateLimitAi() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10000)
                .build();
    }
}
