package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.response.CountryResponse;
import com.tamdao.web_film_backend.mapper.CountryMapper;
import com.tamdao.web_film_backend.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @Transactional(readOnly = true)
    public List<CountryResponse> getAllCountries() {
        return countryMapper.toResponseList(countryRepository.findAll());
    }
}
