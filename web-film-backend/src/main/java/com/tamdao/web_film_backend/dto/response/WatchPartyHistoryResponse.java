package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchPartyHistoryResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private String roomCode;
    private Long movieId;
    private String movieTitle;
    private String movieSlug;
    private String moviePosterUrl;
    private Integer watchDurationSeconds;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
