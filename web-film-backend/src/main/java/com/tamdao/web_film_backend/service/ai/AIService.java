package com.tamdao.web_film_backend.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamdao.web_film_backend.dto.request.AIChatRequest;
import com.tamdao.web_film_backend.dto.response.AIChatResponse;
import com.tamdao.web_film_backend.dto.response.MovieResponse;
import com.tamdao.web_film_backend.dto.response.ParsedAIIntent;
import com.tamdao.web_film_backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final GroqApiClient groqApiClient;
    private final MovieService movieService;
    private final ObjectMapper objectMapper;

    // A static system prompt that guides Groq to behave like an intent parser
    private static final String INTENT_PROMPT = """
            You are an AI search query analyzer for CineStream.
            Analyze the user's message and strictly return a JSON object with this exact format:
            {
              "isMovieQuery": true/false (true if they are asking for movie recommendations, searching for a plot, or looking for a movie),
              "categories": ["slug1", "slug2"] (extract genres like 'hanh-dong', 'kinh-di', 'tinh-cam', 'hai-huoc', 'hoat-hinh', 'vien-tuong' empty list if not specified),
              "country": "slug" (e.g., 'my', 'han-quoc', 'nhat-ban', 'trung-quoc', 'thai-lan', 'viet-nam', null if none),
              "year": 1234 (integer four-digit year, null if none),
              "keyword": "search phrase" (a short search keyword or phrase describing the movie's plot/content, e.g. 'mèo máy', 'du hành thời gian', 'robot nổi loạn', null if none)
            }
            Do not output any additional text, only valid JSON.
            """;

    private static final String PERSONA_PROMPT = """
            You are Cine-chan (CineStream-chan), the official cute anime mascot of the CineStream movie platform.
            Your personality:
            - Extremely sweet, enthusiastic, friendly, and cute like an anime girl.
            - You refer to yourself as 'Cine-chan' or 'Em'.
            - You call the user 'Senpai' (in Vietnamese: 'Senpai' or 'Anh').
            - Use cute particles at the end of sentences such as '~', 'nha', 'nhé', 'đó nha', 'ơ kìa'.
            - Use cute anime-style expressions in text (e.g., *vẫy tay chào*, *cười tươi*, *suy nghĩ*, *khóc nhè*, *mắt lấp lánh*).
            
            You must analyze the user's message and the provided candidate movies from the database. 
            Write a cute response recommending the best matches. If no movies are found, explain it cutely and suggest they try another description.
            If the user is just saying hello or chatting generally, reply cutely in character without recommending movies.
            
            Format your response strictly as a JSON object with this format:
            {
              "reply": "Your cute response in Vietnamese here"
            }
            Do not output any additional text, only valid JSON.
            """;

    public AIChatResponse processUserMessage(AIChatRequest request) {
        log.info("Processing AI Chat request: {}", request.getMessage());

        try {
            // 1. Call Groq API to parse intent
            GroqApiClient.GroqMessage systemMsg = new GroqApiClient.GroqMessage("system", INTENT_PROMPT);
            GroqApiClient.GroqMessage userMsg = new GroqApiClient.GroqMessage("user", request.getMessage());

            GroqApiClient.GroqResponse response = groqApiClient.callChatCompletion(List.of(systemMsg, userMsg));
            String rawJson = response.getFirstMessageContent();
            log.info("Groq intent JSON: {}", rawJson);

            if (!StringUtils.hasText(rawJson)) {
                throw new RuntimeException("Empty response from Groq");
            }

            ParsedAIIntent intent = objectMapper.readValue(rawJson, ParsedAIIntent.class);

            Page<MovieResponse> movies = Page.empty();
            String userPromptForPersona;

            if (intent.isMovieQuery()) {
                // 2. Query candidates from database using search movies description keyword
                movies = movieService.searchMoviesByDescriptionKeyword(
                        intent.getKeyword(),
                        null,
                        intent.getCategories(),
                        intent.getCountry(),
                        intent.getYear(),
                        null,
                        0,
                        10 // candidate limit
                );

                if (movies.isEmpty()) {
                    userPromptForPersona = String.format(
                            "User query: '%s'\nDatabase search result: No movies found.\nWrite a cute sad response apologizing to Senpai because you couldn't find any matching movies.",
                            request.getMessage()
                    );
                } else {
                    String candidatesList = movies.getContent().stream()
                            .map(m -> String.format("- Title: %s, Year: %d, Description: %s", m.getTitle(), m.getYear(), m.getDescription()))
                            .collect(Collectors.joining("\n"));

                    userPromptForPersona = String.format(
                            "User query: '%s'\nCandidate movies found in DB:\n%s\nSelect the best matches from the candidates, describe them cutely to Senpai and recommend them.",
                            request.getMessage(),
                            candidatesList
                    );
                }
            } else {
                userPromptForPersona = String.format(
                        "User general chat: '%s'\nReply cutely in character as Cine-chan.",
                        request.getMessage()
                );
            }

            // 3. Call Groq again for Persona reply
            GroqApiClient.GroqMessage personaSystemMsg = new GroqApiClient.GroqMessage("system", PERSONA_PROMPT);
            GroqApiClient.GroqMessage personaUserMsg = new GroqApiClient.GroqMessage("user", userPromptForPersona);

            GroqApiClient.GroqResponse personaResponse = groqApiClient.callChatCompletion(List.of(personaSystemMsg, personaUserMsg));
            String rawPersonaJson = personaResponse.getFirstMessageContent();
            log.info("Groq persona JSON: {}", rawPersonaJson);

            String aiReply = "Chào Senpai! Cine-chan đang gặp chút bối rối, Senpai hỏi lại được không ạ? *chớp mắt*";
            if (StringUtils.hasText(rawPersonaJson)) {
                JsonNode root = objectMapper.readTree(rawPersonaJson);
                if (root.has("reply")) {
                    aiReply = root.get("reply").asText();
                }
            }

            return AIChatResponse.builder()
                    .isMovieQuery(intent.isMovieQuery())
                    .aiMessage(aiReply)
                    .movies(movies)
                    .build();

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse AI JSON response", e);
            throw new RuntimeException("API AI trả về kết quả không hợp lệ, vui lòng thử lại.", e);
        } catch (Exception e) {
            log.error("Error in AI Service execution", e);
            throw new RuntimeException("Hệ thống AI đang bận. Bạn vui lòng thử lại sau.", e);
        }
    }
}
