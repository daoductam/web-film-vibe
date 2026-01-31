package com.tamdao.web_film_backend.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified DTO for movie data from any source (ophim, nguonc, kkphim).
 * Each crawler client maps its specific response to this format.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawledMovie {
    private String title;
    private String originTitle;
    private String slug;
    private String thumbUrl;
    private String posterUrl;
    private Integer year;
    private String tmdbId;
    private String imdbId;
    private String description;
    private String status;         // "ongoing", "completed", "trailer"
    private String type;           // "single", "series", "hoathinh"
    private Integer totalEpisodes;
    private String currentEpisode;
    private String quality;
    private String language;
    private String duration;
    private String director;
    private String actors;
    private List<String> categories;
    private List<String> countries;
    private List<CrawledServer> servers;
}
