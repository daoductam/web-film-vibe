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
public class WatchRoomResponse {
    private Long id;
    private String code;
    private String name;
    private UserSummary host;
    private MovieSummary movie;
    private EpisodeSummary episode;
    private String roomType;
    private Integer maxMembers;
    private Integer currentMemberCount;
    private String status;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieSummary {
        private Long id;
        private String title;
        private String slug;
        private String posterUrl;
        private String thumbUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodeSummary {
        private Long id;
        private String name;
        private String slug;
    }
}
