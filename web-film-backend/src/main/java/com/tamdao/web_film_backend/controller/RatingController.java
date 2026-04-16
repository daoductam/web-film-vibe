package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.RatingRequest;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.RatingResponse;
import com.tamdao.web_film_backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Movie Rating API endpoints")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Add or update a rating", description = "Submit a 1-5 star rating for a movie. Re-submitting updates the existing score.")
    public ResponseEntity<ApiResponse<Void>> addOrUpdateRating(@Valid @RequestBody RatingRequest request) {
        ratingService.addOrUpdateRating(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{movieSlug}")
    @Operation(summary = "Get movie rating info", description = "Get average rating, total count, and current user's rating score.")
    public ResponseEntity<ApiResponse<RatingResponse>> getMovieRating(@PathVariable String movieSlug) {
        RatingResponse response = ratingService.getMovieRating(movieSlug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
