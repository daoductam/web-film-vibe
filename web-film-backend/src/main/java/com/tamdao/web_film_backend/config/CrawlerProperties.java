package com.tamdao.web_film_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.crawler")
@Data
public class CrawlerProperties {
    private String ophimBaseUrl;
    private String nguoncBaseUrl;
    private String kkphimBaseUrl;
    private String scheduleCron;
    private boolean enabled = true;
}
