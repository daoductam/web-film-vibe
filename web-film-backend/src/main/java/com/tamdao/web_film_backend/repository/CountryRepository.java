package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findBySlug(String slug);

    Optional<Country> findByName(String name);

    boolean existsBySlug(String slug);
}
