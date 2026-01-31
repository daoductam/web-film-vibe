package com.tamdao.web_film_backend.mapper;

import com.tamdao.web_film_backend.dto.response.CountryResponse;
import com.tamdao.web_film_backend.entity.Country;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);

    List<CountryResponse> toResponseList(List<Country> countries);

    Set<CountryResponse> toResponseSet(Set<Country> countries);
}
