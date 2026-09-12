package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.AIChatRequest;
import com.tamdao.web_film_backend.dto.response.AIChatResponse;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.service.GraphSyncService;
import com.tamdao.web_film_backend.service.ai.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI CineGuru", description = "AI Assistant endpoints for movie recommendations")
public class AIController {

    private final AIService aiService;
    private final GraphSyncService graphSyncService;

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
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "AI Semantic Natural Language Search", description = "Extract intent (category, type, year, keyword) via Groq LLM and search movies")
    public ResponseEntity<ApiResponse<com.tamdao.web_film_backend.dto.response.AISearchResultResponse>> searchWithAI(
            @org.springframework.web.bind.annotation.RequestParam String q,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "24") int size) {
        com.tamdao.web_film_backend.dto.response.AISearchResultResponse result = aiService.searchWithAI(q, page, size);
        return ResponseEntity.ok(ApiResponse.<com.tamdao.web_film_backend.dto.response.AISearchResultResponse>builder()
                .success(true)
                .message("AI semantic search completed successfully")
                .data(result)
                .build());
    }

    @PostMapping("/sync-graph")
    @Operation(summary = "Migrate MySQL data to Neo4j Graph DB", description = "Rebuild Neo4j nodes and relationships from MySQL data.")
    public ResponseEntity<ApiResponse<String>> syncGraph() {
        graphSyncService.syncAllData();
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Graph migration triggered asynchronously")
                .data("Migration in progress...")
                .build());
    }
}
