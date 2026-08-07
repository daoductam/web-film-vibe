package com.tamdao.web_film_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    private String movieSlug;

    @NotBlank(message = "Episode slug is required")
    private String episodeSlug;

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private Long parentId; // Nullable, used for replies
}
