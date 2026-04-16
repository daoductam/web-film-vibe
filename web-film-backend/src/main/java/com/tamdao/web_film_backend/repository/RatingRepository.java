package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByMovieSlugAndUserId(String movieSlug, Long userId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.movieSlug = :movieSlug")
    Double getAverageScoreByMovieSlug(@Param("movieSlug") String movieSlug);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.movieSlug = :movieSlug")
    Long countByMovieSlug(@Param("movieSlug") String movieSlug);
}
