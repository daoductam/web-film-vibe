package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = {"categories", "countries", "episodes"})
    @Override
    Optional<Movie> findById(Long id);

    @EntityGraph(attributePaths = {"categories", "countries", "episodes"})
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

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.categories c " +
            "WHERE " +
            "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.originTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.actors) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.director) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.slug) LIKE LOWER(CONCAT('%', :slugKeyword, '%')) OR " +
            "LOWER(m.slug) LIKE LOWER(CONCAT('%', :unaccentedKeyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.slug) LIKE LOWER(CONCAT('%', :slugKeyword, '%')) " +
            "ORDER BY CASE " +
            "   WHEN LOWER(m.title) = LOWER(:keyword) THEN 1 " +
            "   WHEN LOWER(m.title) LIKE LOWER(CONCAT(:keyword, '%')) THEN 2 " +
            "   WHEN LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3 " +
            "   WHEN LOWER(m.slug) LIKE LOWER(CONCAT(:slugKeyword, '%')) THEN 4 " +
            "   WHEN LOWER(m.slug) LIKE LOWER(CONCAT('%', :slugKeyword, '%')) THEN 5 " +
            "   WHEN LOWER(m.originTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 6 " +
            "   WHEN LOWER(m.actors) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 7 " +
            "   WHEN LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 8 " +
            "   ELSE 9 END, m.viewCount DESC")
    Page<Movie> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("unaccentedKeyword") String unaccentedKeyword,
            @Param("slugKeyword") String slugKeyword,
            Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.categories c " +
            "LEFT JOIN m.countries ct " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.originTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:type IS NULL OR m.type = :type) " +
            "AND (:categoryCount = 0 OR c.slug IN :categorySlugs) " +
            "AND (:countrySlug IS NULL OR ct.slug = :countrySlug) " +
            "AND (:year IS NULL OR m.year = :year) " +
            "AND (:status IS NULL OR m.status = :status)")
    Page<Movie> searchByDescriptionKeyword(
            @Param("keyword") String keyword,
            @Param("type") com.tamdao.web_film_backend.entity.MovieType type,
            @Param("categorySlugs") java.util.List<String> categorySlugs,
            @Param("categoryCount") int categoryCount,
            @Param("countrySlug") String countrySlug,
            @Param("year") Integer year,
            @Param("status") com.tamdao.web_film_backend.entity.MovieStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"categories"})
    @Query("SELECT m FROM Movie m")
    java.util.List<Movie> findAllWithCategories();

    boolean existsBySlug(String slug);
}
