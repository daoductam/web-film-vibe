package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.EpisodeResponse;
import com.tamdao.web_film_backend.entity.Episode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {

    @Mapping(target = "sourceProvider", expression = "java(episode.getSourceProvider() != null ? episode.getSourceProvider().name() : null)")
    EpisodeResponse toResponse(Episode episode);

    List<EpisodeResponse> toResponseList(List<Episode> episodes);
}
