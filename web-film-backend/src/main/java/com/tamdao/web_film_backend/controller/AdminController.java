package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.crawler.service.CrawlerService;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin API endpoints (requires ADMIN role)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final CrawlerService crawlerService;
    private final com.tamdao.web_film_backend.service.MovieService movieService;

    @PostMapping("/maintenance/fix-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fix movie types", description = "Automatically re-classify mislabeled movies as SERIES based on episode count")
    public ResponseEntity<ApiResponse<Integer>> fixMovieTypes() {
        log.info("Admin triggered movie type maintenance");
        int count = movieService.cleanupMovieTypes();
        return ResponseEntity.ok(ApiResponse.success(count, "Maintenance completed. Total movies fixed: " + count));
    }

    @PostMapping("/crawl/category/{categorySlug}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crawl by category", description = "Trigger crawl for a specific category (e.g., 'hoat-hinh' for anime)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> crawlByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "1") int pages) {
        log.info("Admin triggered crawl for category: {} ({} pages)", categorySlug, pages);
        crawlerService.crawlByCategory(categorySlug, pages);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("category", categorySlug, "status", "Crawl started", "pages", pages),
                "Category crawl job started in background"
        ));
    }

    @PostMapping("/crawl")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger crawl", description = "Trigger crawl from all enabled sources")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerCrawl(
            @RequestParam(defaultValue = "1") int pages) {
        log.info("Admin triggered crawl for {} pages", pages);
        crawlerService.crawlLatestMovies(pages);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "Crawl started", "pages", pages),
                "Crawl job started in background"
        ));
    }

    @GetMapping("/crawler/sources")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get enabled sources", description = "Get list of enabled crawler sources")
    public ResponseEntity<ApiResponse<List<String>>> getEnabledSources() {
        return ResponseEntity.ok(ApiResponse.success(crawlerService.getEnabledSources()));
    }

    @PostMapping("/crawl/{slug}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crawl single movie", description = "Crawl a specific movie by slug from all sources")
    public ResponseEntity<ApiResponse<String>> crawlSingleMovie(@PathVariable String slug) {
        log.info("Admin triggered crawl for movie: {}", slug);
        var movie = crawlerService.crawlMovieBySlug(slug);
        if (movie != null) {
            return ResponseEntity.ok(ApiResponse.success(movie.getSlug(), "Movie crawled successfully"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Movie not found in any source"));
        }
    }
}
