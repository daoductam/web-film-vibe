package com.tamdao.web_film_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.data.web.config.EnableSpringDataWebSupport(pageSerializationMode = org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebFilmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebFilmBackendApplication.class, args);
	}

}
