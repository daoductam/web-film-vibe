package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id; // UserNotification ID
    private String title;
    private String content;
    private String type;
    private String movieSlug;
    private String thumbUrl;
    @com.fasterxml.jackson.annotation.JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
}
