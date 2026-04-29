import api from './api';
import type { ApiResponse, AIChatRequest, AIChatResponse } from '../types';

export const aiService = {
    chat: async (request: AIChatRequest): Promise<AIChatResponse> => {
        const response = await api.post<ApiResponse<AIChatResponse>>('/ai/chat', request);
        return response.data.data;
    }
};
