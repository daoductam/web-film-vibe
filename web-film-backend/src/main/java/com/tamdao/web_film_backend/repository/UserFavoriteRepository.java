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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user"})
    @org.springframework.data.jpa.repository.Query("SELECT f FROM UserFavorite f")
    List<UserFavorite> findAllWithUser();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user"})
    List<UserFavorite> findByMovieSlug(String movieSlug);
}
