package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.SyncFavoriteRequest;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.FavoriteResponse;
import com.tamdao.web_film_backend.service.FavoriteService;
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
@RequestMapping("/v1/users/me/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Manage user's favorite movies (server-synced)")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Get my favorites", description = "Get list of favorite movies from the server")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.getFavorites(userDetails.getUsername()),
                "Favorites fetched successfully"
        ));
    }

    @PostMapping
    @Operation(summary = "Add a favorite", description = "Add a movie to server favorites")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SyncFavoriteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.addFavorite(userDetails.getUsername(), request),
                "Added to favorites"
        ));
    }

    @DeleteMapping("/{slug}")
    @Operation(summary = "Remove a favorite", description = "Remove a movie from server favorites")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String slug) {
        favoriteService.removeFavorite(userDetails.getUsername(), slug);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from favorites"));
    }

    @PostMapping("/sync")
    @Operation(summary = "Batch sync favorites",
            description = "Sync all local favorites from device to server on first login")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> syncFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<@Valid SyncFavoriteRequest> requests) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.syncFavorites(userDetails.getUsername(), requests),
                "Favorites synced successfully"
        ));
    }
}
