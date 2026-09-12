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
import com.tamdao.web_film_backend.repository.RatingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final EpisodeRepository episodeRepository;
    private final RatingRepository ratingRepository;
    private final MovieMapper movieMapper;
    private final EpisodeMapper episodeMapper;
    private final CategoryMapper categoryMapper;
    private final CountryMapper countryMapper;
    private final java.util.Optional<com.tamdao.web_film_backend.repository.neo4j.MovieNeo4jRepository> movieNeo4jRepository;

    /**
     * Get paginated list of latest movies.
     */
    @Cacheable(value = "latestMovies", key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<MovieResponse> getLatestMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAllByOrderByUpdatedAtDesc(pageable)
                .map(this::enrichWithRating);
    }

    /**
     * Get paginated list of popular movies (by view count).
     */
    @Cacheable(value = "popularMovies", key = "#page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<MovieResponse> getPopularMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAllByOrderByViewCountDesc(pageable)
                .map(this::enrichWithRating);
    }

    /**
     * Get movie detail by slug with episodes grouped by server.
     */
    @Cacheable(value = "movieDetail", key = "#slug")
    @Transactional(readOnly = true)
    public MovieDetailResponse getMovieDetail(String slug) {
        Movie movie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "slug", slug));

        // Fetch episodes and group by server
        List<Episode> episodes = episodeRepository.findByMovieIdOrderByServerNameAscNameAsc(movie.getId());
        List<ServerEpisodeGroup> servers = groupEpisodesByServer(episodes);

        MovieDetailResponse response = MovieDetailResponse.builder()
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

        Double avg = ratingRepository.getAverageScoreByMovieSlug(movie.getSlug());
        response.setAverageRating(avg != null ? (double) Math.round(avg * 10) / 10 : 0.0);
        response.setRatingCount(ratingRepository.countByMovieSlug(movie.getSlug()));

        return response;
    }

    private MovieResponse enrichWithRating(Movie movie) {
        MovieResponse response = movieMapper.toResponse(movie);
        Double avg = ratingRepository.getAverageScoreByMovieSlug(movie.getSlug());
        response.setAverageRating(avg != null ? (double) Math.round(avg * 10) / 10 : 0.0);
        response.setRatingCount(ratingRepository.countByMovieSlug(movie.getSlug()));
        return response;
    }

    /**
     * Search movies by keyword with Vietnamese diacritics normalization.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, int page, int size) {
        return searchMovies(keyword, null, page, size);
    }

    /**
     * Search movies by keyword with Personalized Re-ranking (Neo4j).
     * Ưu tiên đưa các phim thuộc thể loại yêu thích của User lên đầu kết quả tìm kiếm.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String trimmed = keyword != null ? keyword.trim() : "";
        String unaccented = com.tamdao.web_film_backend.util.VietnameseStringUtils.removeAccents(trimmed);
        String slug = com.tamdao.web_film_backend.util.VietnameseStringUtils.toSlug(trimmed);

        Page<Movie> searchResult = movieRepository.searchByKeyword(trimmed, unaccented, slug, pageable);
        List<MovieResponse> list = searchResult.getContent().stream()
                .map(this::enrichWithRating)
                .collect(Collectors.toList());

        // Nếu user đã đăng nhập, truy vấn Neo4j để lấy top thể loại yêu thích và re-rank
        if (username != null && !username.isBlank() && movieNeo4jRepository.isPresent()) {
            try {
                List<String> preferredCategorySlugs = movieNeo4jRepository.get().getTopCategorySlugsForUser(username);
                if (preferredCategorySlugs != null && !preferredCategorySlugs.isEmpty()) {
                    Set<String> preferredSet = new HashSet<>(preferredCategorySlugs);
                    
                    // Sắp xếp lại danh sách: phim nào có category thuộc preferredSet thì xếp ưu tiên lên trước
                    list.sort((m1, m2) -> {
                        boolean m1Match = m1.getCategories() != null && m1.getCategories().stream().anyMatch(c -> preferredSet.contains(c.getSlug()));
                        boolean m2Match = m2.getCategories() != null && m2.getCategories().stream().anyMatch(c -> preferredSet.contains(c.getSlug()));
                        if (m1Match && !m2Match) return -1;
                        if (!m1Match && m2Match) return 1;
                        return 0; // Giữ nguyên thứ tự trọng số ban đầu
                    });
                }
            } catch (Exception e) {
                log.warn("Neo4j personalized re-ranking skipped for user {}: {}", username, e.getMessage());
            }
        }

        return new org.springframework.data.domain.PageImpl<>(list, pageable, searchResult.getTotalElements());
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
     * Search movies by description keyword with filters.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMoviesByDescriptionKeyword(String keyword, String typeStr, java.util.List<String> categorySlugs, String countrySlug, Integer year, String statusStr, int page, int size) {
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

        int categoryCount = (categorySlugs == null || categorySlugs.isEmpty()) ? 0 : categorySlugs.size();
        java.util.List<String> safeCategorySlugs = categoryCount > 0 ? categorySlugs : java.util.Collections.singletonList("EMPTY_PLACEHOLDER");

        return movieRepository.searchByDescriptionKeyword(keyword, type, safeCategorySlugs, categoryCount, countrySlug, year, status, pageable)
                .map(movieMapper::toResponse);
    }

    /**
     * Filter movies based on various criteria.
     */
    @Transactional(readOnly = true)
    public Page<MovieResponse> filterMovies(String typeStr, java.util.List<String> categorySlugs, String countrySlug, Integer year, String statusStr, int page, int size) {
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

        int categoryCount = (categorySlugs == null || categorySlugs.isEmpty()) ? 0 : categorySlugs.size();
        java.util.List<String> safeCategorySlugs = categoryCount > 0 ? categorySlugs : java.util.Collections.singletonList("EMPTY_PLACEHOLDER");

        return movieRepository.filterMovies(type, safeCategorySlugs, categoryCount, countrySlug, year, status, pageable)
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
