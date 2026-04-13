package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.response.*;
import com.tamdao.web_film_backend.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie API endpoints")
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/latest")
    @Operation(summary = "Get latest movies", description = "Get paginated list of latest updated movies")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getLatestMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.getLatestMovies(page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular movies", description = "Get paginated list of most viewed movies")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getPopularMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.getPopularMovies(page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get movie detail", description = "Get movie detail with episodes by slug")
    public ResponseEntity<ApiResponse<MovieDetailResponse>> getMovieDetail(@PathVariable String slug) {
        MovieDetailResponse movie = movieService.getMovieDetail(slug);
        movieService.incrementViewCount(slug);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies", description = "Search movies by keyword")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> searchMovies(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.searchMovies(q, page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/category/{categorySlug}")
    @Operation(summary = "Get movies by category", description = "Get movies filtered by category")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getMoviesByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.getMoviesByCategory(categorySlug, page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/country/{countrySlug}")
    @Operation(summary = "Get movies by country", description = "Get movies filtered by country")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getMoviesByCountry(
            @PathVariable String countrySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.getMoviesByCountry(countrySlug, page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get movies by year", description = "Get movies filtered by release year")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> getMoviesByYear(
            @PathVariable int year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.getMoviesByYear(year, page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter movies with multiple criteria", description = "Filter by type, category, country, year, status")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> filterMovies(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) java.util.List<String> category,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        Page<MovieResponse> movies = movieService.filterMovies(type, category, country, year, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(movies, createPageInfo(movies)));
    }

    private PageInfo createPageInfo(Page<?> page) {
        return PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .itemsPerPage(page.getSize())
                .build();
    }
}
