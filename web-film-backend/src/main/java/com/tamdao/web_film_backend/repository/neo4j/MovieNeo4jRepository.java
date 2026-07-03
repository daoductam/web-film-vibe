package com.tamdao.web_film_backend.repository.neo4j;

import com.tamdao.web_film_backend.entity.neo4j.MovieNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieNeo4jRepository extends Neo4jRepository<MovieNode, Long> {

    @Query("MATCH (u:User {username: $username}) " +
           "MATCH (u)-[r:FAVORITED|WATCHED]->(m:Movie)-[:BELONGS_TO]->(c:Category) " +
           "WITH u, c, SUM(CASE type(r) WHEN 'FAVORITED' THEN 3.0 ELSE 1.0 END) AS categoryWeight " +
           "MATCH (candidate:Movie)-[:BELONGS_TO]->(c) " +
           "WHERE NOT (u)-[:WATCHED|FAVORITED]->(candidate) " +
           "WITH candidate, SUM(categoryWeight) AS score " +
           "RETURN candidate " +
           "ORDER BY score DESC, candidate.views DESC " +
           "LIMIT 10")
    List<MovieNode> getPersonalizedRecommendations(@Param("username") String username);

    @Query("MATCH (m:Movie {slug: $slug})-[:BELONGS_TO]->(c:Category)<-[:BELONGS_TO]-(candidate:Movie) " +
           "WHERE candidate <> m " +
           "RETURN candidate " +
           "ORDER BY candidate.views DESC " +
           "LIMIT 10")
    List<MovieNode> getSimilarMovies(@Param("slug") String slug);
}
