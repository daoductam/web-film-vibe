package com.tamdao.web_film_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Default Spring async executor will be used
    // For production, consider configuring ThreadPoolTaskExecutor with specific pool sizes
}
