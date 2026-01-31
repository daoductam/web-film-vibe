package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Episode;
import com.tamdao.web_film_backend.entity.SourceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findByMovieIdOrderByServerNameAscNameAsc(Long movieId);

    Optional<Episode> findByMovieIdAndServerNameAndName(Long movieId, String serverName, String name);

    List<Episode> findByMovieIdAndSourceProvider(Long movieId, SourceProvider sourceProvider);

    boolean existsByMovieIdAndServerNameAndNameAndSourceProvider(Long movieId, String serverName, String name, SourceProvider sourceProvider);
}
