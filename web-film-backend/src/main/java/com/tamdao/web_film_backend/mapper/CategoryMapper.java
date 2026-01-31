package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.CategoryResponse;
import com.tamdao.web_film_backend.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    Set<CategoryResponse> toResponseSet(Set<Category> categories);
}
