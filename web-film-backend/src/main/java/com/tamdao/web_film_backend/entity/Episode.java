package com.tamdao.web_film_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "episodes", indexes = {
        @Index(name = "idx_episode_movie_server", columnList = "movie_id, server_name, name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "server_name", nullable = false)
    private String serverName;

    @Column(nullable = false)
    private String name;  // Episode name: "1", "2", "Full", "Tập 10"

    private String slug;

    @Column(name = "link_embed", length = 1000)
    private String linkEmbed;

    @Column(name = "link_m3u8", length = 1000)
    private String linkM3u8;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_provider", length = 20)
    private SourceProvider sourceProvider;

    @Builder.Default
    private Integer priority = 1;  // 1 = highest priority
}
