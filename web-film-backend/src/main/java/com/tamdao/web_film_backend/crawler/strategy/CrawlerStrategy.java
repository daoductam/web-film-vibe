package com.tamdao.web_film_backend.crawler.strategy;

import com.tamdao.web_film_backend.crawler.dto.CrawledMovie;
import com.tamdao.web_film_backend.entity.SourceProvider;

import java.util.List;

/**
 * Strategy interface for movie crawlers.
 * Each source (Ophim, Nguonc, Kkphim) implements this interface.
 * 
 * This follows the Strategy Pattern for extensibility:
 * - Add new source by implementing this interface
 * - No modification to existing code required (Open/Closed Principle)
 */
public interface CrawlerStrategy {

    /**
     * Get the source provider identifier.
     */
    SourceProvider getSourceProvider();

    /**
     * Fetch list of latest movies (paginated).
     * @param page Page number (1-based)
     * @return List of crawled movies with basic info (no episodes)
     */
    List<CrawledMovie> fetchLatestMovies(int page);

    /**
     * Fetch full movie detail including episodes.
     * @param slug Movie slug
     * @return Full movie data with episodes, or null if not found
     */
    CrawledMovie fetchMovieDetail(String slug);

    /**
     * Fetch movies by category slug.
     * @param categorySlug The slug of the category (e.g., 'hoat-hinh')
     * @param page Page number
     * @return List of movies in that category
     */
    List<CrawledMovie> fetchByCategory(String categorySlug, int page);

    /**
     * Check if this crawler is enabled.
     */
    boolean isEnabled();
}
