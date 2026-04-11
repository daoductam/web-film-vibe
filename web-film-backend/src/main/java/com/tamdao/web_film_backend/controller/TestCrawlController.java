package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.crawler.dto.CrawlProgress.CrawlProgressSnapshot;
import com.tamdao.web_film_backend.crawler.service.CrawlerService;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TEMPORARY controller for testing the crawler without authentication.
 * TODO: Remove this in production and use AdminController instead.
 */
@RestController
@RequestMapping("/v1/test-crawl")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Crawl", description = "Temporary testing endpoints (REMOVE IN PRODUCTION)")
public class TestCrawlController {

    private final CrawlerService crawlerService;

    @PostMapping("/category/{categorySlug}")
    @Operation(summary = "Trigger category test crawl", description = "Test crawl by category without authentication")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crawlByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "1") int pages) {
        
        if (crawlerService.isCrawling()) {
            return ResponseEntity.ok(ApiResponse.error("Crawl already in progress. Use /progress to check status."));
        }
        
        log.info("Test triggered category crawl for {} ({} pages)", categorySlug, pages);
        crawlerService.crawlByCategory(categorySlug, pages);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("category", categorySlug, "status", "Crawl started", "pages", pages),
                "Category crawl job started in background. Use /progress to monitor."
        ));
    }

    @PostMapping
    @Operation(summary = "Trigger test crawl", description = "Test crawl without authentication")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerCrawl(
            @RequestParam(defaultValue = "1") int pages) {
        
        if (crawlerService.isCrawling()) {
            return ResponseEntity.ok(ApiResponse.error("Crawl already in progress. Use /progress to check status."));
        }
        
        log.info("Test triggered crawl for {} pages", pages);
        crawlerService.crawlLatestMovies(pages);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "Crawl started", "pages", pages),
                "Crawl job started in background. Use /progress to monitor."
        ));
    }

    @GetMapping("/progress")
    @Operation(summary = "Get crawl progress", description = "Get current crawl job progress")
    public ResponseEntity<ApiResponse<CrawlProgressSnapshot>> getProgress() {
        return ResponseEntity.ok(ApiResponse.success(crawlerService.getProgress()));
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop crawl", description = "Stop the current crawl job")
    public ResponseEntity<ApiResponse<String>> stopCrawl() {
        if (!crawlerService.isCrawling()) {
            return ResponseEntity.ok(ApiResponse.error("No crawl job is running"));
        }
        crawlerService.stopCrawl();
        return ResponseEntity.ok(ApiResponse.success("Crawl stopped", "Crawl job has been stopped"));
    }

    @GetMapping("/sources")
    @Operation(summary = "Get enabled sources")
    public ResponseEntity<ApiResponse<List<String>>> getEnabledSources() {
        return ResponseEntity.ok(ApiResponse.success(crawlerService.getEnabledSources()));
    }

    @PostMapping("/{slug}")
    @Operation(summary = "Crawl single movie by slug")
    public ResponseEntity<ApiResponse<String>> crawlSingleMovie(@PathVariable String slug) {
        log.info("Test triggered crawl for movie: {}", slug);
        var movie = crawlerService.crawlMovieBySlug(slug);
        if (movie != null) {
            return ResponseEntity.ok(ApiResponse.success(movie.getSlug(), "Movie crawled successfully"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Movie not found in any source"));
        }
    }
}
