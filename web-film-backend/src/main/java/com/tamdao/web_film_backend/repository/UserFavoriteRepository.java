package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserFavorite> findByUserIdAndMovieSlug(Long userId, String movieSlug);

    boolean existsByUserIdAndMovieSlug(Long userId, String movieSlug);

    void deleteByUserIdAndMovieSlug(Long userId, String movieSlug);
}
