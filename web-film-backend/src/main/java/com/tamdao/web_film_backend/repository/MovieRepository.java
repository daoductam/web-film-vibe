package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findBySlug(String slug);

    Optional<Movie> findByTmdbId(String tmdbId);

    Optional<Movie> findByImdbId(String imdbId);

    @Query("SELECT m FROM Movie m WHERE m.originTitle = :originTitle AND m.year = :year")
    Optional<Movie> findByOriginTitleAndYear(@Param("originTitle") String originTitle, @Param("year") Integer year);

    Page<Movie> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<Movie> findAllByOrderByViewCountDesc(Pageable pageable);

    @Query("SELECT m FROM Movie m JOIN m.categories c WHERE c.slug = :categorySlug")
    Page<Movie> findByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT m FROM Movie m JOIN m.countries c WHERE c.slug = :countrySlug")
    Page<Movie> findByCountrySlug(@Param("countrySlug") String countrySlug, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.type = :type")
    Page<Movie> findByType(@Param("type") com.tamdao.web_film_backend.entity.MovieType type, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.categories c " +
            "LEFT JOIN m.countries ct " +
            "WHERE (:type IS NULL OR m.type = :type) " +
            "AND (:categoryCount = 0 OR c.slug IN :categorySlugs) " +
            "AND (:countrySlug IS NULL OR ct.slug = :countrySlug) " +
            "AND (:year IS NULL OR m.year = :year) " +
            "AND (:status IS NULL OR m.status = :status)")
    Page<Movie> filterMovies(
            @Param("type") com.tamdao.web_film_backend.entity.MovieType type,
            @Param("categorySlugs") java.util.List<String> categorySlugs,
            @Param("categoryCount") int categoryCount,
            @Param("countrySlug") String countrySlug,
            @Param("year") Integer year,
            @Param("status") com.tamdao.web_film_backend.entity.MovieStatus status,
            Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.year = :year")
    Page<Movie> findByYear(@Param("year") Integer year, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.originTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsBySlug(String slug);
}
