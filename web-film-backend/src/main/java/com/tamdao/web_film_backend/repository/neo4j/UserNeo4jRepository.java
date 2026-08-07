package com.tamdao.web_film_backend.repository.neo4j;

import com.tamdao.web_film_backend.entity.neo4j.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNode, String> {
}
