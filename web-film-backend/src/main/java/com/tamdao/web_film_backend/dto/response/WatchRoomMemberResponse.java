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
public class WatchRoomMemberResponse {
    private Long id;
    private Long roomId;
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
