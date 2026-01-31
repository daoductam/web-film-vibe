package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.MovieResponse;
import com.tamdao.web_film_backend.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, CountryMapper.class})
public interface MovieMapper {

    @Mapping(target = "status", expression = "java(movie.getStatus() != null ? movie.getStatus().name() : null)")
    @Mapping(target = "type", expression = "java(movie.getType() != null ? movie.getType().name() : null)")
    MovieResponse toResponse(Movie movie);

    List<MovieResponse> toResponseList(List<Movie> movies);
}
