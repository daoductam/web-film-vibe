package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.entity.neo4j.MovieNode;
import com.tamdao.web_film_backend.repository.neo4j.MovieNeo4jRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RecommendationGraphQLController {

    private final MovieNeo4jRepository movieNeo4jRepository;

    @QueryMapping
    public List<MovieNode> personalizedRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Anonymous access to personalized recommendations - returning empty list.");
            return Collections.emptyList();
        }
        String username = userDetails.getUsername();
        log.info("Fetching personalized recommendations for user: {}", username);
        return movieNeo4jRepository.getPersonalizedRecommendations(username);
    }

    @QueryMapping
    public List<MovieNode> similarMovies(@Argument String slug) {
        log.info("Fetching similar movies for slug: {}", slug);
        return movieNeo4jRepository.getSimilarMovies(slug);
    }
}
