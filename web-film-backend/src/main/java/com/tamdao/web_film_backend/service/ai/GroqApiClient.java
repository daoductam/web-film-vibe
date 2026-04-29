package com.tamdao.web_film_backend.service.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class GroqApiClient {

    @Value("${app.groq.api-url}")
    private String apiUrl;

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.model}")
    private String model;

    private final WebClient webClient;

    public GroqApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public GroqResponse callChatCompletion(List<GroqMessage> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Groq API Key is missing! Please set the GROQ_API_KEY environment variable.");
            throw new RuntimeException("AI Service is currently unavailable: Missing API Configuration.");
        }
        try {
            GroqRequest requestPayload = GroqRequest.builder()
                    .model(model)
                    .messages(messages)
                    .responseFormat(new GroqResponseFormat("json_object"))
                    .build();

            log.info("Calling Groq API with model: {}", model);

            return webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(requestPayload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("Groq API Error Response: {}", errorBody);
                                return reactor.core.publisher.Mono.error(new RuntimeException("Groq API error: " + errorBody));
                            }))
                    .bodyToMono(GroqResponse.class)
                    .block();

        } catch (Exception e) {
            log.error("Error communicating with Groq API", e);
            throw new RuntimeException("Error communicating with Groq API: " + e.getMessage(), e);
        }
    }

    // --- Inner DTOs specifically for Groq API Contract ---

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public static class GroqRequest {
        private String model;
        private List<GroqMessage> messages;
        @com.fasterxml.jackson.annotation.JsonProperty("response_format")
        private GroqResponseFormat responseFormat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqMessage {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqResponseFormat {
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqResponse {
        private List<GroqChoice> choices;

        public String getFirstMessageContent() {
            if (choices != null && !choices.isEmpty() && choices.get(0).getMessage() != null) {
                return choices.get(0).getMessage().getContent();
            }
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroqChoice {
        private GroqMessage message;
    }
}
