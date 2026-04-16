package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.CommentRequest;
import com.tamdao.web_film_backend.dto.response.CommentResponse;
import com.tamdao.web_film_backend.entity.Comment;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.mapper.CommentMapper;
import com.tamdao.web_film_backend.repository.CommentLikeRepository;
import com.tamdao.web_film_backend.repository.CommentRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForEpisode(String episodeSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String currentUsername = getCurrentUsername();

        Page<Comment> mainComments = commentRepository.findByEpisodeSlugAndParentIdIsNullOrderByCreatedAtDesc(episodeSlug, pageable);

        return mainComments.map(comment -> {
            CommentResponse response = mapToResponseWithLikes(comment, currentUsername);
            
            // Map replies (Level 2)
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            response.setReplies(replies.stream()
                    .map(reply -> mapToResponseWithLikes(reply, currentUsername))
                    .collect(Collectors.toList()));
            
            return response;
        });
    }

    @Transactional
    public CommentResponse addComment(CommentRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Enforce max 2 levels
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.getParentId()));
            if (parent.getParentId() != null) {
                throw new RuntimeException("Maximum comment depth reached (2 levels only)");
            }
        }

        Comment comment = Comment.builder()
                .user(user)
                .movieSlug(request.getMovieSlug())
                .episodeSlug(request.getEpisodeSlug())
                .content(request.getContent())
                .parentId(request.getParentId())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponseWithLikes(saved, username);
    }

    @Transactional
    public void deleteComment(Long id) {
        String username = getCurrentUsername();
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        // Delete associated likes first (if not cascading)
        // Or if using cascading, just delete
        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponseWithLikes(Comment comment, String currentUsername) {
        CommentResponse response = commentMapper.toResponse(comment);
        response.setLikeCount(likeRepository.countByCommentId(comment.getId()));
        
        if (currentUsername != null && !currentUsername.equals("anonymousUser")) {
            response.setLiked(likeRepository.existsByCommentIdAndUserId(comment.getId(), 
                    userRepository.findByUsername(currentUsername).map(User::getId).orElse(-1L)));
        }
        
        return response;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
