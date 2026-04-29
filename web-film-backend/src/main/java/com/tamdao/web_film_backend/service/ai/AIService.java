package com.tamdao.web_film_backend.service.ai;

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

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final GroqApiClient groqApiClient;
    private final MovieService movieService;
    private final ObjectMapper objectMapper;

    // A static system prompt that guides Groq to behave like an intent parser
    private static final String SYSTEM_PROMPT = """
            You are an AI assistant for a movie streaming app called CineStream. 
            The user will give you a query. Your job is to extract their search intent and strictly return a JSON object with this exact format:
            {
              "isMovieQuery": true/false (false if the user is just saying hi or asking non-movie stuff),
              "categories": ["slug1", "slug2"] (extract genres like 'hanh-dong', 'kinh-di', 'tinh-cam', 'hai-huoc', 'hoat-hinh', 'vien-tuong' empty list if not specified),
              "country": "slug" (e.g., 'my', 'han-quoc', 'nhat-ban', 'trung-quoc', 'thai-lan', 'viet-nam', null if none),
              "year": 1234 (integer four-digit year, null if none)
            }
            Do not output any additional text, only valid JSON.
            """;

    public AIChatResponse processUserMessage(AIChatRequest request) {
        log.info("Processing AI Chat request: {}", request.getMessage());

        try {
            // 1. Call Groq API
            GroqApiClient.GroqMessage systemMsg = new GroqApiClient.GroqMessage("system", SYSTEM_PROMPT);
            GroqApiClient.GroqMessage userMsg = new GroqApiClient.GroqMessage("user", request.getMessage());

            GroqApiClient.GroqResponse response = groqApiClient.callChatCompletion(java.util.List.of(systemMsg, userMsg));
            String rawJson = response.getFirstMessageContent();
            log.info("Groq returned JSON: {}", rawJson);

            if (!StringUtils.hasText(rawJson)) {
                throw new RuntimeException("Empty response from Groq");
            }

            // 2. Parse the output JSON into DTO
            ParsedAIIntent intent = objectMapper.readValue(rawJson, ParsedAIIntent.class);

            // 3. Check if it is actually a movie search
            if (!intent.isMovieQuery()) {
                return AIChatResponse.builder()
                        .isMovieQuery(false)
                        .aiMessage("Xin chào! Tôi là Trợ lý AI của CineStream. Hiện tại tôi chỉ có thể giúp bạn tìm kiếm phim ảnh. Hãy cho tôi biết bộ phim bạn muốn xem nhé!")
                        .movies(Page.empty())
                        .build();
            }

            // 4. Transform intent directly into database filters
            Page<MovieResponse> movies = movieService.filterMovies(
                    null, // type (series/single/hoathinh)
                    intent.getCategories(),
                    intent.getCountry(),
                    intent.getYear(),
                    null, // status
                    0, // page 0
                    20 // take 20 results maximum for chat UI
            );

            // 5. Build friendly response text based on result count
            String aiReply;
            if (movies.isEmpty()) {
                aiReply = "Rất tiếc, tôi không tìm thấy bộ phim nào phù hợp với yêu cầu của bạn lúc này. Bạn thử đổi từ khóa khác nhé!";
            } else {
                aiReply = "Tuyệt vời, tôi đã tìm thấy một số bộ phim phù hợp với mong muốn của bạn. Mời bạn chọn nhé!";
            }

            return AIChatResponse.builder()
                    .isMovieQuery(true)
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
