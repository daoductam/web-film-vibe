package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchHistoryRequest {

    @NotBlank(message = "Movie slug is required")
    private String movieSlug;

    @NotBlank(message = "Title is required")
    private String title;

    private String thumbUrl;
    private String lastEpisodeSlug;
    private String lastEpisodeName;
    private Long progressMs;
    private Long durationMs;
}
