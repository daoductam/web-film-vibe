package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.UserWatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWatchHistoryRepository extends JpaRepository<UserWatchHistory, Long> {

    List<UserWatchHistory> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<UserWatchHistory> findByUserIdAndMovieSlug(Long userId, String movieSlug);
}
