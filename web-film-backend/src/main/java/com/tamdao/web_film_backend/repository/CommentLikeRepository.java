package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    long countByCommentId(Long commentId);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}
