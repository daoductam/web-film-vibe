package com.tamdao.web_film_backend.crawler.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks the progress of crawl jobs.
 * Thread-safe for concurrent updates from async crawlers.
 */
@Data
public class CrawlProgress {
    
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger totalPages = new AtomicInteger(0);
    private final AtomicInteger completedPages = new AtomicInteger(0);
    private final AtomicInteger totalMoviesFound = new AtomicInteger(0);
    private final AtomicInteger successfullyMerged = new AtomicInteger(0);
    private final AtomicInteger failed = new AtomicInteger(0);
    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    
    private String currentSource;
    private String currentMovie;
    private String lastError;

    public CrawlProgress() {
        this.startTime = LocalDateTime.now();
    }

    public void start(int pages) {
        running.set(true);
        totalPages.set(pages);
        completedPages.set(0);
        totalMoviesFound.set(0);
        successfullyMerged.set(0);
        failed.set(0);
        lastUpdateTime.set(System.currentTimeMillis());
    }

    public void complete() {
        running.set(false);
        endTime = LocalDateTime.now();
    }

    public void incrementCompletedPages() {
        completedPages.incrementAndGet();
        lastUpdateTime.set(System.currentTimeMillis());
    }

    public void incrementMoviesFound(int count) {
        totalMoviesFound.addAndGet(count);
    }

    public void incrementMerged() {
        successfullyMerged.incrementAndGet();
        lastUpdateTime.set(System.currentTimeMillis());
    }

    public void incrementFailed() {
        failed.incrementAndGet();
    }

    public void setCurrentSource(String source) {
        this.currentSource = source;
        lastUpdateTime.set(System.currentTimeMillis());
    }

    public void setCurrentMovie(String movie) {
        this.currentMovie = movie;
    }

    public void setLastError(String error) {
        this.lastError = error;
    }

    public double getProgressPercent() {
        int total = totalPages.get();
        if (total == 0) return 0;
        return (completedPages.get() * 100.0) / total;
    }

    public long getElapsedSeconds() {
        if (startTime == null) return 0;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).getSeconds();
    }

    public CrawlProgressSnapshot getSnapshot() {
        return CrawlProgressSnapshot.builder()
                .running(running.get())
                .startTime(startTime)
                .endTime(endTime)
                .totalPages(totalPages.get())
                .completedPages(completedPages.get())
                .progressPercent(Math.round(getProgressPercent() * 100) / 100.0)
                .totalMoviesFound(totalMoviesFound.get())
                .successfullyMerged(successfullyMerged.get())
                .failed(failed.get())
                .currentSource(currentSource)
                .currentMovie(currentMovie)
                .lastError(lastError)
                .elapsedSeconds(getElapsedSeconds())
                .build();
    }

    @Data
    @Builder
    public static class CrawlProgressSnapshot {
        private boolean running;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalPages;
        private int completedPages;
        private double progressPercent;
        private int totalMoviesFound;
        private int successfullyMerged;
        private int failed;
        private String currentSource;
        private String currentMovie;
        private String lastError;
        private long elapsedSeconds;
    }
}
