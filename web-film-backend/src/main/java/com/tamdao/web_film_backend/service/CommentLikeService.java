package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.entity.Comment;
import com.tamdao.web_film_backend.entity.CommentLike;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.repository.CommentLikeRepository;
import com.tamdao.web_film_backend.repository.CommentRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        return likeRepository.findByCommentIdAndUserId(commentId, user.getId())
                .map(like -> {
                    likeRepository.delete(like);
                    return false; // Result is NOT LIKED
                })
                .orElseGet(() -> {
                    likeRepository.save(CommentLike.builder()
                            .user(user)
                            .comment(comment)
                            .build());
                    return true; // Result is LIKED
                });
    }
}
