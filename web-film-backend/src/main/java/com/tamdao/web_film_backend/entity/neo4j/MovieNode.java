package com.tamdao.web_film_backend.entity.neo4j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieNode {
    @Id
    private Long id;
    private String title;
    private String slug;
    private String posterUrl;
    private Integer views;
    private Double rating;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    @Builder.Default
    private Set<CategoryNode> categories = new HashSet<>();
}
