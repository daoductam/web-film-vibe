package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.CommentRequest;
import com.tamdao.web_film_backend.dto.response.CommentResponse;
import com.tamdao.web_film_backend.entity.Comment;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.mapper.CommentMapper;
import com.tamdao.web_film_backend.repository.CommentLikeRepository;
import com.tamdao.web_film_backend.repository.CommentRepository;
import com.tamdao.web_film_backend.repository.MovieRepository;
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
    private final MovieRepository movieRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForEpisode(String movieSlug, String episodeSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String currentUsername = getCurrentUsername();

        Page<Comment> mainComments = commentRepository.findByMovieSlugAndEpisodeSlugAndParentIdIsNullOrderByCreatedAtDesc(movieSlug, episodeSlug, pageable);

        com.tamdao.web_film_backend.entity.Movie movie = movieRepository.findBySlug(movieSlug).orElse(null);
        java.util.Map<String, String> episodeNameMap = movie != null ? 
            movie.getEpisodes().stream().collect(java.util.stream.Collectors.toMap(com.tamdao.web_film_backend.entity.Episode::getSlug, com.tamdao.web_film_backend.entity.Episode::getName, (a, b) -> a)) : 
            new java.util.HashMap<>();

        return mainComments.map(comment -> {
            CommentResponse response = mapToResponseWithLikes(comment, currentUsername, episodeNameMap);
            
            // Map replies (Level 2)
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            response.setReplies(replies.stream()
                    .map(reply -> mapToResponseWithLikes(reply, currentUsername, episodeNameMap))
                    .collect(java.util.stream.Collectors.toList()));
            
            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForMovie(String movieSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String currentUsername = getCurrentUsername();

        Page<Comment> mainComments = commentRepository.findByMovieSlugAndParentIdIsNullOrderByCreatedAtDesc(movieSlug, pageable);

        com.tamdao.web_film_backend.entity.Movie movie = movieRepository.findBySlug(movieSlug).orElse(null);
        java.util.Map<String, String> episodeNameMap = movie != null ? 
            movie.getEpisodes().stream().collect(java.util.stream.Collectors.toMap(com.tamdao.web_film_backend.entity.Episode::getSlug, com.tamdao.web_film_backend.entity.Episode::getName, (a, b) -> a)) : 
            new java.util.HashMap<>();

        return mainComments.map(comment -> {
            CommentResponse response = mapToResponseWithLikes(comment, currentUsername, episodeNameMap);
            
            // Map replies (Level 2)
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            response.setReplies(replies.stream()
                    .map(reply -> mapToResponseWithLikes(reply, currentUsername, episodeNameMap))
                    .collect(java.util.stream.Collectors.toList()));
            
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
        
        // Fetch episode name for response
        java.util.Map<String, String> episodeNameMap = new java.util.HashMap<>();
        if (saved.getMovieSlug() != null && saved.getEpisodeSlug() != null) {
            movieRepository.findBySlug(saved.getMovieSlug()).ifPresent(m -> {
                m.getEpisodes().stream()
                    .filter(e -> e.getSlug().equals(saved.getEpisodeSlug()))
                    .findFirst()
                    .ifPresent(e -> episodeNameMap.put(e.getSlug(), e.getName()));
            });
        }

        return mapToResponseWithLikes(saved, username, episodeNameMap);
    }

    @Transactional
    public void deleteComment(Long id) {
        String username = getCurrentUsername();
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponseWithLikes(Comment comment, String currentUsername, java.util.Map<String, String> episodeNameMap) {
        CommentResponse response = commentMapper.toResponse(comment);
        response.setLikeCount(likeRepository.countByCommentId(comment.getId()));
        
        // Set metadata
        response.setMovieSlug(comment.getMovieSlug());
        response.setEpisodeSlug(comment.getEpisodeSlug());
        response.setEpisodeName(episodeNameMap.getOrDefault(comment.getEpisodeSlug(), "Full"));

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
