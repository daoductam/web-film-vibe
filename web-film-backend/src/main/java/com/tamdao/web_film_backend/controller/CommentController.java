package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.CommentRequest;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.CommentResponse;
import com.tamdao.web_film_backend.dto.response.PageInfo;
import com.tamdao.web_film_backend.service.CommentLikeService;
import com.tamdao.web_film_backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Episode Commenting API endpoints")
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService likeService;

    @GetMapping("/episode/{episodeSlug}")
    @Operation(summary = "Get comments for an episode", description = "Fetch paginated comments for a specific episode, including 2-level threaded replies.")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable String episodeSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CommentResponse> comments = commentService.getCommentsForEpisode(episodeSlug, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments, createPageInfo(comments)));
    }

    @PostMapping
    @Operation(summary = "Add a comment or reply", description = "Post a new comment or reply to an existing comment (limit 2 levels).")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment", description = "Delete a comment or reply. Only the owner can delete their comments.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a comment", description = "Like or unlike a comment. Returns true if liked, false if unliked.")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(@PathVariable Long id) {
        boolean result = likeService.toggleLike(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private PageInfo createPageInfo(Page<?> page) {
        return PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .itemsPerPage(page.getSize())
                .build();
    }
}
