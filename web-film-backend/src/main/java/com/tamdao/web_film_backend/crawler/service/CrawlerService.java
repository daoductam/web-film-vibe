package com.tamdao.web_film_backend.crawler.service;

import com.tamdao.web_film_backend.crawler.dto.CrawledMovie;
import com.tamdao.web_film_backend.crawler.dto.CrawlProgress;
import com.tamdao.web_film_backend.crawler.dto.CrawlProgress.CrawlProgressSnapshot;
import com.tamdao.web_film_backend.crawler.strategy.CrawlerStrategy;
import com.tamdao.web_film_backend.entity.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main crawler service that orchestrates crawling from all sources.
 * Uses async processing for better performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final List<CrawlerStrategy> crawlers;
    private final DataMergerService dataMergerService;
    
    // Progress tracker - singleton for current crawl job
    private volatile CrawlProgress currentProgress;

    /**
     * Get current crawl progress.
     */
    public CrawlProgressSnapshot getProgress() {
        if (currentProgress == null) {
            return CrawlProgressSnapshot.builder()
                    .running(false)
                    .totalPages(0)
                    .completedPages(0)
                    .progressPercent(0)
                    .totalMoviesFound(0)
                    .successfullyMerged(0)
                    .failed(0)
                    .build();
        }
        return currentProgress.getSnapshot();
    }

    /**
     * Check if a crawl job is currently running.
     */
    public boolean isCrawling() {
        return currentProgress != null && currentProgress.getRunning().get();
    }

    /**
     * Crawl latest movies from all enabled sources.
     * @param pages Number of pages to crawl from each source
     */
    @Async
    public CompletableFuture<Integer> crawlLatestMovies(int pages) {
        // Prevent multiple concurrent crawls
        if (isCrawling()) {
            log.warn("Crawl already in progress, ignoring request");
            return CompletableFuture.completedFuture(-1);
        }

        // Initialize progress tracker
        currentProgress = new CrawlProgress();
        int totalPagesToProcess = pages * (int) crawlers.stream().filter(CrawlerStrategy::isEnabled).count();
        currentProgress.start(totalPagesToProcess);
        
        log.info("Starting crawl for {} pages from {} sources (total pages: {})", 
                pages, crawlers.size(), totalPagesToProcess);

        for (CrawlerStrategy crawler : crawlers) {
            if (!crawler.isEnabled()) {
                log.info("Skipping disabled crawler: {}", crawler.getSourceProvider());
                continue;
            }

            String sourceName = crawler.getSourceProvider().name();
            currentProgress.setCurrentSource(sourceName);
            log.info("Crawling from source: {}", sourceName);

            for (int page = 1; page <= pages; page++) {
                try {
                    List<CrawledMovie> movies = crawler.fetchLatestMovies(page);
                    currentProgress.incrementMoviesFound(movies.size());
                    
                    for (CrawledMovie crawledMovie : movies) {
                        try {
                            currentProgress.setCurrentMovie(crawledMovie.getTitle());
                            
                            // Fetch full detail for each movie
                            CrawledMovie detail = crawler.fetchMovieDetail(crawledMovie.getSlug());
                            if (detail != null) {
                                dataMergerService.mergeMovie(detail, crawler.getSourceProvider());
                                currentProgress.incrementMerged();
                            }
                        } catch (Exception e) {
                            currentProgress.incrementFailed();
                            currentProgress.setLastError(crawledMovie.getSlug() + ": " + e.getMessage());
                            log.error("Error processing movie {}: {}", crawledMovie.getSlug(), e.getMessage());
                        }
                    }
                    
                    currentProgress.incrementCompletedPages();
                    log.info("Processed page {}/{} from {} | Progress: {:.1f}% | Merged: {}", 
                            page, pages, sourceName, 
                            currentProgress.getProgressPercent(),
                            currentProgress.getSuccessfullyMerged().get());
                            
                } catch (Exception e) {
                    currentProgress.incrementCompletedPages();
                    currentProgress.setLastError("Page " + page + ": " + e.getMessage());
                    log.error("Error crawling page {} from {}: {}", page, sourceName, e.getMessage());
                }
            }
        }

        currentProgress.complete();
        int totalMerged = currentProgress.getSuccessfullyMerged().get();
        log.info("Crawl completed. Total merged: {} | Failed: {} | Time: {}s", 
                totalMerged, 
                currentProgress.getFailed().get(),
                currentProgress.getElapsedSeconds());
                
        return CompletableFuture.completedFuture(totalMerged);
    }

    @Async
    public CompletableFuture<Integer> crawlByCategory(String categorySlug, int pages) {
        if (isCrawling()) {
            log.warn("Crawl already in progress, ignoring request");
            return CompletableFuture.completedFuture(-1);
        }

        currentProgress = new CrawlProgress();
        int totalPagesToProcess = pages * (int) crawlers.stream().filter(CrawlerStrategy::isEnabled).count();
        currentProgress.start(totalPagesToProcess);
        
        log.info("Starting crawl for category '{}' | {} pages from {} sources", 
                categorySlug, pages, crawlers.size());

        for (CrawlerStrategy crawler : crawlers) {
            if (!crawler.isEnabled()) continue;

            String sourceName = crawler.getSourceProvider().name();
            currentProgress.setCurrentSource(sourceName);

            for (int page = 1; page <= pages; page++) {
                try {
                    List<CrawledMovie> movies = crawler.fetchByCategory(categorySlug, page);
                    currentProgress.incrementMoviesFound(movies.size());
                    
                    for (CrawledMovie crawledMovie : movies) {
                        try {
                            currentProgress.setCurrentMovie(crawledMovie.getTitle());
                            CrawledMovie detail = crawler.fetchMovieDetail(crawledMovie.getSlug());
                            if (detail != null) {
                                // Force type for hoat-hinh/anime if needed, but merger should handle it
                                dataMergerService.mergeMovie(detail, crawler.getSourceProvider());
                                currentProgress.incrementMerged();
                            }
                        } catch (Exception e) {
                            currentProgress.incrementFailed();
                            log.error("Error processing category movie {}: {}", crawledMovie.getSlug(), e.getMessage());
                        }
                    }
                    currentProgress.incrementCompletedPages();
                } catch (Exception e) {
                    currentProgress.incrementCompletedPages();
                    log.error("Error crawling category page {} from {}: {}", page, sourceName, e.getMessage());
                }
            }
        }

        currentProgress.complete();
        return CompletableFuture.completedFuture(currentProgress.getSuccessfullyMerged().get());
    }

    /**
     * Stop the current crawl job.
     */
    public void stopCrawl() {
        if (currentProgress != null && currentProgress.getRunning().get()) {
            currentProgress.complete();
            log.info("Crawl stopped by user");
        }
    }

    /**
     * Crawl a single movie by slug from all sources.
     */
    public Movie crawlMovieBySlug(String slug) {
        log.info("Crawling movie by slug: {}", slug);
        Movie result = null;

        for (CrawlerStrategy crawler : crawlers) {
            if (!crawler.isEnabled()) continue;

            try {
                CrawledMovie detail = crawler.fetchMovieDetail(slug);
                if (detail != null) {
                    result = dataMergerService.mergeMovie(detail, crawler.getSourceProvider());
                    log.info("Found and merged movie {} from {}", slug, crawler.getSourceProvider());
                }
            } catch (Exception e) {
                log.error("Error crawling {} from {}: {}", slug, crawler.getSourceProvider(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * Get list of enabled crawlers.
     */
    public List<String> getEnabledSources() {
        return crawlers.stream()
                .filter(CrawlerStrategy::isEnabled)
                .map(c -> c.getSourceProvider().name())
                .toList();
    }
}
