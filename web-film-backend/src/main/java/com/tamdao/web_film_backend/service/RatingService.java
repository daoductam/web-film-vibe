package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.RatingRequest;
import com.tamdao.web_film_backend.dto.response.RatingResponse;
import com.tamdao.web_film_backend.entity.Rating;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.repository.RatingRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addOrUpdateRating(RatingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Rating rating = ratingRepository.findByMovieSlugAndUserId(request.getMovieSlug(), user.getId())
                .orElse(Rating.builder()
                        .user(user)
                        .movieSlug(request.getMovieSlug())
                        .build());

        rating.setScore(request.getScore());
        ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public RatingResponse getMovieRating(String movieSlug) {
        Double average = ratingRepository.getAverageScoreByMovieSlug(movieSlug);
        Long count = ratingRepository.countByMovieSlug(movieSlug);
        
        Integer userRating = null;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")) {
            userRating = userRepository.findByUsername(username)
                    .flatMap(user -> ratingRepository.findByMovieSlugAndUserId(movieSlug, user.getId()))
                    .map(Rating::getScore)
                    .orElse(null);
        }

        return RatingResponse.builder()
                .averageRating(average != null ? (double) Math.round(average * 10) / 10 : 0.0)
                .totalRatings(count)
                .userRating(userRating)
                .build();
    }
}
