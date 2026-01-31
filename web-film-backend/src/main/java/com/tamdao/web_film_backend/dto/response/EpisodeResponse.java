package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeResponse {
    private Long id;
    private String name;
    private String slug;
    private String linkEmbed;
    private String linkM3u8;
    private String sourceProvider;
}
