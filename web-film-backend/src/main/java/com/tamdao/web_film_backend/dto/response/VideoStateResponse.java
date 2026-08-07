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
public class VideoStateResponse {
    private Double currentTime;
    private Boolean isPlaying;
    private Double playbackRate;
    private LocalDateTime lastSyncAt;
}
