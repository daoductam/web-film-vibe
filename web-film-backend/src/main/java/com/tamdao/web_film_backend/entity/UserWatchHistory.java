package com.tamdao.web_film_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_watch_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_slug"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "movie_slug", nullable = false)
    private String movieSlug;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "thumb_url")
    private String thumbUrl;

    @Column(name = "last_episode_slug")
    private String lastEpisodeSlug;

    @Column(name = "last_episode_name")
    private String lastEpisodeName;

    @Column(name = "progress_ms")
    private Long progressMs;

    @Column(name = "duration_ms")
    private Long durationMs;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
