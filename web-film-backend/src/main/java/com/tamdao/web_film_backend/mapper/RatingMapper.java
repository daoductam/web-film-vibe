package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.RatingResponse;
import com.tamdao.web_film_backend.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalRatings", ignore = true)
    @Mapping(target = "userRating", source = "score")
    RatingResponse toResponse(Rating rating);
}
