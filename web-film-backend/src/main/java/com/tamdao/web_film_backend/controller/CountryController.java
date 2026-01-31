package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.CountryResponse;
import com.tamdao.web_film_backend.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Countries", description = "Country API endpoints")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    @Operation(summary = "Get all countries", description = "Get list of all movie origin countries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getAllCountries() {
        return ResponseEntity.ok(ApiResponse.success(countryService.getAllCountries()));
    }
}
