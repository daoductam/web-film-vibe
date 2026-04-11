package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.response.*;
import com.tamdao.web_film_backend.entity.Episode;
import com.tamdao.web_film_backend.entity.Movie;
import com.tamdao.web_film_backend.entity.MovieType;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.mapper.CategoryMapper;
import com.tamdao.web_film_backend.mapper.CountryMapper;
import com.tamdao.web_film_backend.mapper.EpisodeMapper;
import com.tamdao.web_film_backend.mapper.MovieMapper;
import com.tamdao.web_film_backend.repository.EpisodeRepository;
import com.tamdao.web_film_backend.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final EpisodeRepository episodeRepository;
    private final MovieMapper movieMapper;
    private final EpisodeMapper episodeMapper;
    private final CategoryMapper categoryMapper;
    private final CountryMapper countryMapper;

    /**
     * Get paginated list of latest movies.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> getLatestMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAllByOrderByUpdatedAtDesc(pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Get paginated list of popular movies (by view count).
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> getPopularMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAllByOrderByViewCountDesc(pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Get movie detail by slug with episodes grouped by server.
     */
    @Transactional(readOnly = true)
    public MovieDetailResponse getMovieDetail(String slug) {
        Movie movie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "slug", slug));

        // Fetch episodes and group by server
        List<Episode> episodes = episodeRepository.findByMovieIdOrderByServerNameAscNameAsc(movie.getId());
        List<ServerEpisodeGroup> servers = groupEpisodesByServer(episodes);

        return MovieDetailResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originTitle(movie.getOriginTitle())
                .slug(movie.getSlug())
                .thumbUrl(movie.getThumbUrl())
                .posterUrl(movie.getPosterUrl())
                .year(movie.getYear())
                .description(movie.getDescription())
                .status(movie.getStatus() != null ? movie.getStatus().name() : null)
                .type(movie.getType() != null ? movie.getType().name() : null)
                .viewCount(movie.getViewCount())
                .totalEpisodes(movie.getTotalEpisodes())
                .currentEpisode(movie.getCurrentEpisode())
                .quality(movie.getQuality())
                .language(movie.getLanguage())
                .duration(movie.getDuration())
                .director(movie.getDirector())
                .actors(movie.getActors())
                .categories(new ArrayList<>(categoryMapper.toResponseSet(movie.getCategories())))
                .countries(new ArrayList<>(countryMapper.toResponseSet(movie.getCountries())))
                .servers(servers)
                .build();
    }

    /**
     * Search movies by keyword.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.searchByKeyword(keyword, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Get movies by category.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMoviesByCategory(String categorySlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findByCategorySlug(categorySlug, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Get movies by country.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMoviesByCountry(String countrySlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findByCountrySlug(countrySlug, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Filter movies based on various criteria.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> filterMovies(String typeStr, String categorySlug, String countrySlug, Integer year, String statusStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        com.tamdao.web_film_backend.entity.MovieType type = null;
        if (typeStr != null && !typeStr.isEmpty()) {
            try {
                type = com.tamdao.web_film_backend.entity.MovieType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        com.tamdao.web_film_backend.entity.MovieStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = com.tamdao.web_film_backend.entity.MovieStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return movieRepository.filterMovies(type, categorySlug, countrySlug, year, status, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Get movies by year.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMoviesByYear(int year, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findByYear(year, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Increment view count for a movie.
     */
    /**
     * Fix mislabeled movies in database.
     * Any movie with more than 1 total episode or current episode clearly being a series
     * should be marked as SERIES if it's currently marked as SINGLE.
     */
    @Transactional
    public int cleanupMovieTypes() {
        log.info("Starting movie type cleanup...");
        List<Movie> allMovies = movieRepository.findAll();
        int fixedCount = 0;
        
        for (Movie movie : allMovies) {
            MovieType currentType = movie.getType();
            MovieType targetType = currentType;
            
            // Rule 1: More than 1 episode is always a SERIES
            if (movie.getTotalEpisodes() != null && movie.getTotalEpisodes() > 1) {
                targetType = MovieType.SERIES;
            } 
            // Rule 2: 1 or 0 episode should be SINGLE for navigation (Movies page), 
            // even if it was HOAT_HINH or TV_SHOW
            else if (movie.getTotalEpisodes() != null && movie.getTotalEpisodes() <= 1) {
                targetType = MovieType.SINGLE;
            }
            
            // Rule 3: Check by current episode name for fuzzy detection of series
            String currentEp = movie.getCurrentEpisode();
            if (currentEp != null && (currentEp.toLowerCase().contains("tập") || currentEp.contains("/"))) {
                targetType = MovieType.SERIES;
            }

            if (targetType != currentType) {
                movie.setType(targetType);
                movieRepository.save(movie);
                fixedCount++;
            }
        }
        
        log.info("Movie type cleanup completed. Fixed {} movies.", fixedCount);
        return fixedCount;
    }

    @Transactional
    public void incrementViewCount(String slug) {
        movieRepository.findBySlug(slug).ifPresent(movie -> {
            movie.setViewCount(movie.getViewCount() + 1);
            movieRepository.save(movie);
        });
    }

    private List<ServerEpisodeGroup> groupEpisodesByServer(List<Episode> episodes) {
        Map<String, List<Episode>> grouped = episodes.stream()
                .collect(Collectors.groupingBy(Episode::getServerName, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> ServerEpisodeGroup.builder()
                        .serverName(entry.getKey())
                        .episodes(episodeMapper.toResponseList(entry.getValue()))
                        .build())
                .toList();
    }
}
