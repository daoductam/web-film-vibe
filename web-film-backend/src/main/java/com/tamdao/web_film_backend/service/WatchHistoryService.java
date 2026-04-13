package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.WatchHistoryRequest;
import com.tamdao.web_film_backend.dto.response.WatchHistoryResponse;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.entity.UserWatchHistory;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.repository.UserRepository;
import com.tamdao.web_film_backend.repository.UserWatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchHistoryService {

    private final UserWatchHistoryRepository historyRepository;
    private final UserRepository userRepository;

    /**
     * Get watch history for the authenticated user (most recent first).
     */
    @Transactional(readOnly = true)
    public List<WatchHistoryResponse> getHistory(String username) {
        User user = findUser(username);
        return historyRepository.findByUserIdOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Upsert watch progress — creates or updates the entry for this movie.
     */
    @Transactional
    public WatchHistoryResponse saveHistory(String username, WatchHistoryRequest request) {
        User user = findUser(username);

        UserWatchHistory history = historyRepository
                .findByUserIdAndMovieSlug(user.getId(), request.getMovieSlug())
                .orElse(UserWatchHistory.builder()
                        .user(user)
                        .movieSlug(request.getMovieSlug())
                        .build());

        history.setTitle(request.getTitle());
        history.setThumbUrl(request.getThumbUrl());
        history.setLastEpisodeSlug(request.getLastEpisodeSlug());
        history.setLastEpisodeName(request.getLastEpisodeName());
        history.setProgressMs(request.getProgressMs());
        history.setDurationMs(request.getDurationMs());

        UserWatchHistory saved = historyRepository.save(history);
        log.info("Watch history saved: user={} movie={}", username, request.getMovieSlug());
        return toResponse(saved);
    }

    /**
     * Batch sync: upserts all local watch history from Android at login time.
     */
    @Transactional
    public List<WatchHistoryResponse> syncHistory(String username, List<WatchHistoryRequest> requests) {
        for (WatchHistoryRequest request : requests) {
            saveHistory(username, request);
        }
        return getHistory(username);
    }

    // ── Private helpers ──────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found: " + username));
    }

    private WatchHistoryResponse toResponse(UserWatchHistory h) {
        return WatchHistoryResponse.builder()
                .movieSlug(h.getMovieSlug())
                .title(h.getTitle())
                .thumbUrl(h.getThumbUrl())
                .lastEpisodeSlug(h.getLastEpisodeSlug())
                .lastEpisodeName(h.getLastEpisodeName())
                .progressMs(h.getProgressMs())
                .durationMs(h.getDurationMs())
                .updatedAt(h.getUpdatedAt())
                .build();
    }
}
