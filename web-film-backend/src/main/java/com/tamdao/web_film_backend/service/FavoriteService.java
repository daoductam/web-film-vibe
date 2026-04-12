package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.SyncFavoriteRequest;
import com.tamdao.web_film_backend.dto.response.FavoriteResponse;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.entity.UserFavorite;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.repository.UserFavoriteRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    /**
     * Get all favorites for the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(String username) {
        User user = findUser(username);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a movie to favorites (idempotent — skip if already exists).
     */
    @Transactional
    public FavoriteResponse addFavorite(String username, SyncFavoriteRequest request) {
        User user = findUser(username);

        // Idempotent: return existing if already favorited
        return favoriteRepository.findByUserIdAndMovieSlug(user.getId(), request.getMovieSlug())
                .map(this::toResponse)
                .orElseGet(() -> {
                    UserFavorite favorite = UserFavorite.builder()
                            .user(user)
                            .movieSlug(request.getMovieSlug())
                            .title(request.getTitle())
                            .thumbUrl(request.getThumbUrl())
                            .quality(request.getQuality())
                            .year(request.getYear())
                            .build();
                    UserFavorite saved = favoriteRepository.save(favorite);
                    log.info("Favorite added: user={} slug={}", username, request.getMovieSlug());
                    return toResponse(saved);
                });
    }

    /**
     * Remove a movie from favorites.
     */
    @Transactional
    public void removeFavorite(String username, String movieSlug) {
        User user = findUser(username);
        if (!favoriteRepository.existsByUserIdAndMovieSlug(user.getId(), movieSlug)) {
            throw new BadRequestException("Favorite not found for slug: " + movieSlug);
        }
        favoriteRepository.deleteByUserIdAndMovieSlug(user.getId(), movieSlug);
        log.info("Favorite removed: user={} slug={}", username, movieSlug);
    }

    /**
     * Batch sync: upserts all local favorites from Android at login time.
     */
    @Transactional
    public List<FavoriteResponse> syncFavorites(String username, List<SyncFavoriteRequest> requests) {
        for (SyncFavoriteRequest request : requests) {
            addFavorite(username, request);
        }
        return getFavorites(username);
    }

    // ── Private helpers ──────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));
    }

    private FavoriteResponse toResponse(UserFavorite fav) {
        return FavoriteResponse.builder()
                .movieSlug(fav.getMovieSlug())
                .title(fav.getTitle())
                .thumbUrl(fav.getThumbUrl())
                .quality(fav.getQuality())
                .year(fav.getYear())
                .createdAt(fav.getCreatedAt())
                .build();
    }
}
