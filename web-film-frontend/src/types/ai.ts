export interface AIChatMessage {
    role: 'user' | 'assistant';
    content: string;
}

export interface AIChatRequest {
    query: string;
    history: AIChatMessage[];
}

export interface AIChatResponse {
    reply: string;
}
