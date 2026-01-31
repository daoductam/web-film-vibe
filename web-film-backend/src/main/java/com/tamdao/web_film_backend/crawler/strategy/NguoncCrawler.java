package com.tamdao.web_film_backend.crawler.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.tamdao.web_film_backend.config.CrawlerProperties;
import com.tamdao.web_film_backend.crawler.dto.CrawledEpisode;
import com.tamdao.web_film_backend.crawler.dto.CrawledMovie;
import com.tamdao.web_film_backend.crawler.dto.CrawledServer;
import com.tamdao.web_film_backend.entity.SourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Crawler implementation for phim.nguonc.com API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NguoncCrawler implements CrawlerStrategy {

    private final WebClient webClient;
    private final CrawlerProperties properties;

    @Override
    public SourceProvider getSourceProvider() {
        return SourceProvider.NGUONC;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public List<CrawledMovie> fetchLatestMovies(int page) {
        try {
            String url = properties.getNguoncBaseUrl() + "/films/phim-moi-cap-nhat?page=" + page;
            log.info("Fetching latest movies from Nguonc: {}", url);

            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("items")) {
                log.warn("No items found in Nguonc response");
                return Collections.emptyList();
            }

            List<CrawledMovie> movies = new ArrayList<>();
            for (JsonNode item : response.get("items")) {
                movies.add(mapToCrawledMovie(item));
            }

            log.info("Fetched {} movies from Nguonc page {}", movies.size(), page);
            return movies;
        } catch (Exception e) {
            log.error("Error fetching movies from Nguonc: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public CrawledMovie fetchMovieDetail(String slug) {
        try {
            String url = properties.getNguoncBaseUrl() + "/film/" + slug;
            log.info("Fetching movie detail from Nguonc: {}", url);

            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("movie")) {
                log.warn("Movie not found in Nguonc: {}", slug);
                return null;
            }

            JsonNode movie = response.get("movie");
            CrawledMovie crawledMovie = mapDetailToCrawledMovie(movie);

            // Map episodes
            JsonNode episodes = movie.path("episodes");
            if (episodes.isArray() && !episodes.isEmpty()) {
                List<CrawledServer> servers = new ArrayList<>();
                for (JsonNode serverNode : episodes) {
                    String serverName = serverNode.path("server_name").asText("Vietsub #1");
                    List<CrawledEpisode> episodeList = new ArrayList<>();

                    JsonNode items = serverNode.path("items");
                    if (items.isArray()) {
                        for (JsonNode ep : items) {
                            episodeList.add(CrawledEpisode.builder()
                                    .name(ep.path("name").asText())
                                    .slug(ep.path("slug").asText())
                                    .linkEmbed(ep.path("embed").asText())
                                    .linkM3u8(ep.path("m3u8").asText())
                                    .build());
                        }
                    }

                    servers.add(CrawledServer.builder()
                            .serverName(serverName)
                            .episodes(episodeList)
                            .build());
                }
                crawledMovie.setServers(servers);
            }

            log.info("Fetched movie detail from Nguonc: {}", crawledMovie.getTitle());
            return crawledMovie;
        } catch (Exception e) {
            log.error("Error fetching movie detail from Nguonc: {}", e.getMessage());
            return null;
        }
    }

    private CrawledMovie mapToCrawledMovie(JsonNode item) {
        return CrawledMovie.builder()
                .title(item.path("name").asText())
                .originTitle(item.path("original_name").asText())
                .slug(item.path("slug").asText())
                .thumbUrl(item.path("thumb_url").asText())
                .posterUrl(item.path("poster_url").asText())
                .year(parseYear(item.path("created").asText()))
                .totalEpisodes(item.path("total_episodes").asInt(0))
                .currentEpisode(item.path("current_episode").asText())
                .quality(item.path("quality").asText())
                .language(item.path("language").asText())
                .build();
    }

    private CrawledMovie mapDetailToCrawledMovie(JsonNode movie) {
        List<String> categories = new ArrayList<>();
        if (movie.has("category") && movie.get("category").isArray()) {
            for (JsonNode cat : movie.get("category")) {
                categories.add(cat.path("name").asText());
            }
        }

        List<String> countries = new ArrayList<>();
        if (movie.has("country") && movie.get("country").isArray()) {
            for (JsonNode country : movie.get("country")) {
                countries.add(country.path("name").asText());
            }
        }

        return CrawledMovie.builder()
                .title(movie.path("name").asText())
                .originTitle(movie.path("original_name").asText())
                .slug(movie.path("slug").asText())
                .thumbUrl(movie.path("thumb_url").asText())
                .posterUrl(movie.path("poster_url").asText())
                .year(movie.path("year").asInt(0))
                .description(movie.path("description").asText())
                .status(movie.path("status").asText())
                .type(movie.path("type").asText())
                .totalEpisodes(movie.path("total_episodes").asInt(0))
                .currentEpisode(movie.path("current_episode").asText())
                .quality(movie.path("quality").asText())
                .language(movie.path("language").asText())
                .duration(movie.path("time").asText())
                .director(movie.path("director").asText())
                .actors(movie.path("casts").asText())
                .categories(categories)
                .countries(countries)
                .build();
    }

    private Integer parseYear(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Format: 2026-01-31T06:22:00.000000Z
            return Integer.parseInt(dateStr.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }
}
