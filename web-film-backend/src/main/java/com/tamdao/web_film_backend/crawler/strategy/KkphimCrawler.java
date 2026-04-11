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
 * Crawler implementation for kkphim (phimapi.com) API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KkphimCrawler implements CrawlerStrategy {

    private final WebClient webClient;
    private final CrawlerProperties properties;

    @Override
    public SourceProvider getSourceProvider() {
        return SourceProvider.KKPHIM;
    }

    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }

    @Override
    public List<CrawledMovie> fetchLatestMovies(int page) {
        try {
            String url = properties.getKkphimBaseUrl() + "/danh-sach/phim-moi-cap-nhat?page=" + page;
            log.info("Fetching latest movies from KKPhim: {}", url);

            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("items")) {
                log.warn("No items found in KKPhim response");
                return Collections.emptyList();
            }

            List<CrawledMovie> movies = new ArrayList<>();
            for (JsonNode item : response.get("items")) {
                movies.add(mapToCrawledMovie(item));
            }

            return movies;
        } catch (Exception e) {
            log.error("Error fetching movies from KKPhim: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CrawledMovie> fetchByCategory(String categorySlug, int page) {
        try {
            // KKPhim v1 API structure for category lists
            String url = properties.getKkphimBaseUrl() + "/v1/api/danh-sach/" + categorySlug + "?page=" + page;
            log.info("Fetching movies by category {} from KKPhim: {}", categorySlug, url);

            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.path("data").has("items")) {
                log.warn("No items found in KKPhim category response");
                return Collections.emptyList();
            }

            List<CrawledMovie> movies = new ArrayList<>();
            for (JsonNode item : response.path("data").get("items")) {
                movies.add(mapToCrawledMovie(item));
            }

            return movies;
        } catch (Exception e) {
            log.error("Error fetching category from KKPhim: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public CrawledMovie fetchMovieDetail(String slug) {
        try {
            String url = properties.getKkphimBaseUrl() + "/phim/" + slug;
            log.info("Fetching movie detail from KKPhim: {}", url);

            JsonNode response = webClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || !response.has("movie")) {
                log.warn("Movie not found in KKPhim: {}", slug);
                return null;
            }

            JsonNode movie = response.get("movie");
            JsonNode episodes = response.get("episodes");

            CrawledMovie crawledMovie = mapDetailToCrawledMovie(movie);

            // Map episodes
            if (episodes != null && episodes.isArray()) {
                List<CrawledServer> servers = new ArrayList<>();
                for (JsonNode serverNode : episodes) {
                    String serverName = serverNode.path("server_name").asText("Server #1");
                    List<CrawledEpisode> episodeList = new ArrayList<>();

                    JsonNode serverData = serverNode.path("server_data");
                    if (serverData.isArray()) {
                        for (JsonNode ep : serverData) {
                            episodeList.add(CrawledEpisode.builder()
                                    .name(ep.path("name").asText())
                                    .slug(ep.path("slug").asText())
                                    .linkEmbed(ep.path("link_embed").asText())
                                    .linkM3u8(ep.path("link_m3u8").asText())
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

            return crawledMovie;
        } catch (Exception e) {
            log.error("Error fetching movie detail from KKPhim: {}", e.getMessage());
            return null;
        }
    }

    private CrawledMovie mapToCrawledMovie(JsonNode item) {
        // KKPhim and Ophim share very similar item structures
        return CrawledMovie.builder()
                .title(item.path("name").asText())
                .originTitle(item.path("origin_name").asText())
                .slug(item.path("slug").asText())
                .thumbUrl(item.path("thumb_url").asText())
                .posterUrl(item.path("poster_url").asText())
                .year(item.path("year").asInt(0))
                .type(item.path("type").asText("series")) 
                .totalEpisodes(item.path("episode_total").asInt(0))
                .currentEpisode(item.path("episode_current").asText(""))
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
                .originTitle(movie.path("origin_name").asText())
                .slug(movie.path("slug").asText())
                .thumbUrl(movie.path("thumb_url").asText())
                .posterUrl(movie.path("poster_url").asText())
                .year(movie.path("year").asInt(0))
                .description(movie.path("content").asText())
                .status(movie.path("status").asText())
                .type(movie.path("type").asText())
                .totalEpisodes(movie.path("episode_total").asInt(0))
                .currentEpisode(movie.path("episode_current").asText())
                .quality(movie.path("quality").asText())
                .language(movie.path("lang").asText())
                .duration(movie.path("time").asText())
                .director(String.join(", ", extractArray(movie.get("director"))))
                .actors(String.join(", ", extractArray(movie.get("actor"))))
                .categories(categories)
                .countries(countries)
                .build();
    }

    private List<String> extractArray(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                result.add(item.asText());
            }
        }
        return result;
    }
}
