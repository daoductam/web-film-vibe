Started at:  2026/07/08 08:21:00
Finished at: 2026/07/08 08:22:00
Total time: 1 minute
---

# Bug Report: Movie Recommendation Feature Not Working

## Symptoms

- **Error message:** GraphQL query failure or connection exception when attempting to fetch recommendations / similar movies.
- **Affected area:** RecommendationGraphQLController, movieService.getPersonalizedRecommendations, movieService.getSimilarMovies, and frontend UI recommendation panels.

## Reproduction

1. Start the backend application.
2. Log in and navigate to the Movie details page or Home page where recommendations/similar movies are displayed.
3. Observe that the recommendation elements do not load or show errors.
- **Reproducibility:** Always
- **Environment:** Windows, Local Development (Dev)

## Expected vs Actual

- **Expected:** Personalized recommendations and similar movies are retrieved successfully from Neo4j and rendered.
- **Actual:** Recommendations fail to load.

## Context

- **Recent changes:** None specified.
- **Domain skills referenced:** `api-design`, `database-design`

## Hypotheses

| # | Hypothesis | Likelihood | Mechanism | Evidence Needed | Where to Look |
|---|-----------|-----------|-----------|-----------------|---------------|
| 1 | Neo4j database service is offline/unreachable | High | Docker container for Neo4j is not running, causing connection failures for GraphSyncService/Repositories. | Test connection to port 7687 or check backend startup/query logs. | `application.yml`, Docker status, Spring Boot console |
| 2 | Incorrect GraphQL API path resolution | Medium | The frontend calls `api.post('/../graphql')` which might resolve incorrectly or get blocked by security filters. | Check browser network logs for 404/403/401 errors on `/graphql` or `/api/graphql`. | `movie.service.ts`, `SecurityConfig.java` |
| 3 | GraphQL Authentication Principal resolver issue | Low | `@AuthenticationPrincipal UserDetails` is null or failing to resolve, causing empty recommendations to be returned. | Check backend logs for "Anonymous access to personalized recommendations". | `RecommendationGraphQLController.java` |

