package com.tamdao.web_film_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISearchResultResponse {
    private ParsedAIIntent parsedIntent;
    private Page<MovieResponse> movies;
    private String explanation;
}
