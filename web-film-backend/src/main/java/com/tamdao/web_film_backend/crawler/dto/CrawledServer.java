package com.tamdao.web_film_backend.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawledServer {
    private String serverName;
    private List<CrawledEpisode> episodes;
}
