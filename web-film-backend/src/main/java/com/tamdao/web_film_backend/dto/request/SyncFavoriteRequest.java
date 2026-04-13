package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SyncFavoriteRequest {

    @NotBlank(message = "Movie slug is required")
    private String movieSlug;

    @NotBlank(message = "Title is required")
    private String title;

    private String thumbUrl;
    private String quality;
    private Integer year;
}
