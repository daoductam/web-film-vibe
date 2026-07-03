package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.entity.Category;
import com.tamdao.web_film_backend.entity.Movie;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.entity.UserFavorite;
import com.tamdao.web_film_backend.entity.UserWatchHistory;
import com.tamdao.web_film_backend.entity.neo4j.CategoryNode;
import com.tamdao.web_film_backend.entity.neo4j.MovieNode;
import com.tamdao.web_film_backend.entity.neo4j.UserNode;
import com.tamdao.web_film_backend.repository.MovieRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import com.tamdao.web_film_backend.repository.UserFavoriteRepository;
import com.tamdao.web_film_backend.repository.UserWatchHistoryRepository;
import com.tamdao.web_film_backend.repository.neo4j.MovieNeo4jRepository;
import com.tamdao.web_film_backend.repository.neo4j.UserNeo4jRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphSyncService {

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final UserWatchHistoryRepository watchHistoryRepository;
    
    private final MovieNeo4jRepository movieNeo4jRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final Neo4jClient neo4jClient;

    /**
     * Synchronize a single movie from MySQL to Neo4j.
     */
    @Transactional(readOnly = true)
    public void syncMovieNode(String slug) {
        try {
            Optional<Movie> movieOpt = movieRepository.findBySlug(slug);
            if (movieOpt.isEmpty()) {
                log.warn("Cannot sync movie to Neo4j, not found in MySQL: {}", slug);
                return;
            }
            Movie movie = movieOpt.get();

            Set<CategoryNode> categoryNodes = movie.getCategories().stream()
                    .map(cat -> CategoryNode.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .slug(cat.getSlug())
                            .build())
                    .collect(Collectors.toSet());

            MovieNode movieNode = MovieNode.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .slug(movie.getSlug())
                    .posterUrl(movie.getPosterUrl())
                    .views(movie.getViewCount() != null ? movie.getViewCount().intValue() : 0)
                    .rating(4.5) // Default rating fallback
                    .categories(categoryNodes)
                    .build();

            movieNeo4jRepository.save(movieNode);
            log.info("Synced Movie node to Neo4j: {}", slug);
        } catch (Exception e) {
            log.error("Failed to sync movie node to Neo4j for slug: " + slug, e);
        }
    }

    /**
     * Synchronize a user from MySQL to Neo4j.
     */
    @Transactional(readOnly = true)
    public void syncUserNode(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("Cannot sync user to Neo4j, not found in MySQL: {}", username);
                return;
            }
            UserNode userNode = UserNode.builder()
                    .username(username)
                    .build();
            userNeo4jRepository.save(userNode);
            log.info("Synced User node to Neo4j: {}", username);
        } catch (Exception e) {
            log.error("Failed to sync user node to Neo4j for user: " + username, e);
        }
    }

    /**
     * Synchronize a Favorite relationship.
     */
    public void syncFavoriteEdge(String username, String movieSlug, boolean favorited) {
        try {
            // Guarantee nodes exist
            syncUserNode(username);
            syncMovieNode(movieSlug);

            if (favorited) {
                neo4jClient.query(
                        "MATCH (u:User {username: $username}), (m:Movie {slug: $movieSlug}) " +
                        "MERGE (u)-[:FAVORITED]->(m)"
                )
                .bind(username).to("username")
                .bind(movieSlug).to("movieSlug")
                .run();
                log.info("Linked FAVORITED edge in Neo4j: {} -> {}", username, movieSlug);
            } else {
                neo4jClient.query(
                        "MATCH (u:User {username: $username})-[r:FAVORITED]->(m:Movie {slug: $movieSlug}) " +
                        "DELETE r"
                )
                .bind(username).to("username")
                .bind(movieSlug).to("movieSlug")
                .run();
                log.info("Unlinked FAVORITED edge in Neo4j: {} -> {}", username, movieSlug);
            }
        } catch (Exception e) {
            log.error("Failed to sync favorite edge to Neo4j", e);
        }
    }

    /**
     * Synchronize a Watch History relationship.
     */
    public void syncWatchHistoryEdge(String username, String movieSlug) {
        try {
            // Guarantee nodes exist
            syncUserNode(username);
            syncMovieNode(movieSlug);

            neo4jClient.query(
                    "MATCH (u:User {username: $username}), (m:Movie {slug: $movieSlug}) " +
                    "MERGE (u)-[r:WATCHED]->(m) " +
                    "ON CREATE SET r.views = 1 " +
                    "ON MATCH SET r.views = r.views + 1"
            )
            .bind(username).to("username")
            .bind(movieSlug).to("movieSlug")
            .run();
            log.info("Linked WATCHED edge in Neo4j: {} -> {}", username, movieSlug);
        } catch (Exception e) {
            log.error("Failed to sync watch history edge to Neo4j", e);
        }
    }

    /**
     * Complete Database Sync / Migration to Neo4j.
     */
    @Async
    @Transactional(readOnly = true)
    public void syncAllData() {
        log.info("Starting complete graph data migration to Neo4j...");
        try {
            // 1. Clear database
            neo4jClient.query("MATCH (n) DETACH DELETE n").run();
            log.info("Neo4j database cleared for fresh sync.");

            // 2. Sync all movies and their categories
            movieRepository.findAll().forEach(movie -> {
                Set<CategoryNode> categoryNodes = movie.getCategories().stream()
                        .map(cat -> CategoryNode.builder()
                                .id(cat.getId())
                                .name(cat.getName())
                                .slug(cat.getSlug())
                                .build())
                        .collect(Collectors.toSet());

                MovieNode movieNode = MovieNode.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .slug(movie.getSlug())
                        .posterUrl(movie.getPosterUrl())
                        .views(movie.getViewCount() != null ? movie.getViewCount().intValue() : 0)
                        .rating(4.5)
                        .categories(categoryNodes)
                        .build();

                movieNeo4jRepository.save(movieNode);
            });
            log.info("All movies synced to Neo4j.");

            // 3. Sync all users
            userRepository.findAll().forEach(user -> {
                UserNode userNode = UserNode.builder()
                        .username(user.getUsername())
                        .build();
                userNeo4jRepository.save(userNode);
            });
            log.info("All users synced to Neo4j.");

            // 4. Sync Favorites
            favoriteRepository.findAll().forEach(fav -> {
                if (fav.getUser() != null) {
                    syncFavoriteEdge(fav.getUser().getUsername(), fav.getMovieSlug(), true);
                }
            });
            log.info("All favorite edges synced to Neo4j.");

            // 5. Sync History
            watchHistoryRepository.findAll().forEach(hist -> {
                if (hist.getUser() != null) {
                    syncWatchHistoryEdge(hist.getUser().getUsername(), hist.getMovieSlug());
                }
            });
            log.info("All watch history edges synced to Neo4j.");
            log.info("Complete graph migration completed successfully!");
        } catch (Exception e) {
            log.error("Failed to run complete graph migration to Neo4j", e);
        }
    }
}
