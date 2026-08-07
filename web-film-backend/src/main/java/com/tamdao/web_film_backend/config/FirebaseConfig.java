package com.tamdao.web_film_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${app.firebase.service-account-path}")
    private String serviceAccountPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase Application with path: {}", serviceAccountPath);
                Resource resource = resourceLoader.getResource(serviceAccountPath);
                
                if (!resource.exists()) {
                    log.warn("Firebase service account credentials file not found at {}. Firebase push notifications will be disabled.", serviceAccountPath);
                    return;
                }

                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

                    FirebaseApp.initializeApp(options);
                    log.info("Firebase Application successfully initialized.");
                }
            } else {
                log.info("Firebase App already initialized.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Application: {}. Push notifications might fail.", e.getMessage(), e);
        }
    }
}
