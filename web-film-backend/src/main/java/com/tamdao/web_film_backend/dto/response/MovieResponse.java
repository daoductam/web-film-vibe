package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String originTitle;
    private String slug;
    private String thumbUrl;
    private String posterUrl;
    private Integer year;
    private String description;
    private String status;
    private String type;
    private Long viewCount;
    private Integer totalEpisodes;
    private String currentEpisode;
    private String quality;
    private String language;
    private String duration;
    private String director;
    private String actors;
    private Set<CategoryResponse> categories;
    private Set<CountryResponse> countries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
