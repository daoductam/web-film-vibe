package com.tamdao.web_film_backend.entity.neo4j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryNode {
    @Id
    private Long id;
    private String name;
    private String slug;
}
