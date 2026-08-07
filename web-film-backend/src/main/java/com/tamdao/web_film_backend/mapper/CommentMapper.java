package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.CommentResponse;
import com.tamdao.web_film_backend.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "replies", ignore = true) // Handled in service logic for threading
    @Mapping(target = "isLiked", ignore = true)     // Handled in service based on current user
    @Mapping(target = "likeCount", ignore = true) // Handled in service
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);
}
