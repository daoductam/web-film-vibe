package com.tamdao.web_film_backend.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawledEpisode {
    private String name;
    private String slug;
    private String linkEmbed;
    private String linkM3u8;
}
