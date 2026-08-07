package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.*;
import com.tamdao.web_film_backend.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WatchRoomMapper {

    @Mapping(target = "host", source = "host")
    @Mapping(target = "movie", source = "movie")
    @Mapping(target = "episode", source = "episode")
    @Mapping(target = "roomType", expression = "java(room.getRoomType() != null ? room.getRoomType().name() : null)")
    @Mapping(target = "status", expression = "java(room.getStatus() != null ? room.getStatus().name() : null)")
    @Mapping(target = "currentMemberCount", ignore = true)
    WatchRoomResponse toResponse(WatchRoom room);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    WatchRoomResponse.UserSummary toUserSummary(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "posterUrl", source = "posterUrl")
    @Mapping(target = "thumbUrl", source = "thumbUrl")
    WatchRoomResponse.MovieSummary toMovieSummary(Movie movie);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    WatchRoomResponse.EpisodeSummary toEpisodeSummary(Episode episode);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "role", expression = "java(member.getRole() != null ? member.getRole().name() : null)")
    WatchRoomMemberResponse toMemberResponse(WatchRoomMember member);

    List<WatchRoomMemberResponse> toMemberResponseList(List<WatchRoomMember> members);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomName", source = "room.name")
    @Mapping(target = "roomCode", source = "room.code")
    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "movieSlug", source = "movie.slug")
    @Mapping(target = "moviePosterUrl", source = "movie.posterUrl")
    WatchPartyHistoryResponse toHistoryResponse(WatchPartyHistory history);
}
