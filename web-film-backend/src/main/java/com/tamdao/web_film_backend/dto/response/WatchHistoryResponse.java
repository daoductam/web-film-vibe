package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryResponse {
    private String movieSlug;
    private String title;
    private String thumbUrl;
    private String lastEpisodeSlug;
    private String lastEpisodeName;
    private Long progressMs;
    private Long durationMs;
    private LocalDateTime updatedAt;
}
