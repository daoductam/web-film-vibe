package com.tamdao.web_film_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.tamdao.web_film_backend.crawler.dto.CrawledMovie;
import com.tamdao.web_film_backend.crawler.strategy.CrawlerStrategy;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Debug controller for testing WebClient connectivity
 */
@RestController
@RequestMapping("/v1/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final WebClient webClient;
    private final List<CrawlerStrategy> crawlers;

    @GetMapping("/test-ophim")
    public ApiResponse<Map<String, Object>> testOphim() {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = "https://ophim1.com/danh-sach/phim-moi-cap-nhat?page=1";
            log.info("Testing WebClient to: {}", url);
            
            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            
            if (response != null) {
                result.put("status", response.has("status") ? response.get("status").asText() : "no status");
                result.put("hasItems", response.has("items"));
                result.put("itemCount", response.has("items") ? response.get("items").size() : 0);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("error", "Response is null");
            }
        } catch (Exception e) {
            log.error("Error testing ophim: ", e);
            result.put("success", false);
            result.put("error", e.getClass().getName() + ": " + e.getMessage());
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/test-fetch")
    public ApiResponse<Map<String, Object>> testFetch() {
        Map<String, Object> result = new HashMap<>();
        result.put("crawlersCount", crawlers.size());
        
        for (CrawlerStrategy crawler : crawlers) {
            Map<String, Object> crawlerResult = new HashMap<>();
            crawlerResult.put("enabled", crawler.isEnabled());
            try {
                List<CrawledMovie> movies = crawler.fetchLatestMovies(1);
                crawlerResult.put("moviesCount", movies.size());
                if (!movies.isEmpty()) {
                    crawlerResult.put("firstMovie", movies.get(0).getTitle());
                }
            } catch (Exception e) {
                crawlerResult.put("error", e.getClass().getName() + ": " + e.getMessage());
            }
            result.put(crawler.getSourceProvider().name(), crawlerResult);
        }
        
        return ApiResponse.success(result);
    }
}
