package com.tamdao.web_film_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedAIIntent {
    @JsonProperty("isMovieQuery")
    private boolean isMovieQuery;
    
    private List<String> categories;
    private String country;
    private Integer year;
}
