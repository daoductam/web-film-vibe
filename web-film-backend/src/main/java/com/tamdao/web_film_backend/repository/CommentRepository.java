package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEpisodeSlugAndParentIdIsNullOrderByCreatedAtDesc(String episodeSlug, Pageable pageable);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByEpisodeSlugAndParentIdIsNull(String episodeSlug);
}
