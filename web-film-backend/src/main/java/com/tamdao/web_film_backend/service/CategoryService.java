package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.response.CategoryResponse;
import com.tamdao.web_film_backend.mapper.CategoryMapper;
import com.tamdao.web_film_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }
}
