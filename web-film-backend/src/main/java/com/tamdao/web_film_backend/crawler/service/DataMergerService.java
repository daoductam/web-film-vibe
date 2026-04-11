package com.tamdao.web_film_backend.crawler.service;

import com.tamdao.web_film_backend.crawler.dto.CrawledEpisode;
import com.tamdao.web_film_backend.crawler.dto.CrawledMovie;
import com.tamdao.web_film_backend.crawler.dto.CrawledServer;
import com.tamdao.web_film_backend.entity.*;
import com.tamdao.web_film_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service responsible for merging crawled movie data into the database.
 * Implements the data merging strategy to avoid duplicates:
 * 1. Match by TMDB ID
 * 2. Match by IMDB ID  
 * 3. Match by slug
 * 4. Match by originTitle + year
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataMergerService {

    private final MovieRepository movieRepository;
    private final EpisodeRepository episodeRepository;
    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;

    /**
     * Merge a crawled movie into the database.
     * If movie exists (by TMDB/IMDB/slug/name+year) -> update & add episodes
     * If movie doesn't exist -> create new
     */
    @Transactional
    public Movie mergeMovie(CrawledMovie crawled, SourceProvider source) {
        log.debug("Merging movie: {} from source: {}", crawled.getSlug(), source);

        // Try to find existing movie by various criteria
        Optional<Movie> existingMovie = findExistingMovie(crawled);

        Movie movie;
        if (existingMovie.isPresent()) {
            movie = existingMovie.get();
            log.debug("Found existing movie: {}", movie.getSlug());
            updateMovieData(movie, crawled);
        } else {
            movie = createNewMovie(crawled);
            log.info("Created new movie: {}", movie.getSlug());
        }

        // Handle categories
        if (crawled.getCategories() != null) {
            Set<Category> categories = new HashSet<>();
            for (String catName : crawled.getCategories()) {
                Category category = getOrCreateCategory(catName);
                categories.add(category);
            }
            movie.setCategories(categories);
        }

        // Handle countries
        if (crawled.getCountries() != null) {
            Set<Country> countries = new HashSet<>();
            for (String countryName : crawled.getCountries()) {
                Country country = getOrCreateCountry(countryName);
                countries.add(country);
            }
            movie.setCountries(countries);
        }

        movie = movieRepository.save(movie);

        // Merge episodes
        if (crawled.getServers() != null) {
            mergeEpisodes(movie, crawled.getServers(), source);
        }

        return movie;
    }

    private Optional<Movie> findExistingMovie(CrawledMovie crawled) {
        // Priority 1: Match by TMDB ID
        if (crawled.getTmdbId() != null && !crawled.getTmdbId().isEmpty()) {
            Optional<Movie> byTmdb = movieRepository.findByTmdbId(crawled.getTmdbId());
            if (byTmdb.isPresent()) return byTmdb;
        }

        // Priority 2: Match by IMDB ID
        if (crawled.getImdbId() != null && !crawled.getImdbId().isEmpty()) {
            Optional<Movie> byImdb = movieRepository.findByImdbId(crawled.getImdbId());
            if (byImdb.isPresent()) return byImdb;
        }

        // Priority 3: Match by slug
        if (crawled.getSlug() != null) {
            Optional<Movie> bySlug = movieRepository.findBySlug(crawled.getSlug());
            if (bySlug.isPresent()) return bySlug;
        }

        // Priority 4: Match by originTitle + year (fuzzy match)
        if (crawled.getOriginTitle() != null && crawled.getYear() != null && crawled.getYear() > 0) {
            return movieRepository.findByOriginTitleAndYear(crawled.getOriginTitle(), crawled.getYear());
        }

        return Optional.empty();
    }

    private Movie createNewMovie(CrawledMovie crawled) {
        MovieType movieType = parseType(crawled.getType());
        
        // Refined logic: Prioritize SINGLE/SERIES based on episode count
        if (crawled.getTotalEpisodes() != null) {
            if (crawled.getTotalEpisodes() > 1) {
                movieType = MovieType.SERIES;
            } else if (crawled.getTotalEpisodes() <= 1 && (movieType == MovieType.HOAT_HINH || movieType == MovieType.TV_SHOW)) {
                // If it's animation or TV show but 0 or 1 episode, it's a "single movie" format
                movieType = MovieType.SINGLE;
            }
        }

        return Movie.builder()
                .title(crawled.getTitle())
                .originTitle(crawled.getOriginTitle())
                .slug(crawled.getSlug())
                .thumbUrl(crawled.getThumbUrl())
                .posterUrl(crawled.getPosterUrl())
                .year(crawled.getYear())
                .tmdbId(crawled.getTmdbId())
                .imdbId(crawled.getImdbId())
                .description(crawled.getDescription())
                .status(parseStatus(crawled.getStatus()))
                .type(movieType)
                .totalEpisodes(crawled.getTotalEpisodes())
                .currentEpisode(crawled.getCurrentEpisode())
                .quality(crawled.getQuality())
                .language(crawled.getLanguage())
                .duration(crawled.getDuration())
                .director(crawled.getDirector())
                .actors(crawled.getActors())
                .viewCount(0L)
                .build();
    }

    private void updateMovieData(Movie movie, CrawledMovie crawled) {
        // Update with newer/more complete data
        if (crawled.getTmdbId() != null && movie.getTmdbId() == null) {
            movie.setTmdbId(crawled.getTmdbId());
        }
        if (crawled.getImdbId() != null && movie.getImdbId() == null) {
            movie.setImdbId(crawled.getImdbId());
        }
        if (crawled.getDescription() != null && (movie.getDescription() == null || movie.getDescription().length() < crawled.getDescription().length())) {
            movie.setDescription(crawled.getDescription());
        }
        if (crawled.getThumbUrl() != null && movie.getThumbUrl() == null) {
            movie.setThumbUrl(crawled.getThumbUrl());
        }
        if (crawled.getPosterUrl() != null && movie.getPosterUrl() == null) {
            movie.setPosterUrl(crawled.getPosterUrl());
        }
        if (crawled.getCurrentEpisode() != null) {
            movie.setCurrentEpisode(crawled.getCurrentEpisode());
        }
        if (crawled.getTotalEpisodes() != null && crawled.getTotalEpisodes() > 0) {
            movie.setTotalEpisodes(crawled.getTotalEpisodes());
        }
        if (crawled.getStatus() != null) {
            movie.setStatus(parseStatus(crawled.getStatus()));
        }
        
        // Sync movie type (Fix: updating type was missing)
        if (crawled.getType() != null) {
            MovieType newType = parseType(crawled.getType());
            
            // Refined logic: Prioritize SINGLE/SERIES based on episode count
            if (movie.getTotalEpisodes() != null) {
                if (movie.getTotalEpisodes() > 1) {
                    newType = MovieType.SERIES;
                } else if (movie.getTotalEpisodes() <= 1 && (newType == MovieType.HOAT_HINH || newType == MovieType.TV_SHOW)) {
                    // Treat single-episode animations/shows as SINGLE for better navigation
                    newType = MovieType.SINGLE;
                }
            }
            movie.setType(newType);
        }
    }

    private void mergeEpisodes(Movie movie, java.util.List<CrawledServer> servers, SourceProvider source) {
        for (CrawledServer server : servers) {
            for (CrawledEpisode crawledEp : server.getEpisodes()) {
                // Check if this episode from this source already exists
                boolean exists = episodeRepository.existsByMovieIdAndServerNameAndNameAndSourceProvider(
                        movie.getId(), server.getServerName(), crawledEp.getName(), source);

                if (!exists) {
                    Episode episode = Episode.builder()
                            .movie(movie)
                            .serverName(server.getServerName())
                            .name(crawledEp.getName())
                            .slug(crawledEp.getSlug())
                            .linkEmbed(crawledEp.getLinkEmbed())
                            .linkM3u8(crawledEp.getLinkM3u8())
                            .sourceProvider(source)
                            .priority(getSourcePriority(source))
                            .build();
                    episodeRepository.save(episode);
                    log.debug("Added episode {} - {} from {}", server.getServerName(), crawledEp.getName(), source);
                }
            }
        }
    }

    private int getSourcePriority(SourceProvider source) {
        return switch (source) {
            case OPHIM -> 1;
            case NGUONC -> 2;
            case KKPHIM -> 3;
        };
    }

    private Category getOrCreateCategory(String name) {
        String slug = toSlug(name);
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name(name).slug(slug).build()
                ));
    }

    private Country getOrCreateCountry(String name) {
        String slug = toSlug(name);
        return countryRepository.findBySlug(slug)
                .orElseGet(() -> countryRepository.save(
                        Country.builder().name(name).slug(slug).build()
                ));
    }

    private String toSlug(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private MovieStatus parseStatus(String status) {
        if (status == null) return MovieStatus.ONGOING;
        return switch (status.toLowerCase()) {
            case "completed", "hoàn thành", "full" -> MovieStatus.COMPLETED;
            case "trailer" -> MovieStatus.TRAILER;
            default -> MovieStatus.ONGOING;
        };
    }

    private MovieType parseType(String type) {
        if (type == null || type.isEmpty()) return MovieType.SINGLE;
        
        String typeLower = type.toLowerCase().trim();
        return switch (typeLower) {
            case "series", "tv", "phim bộ", "tvshows", "tvshow", "television" -> MovieType.SERIES;
            case "hoathinh", "hoạt hình", "animation", "anime" -> MovieType.HOAT_HINH;
            case "tv_show", "show", "variety" -> MovieType.TV_SHOW;
            case "single", "movie", "phim lẻ" -> MovieType.SINGLE;
            default -> MovieType.SINGLE;
        };
    }
}
