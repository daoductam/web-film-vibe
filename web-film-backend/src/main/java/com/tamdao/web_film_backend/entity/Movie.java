package com.tamdao.web_film_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_slug", columnList = "slug", unique = true),
        @Index(name = "idx_movie_tmdb_id", columnList = "tmdbId"),
        @Index(name = "idx_movie_imdb_id", columnList = "imdbId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "origin_title")
    private String originTitle;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "thumb_url", length = 500)
    private String thumbUrl;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    private Integer year;

    private String tmdbId;

    private String imdbId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MovieStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MovieType type;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "total_episodes")
    private Integer totalEpisodes;

    @Column(name = "current_episode")
    private String currentEpisode;

    private String quality;

    private String language;

    private String duration;

    private String director;

    private String actors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_categories",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_countries",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    @Builder.Default
    private Set<Country> countries = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Episode> episodes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to add episode
    public void addEpisode(Episode episode) {
        episodes.add(episode);
        episode.setMovie(this);
    }
}
