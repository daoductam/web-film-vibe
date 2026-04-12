package com.tamdao.web_film_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_slug"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavorite {

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

    @Column(name = "quality")
    private String quality;

    @Column(name = "year")
    private Integer year;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
