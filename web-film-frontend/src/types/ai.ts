import type { Movie, PageResponse } from './index';

export interface AIChatMessage {
    role: 'user' | 'assistant';
    content: string;
    movies?: Movie[];
}

export interface AIChatRequest {
    query: string;
    history: AIChatMessage[];
}

export interface AIChatResponse {
    isMovieQuery: boolean;
    aiMessage: string;
    movies: PageResponse<Movie>;
}
