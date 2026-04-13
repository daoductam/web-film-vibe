package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.WatchHistoryRequest;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.WatchHistoryResponse;
import com.tamdao.web_film_backend.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users/me/history")
@RequiredArgsConstructor
@Tag(name = "Watch History", description = "Manage user's watch history (server-synced)")
@SecurityRequirement(name = "bearerAuth")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @GetMapping
    @Operation(summary = "Get my watch history", description = "Get watch history from server (most recent first)")
    public ResponseEntity<ApiResponse<List<WatchHistoryResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                watchHistoryService.getHistory(userDetails.getUsername()),
                "Watch history fetched successfully"
        ));
    }

    @PostMapping
    @Operation(summary = "Save watch progress",
            description = "Upsert watch progress for a movie/episode — call when pausing or finishing")
    public ResponseEntity<ApiResponse<WatchHistoryResponse>> saveHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WatchHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                watchHistoryService.saveHistory(userDetails.getUsername(), request),
                "Watch history saved"
        ));
    }

    @PostMapping("/sync")
    @Operation(summary = "Batch sync watch history",
            description = "Sync all local watch history from device to server on first login")
    public ResponseEntity<ApiResponse<List<WatchHistoryResponse>>> syncHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<@Valid WatchHistoryRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(
                watchHistoryService.syncHistory(userDetails.getUsername(), requests),
                "Watch history synced successfully"
        ));
    }
}
