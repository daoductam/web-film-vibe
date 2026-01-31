package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailResponse {
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
    private List<CategoryResponse> categories;
    private List<CountryResponse> countries;
    private List<ServerEpisodeGroup> servers;
}
