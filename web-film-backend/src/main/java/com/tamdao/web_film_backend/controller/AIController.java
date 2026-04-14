package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.AIChatRequest;
import com.tamdao.web_film_backend.dto.response.AIChatResponse;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.service.ai.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI CineGuru", description = "AI Assistant endpoints for movie recommendations")
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI CineGuru", description = "Submit a natural language query and get movie recommendations.")
    public ResponseEntity<ApiResponse<AIChatResponse>> chatWithAI(@Valid @RequestBody AIChatRequest request) {
        try {
            AIChatResponse response = aiService.processUserMessage(request);
            return ResponseEntity.ok(ApiResponse.<AIChatResponse>builder()
                    .success(true)
                    .message("AI request processed successfully")
                    .data(response)
                    .build());
        } catch (Exception e) {
            // Because our global exception handler usually handles throwing errors, 
            // throwing a RuntimeException here will be caught globally. 
            // We just ensure we throw it up to be caught by GlobalExceptionHandler.
            throw new RuntimeException(e.getMessage());
        }
    }
}
