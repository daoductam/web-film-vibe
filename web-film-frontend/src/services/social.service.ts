import api from './api';
import { ApiResponse, Comment, Rating, PageResponse } from '../types';

export const socialService = {
    // Comments
    getCommentsByEpisode: async (episodeSlug: string, page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Comment>>>(
            `/comments/episode/${episodeSlug}`, {
                params: { page, size }
            }
        );
        return response.data;
    },

    addComment: async (data: { movieSlug?: string; episodeSlug: string; content: string; parentId?: number }) => {
        const response = await api.post<ApiResponse<Comment>>('/comments', data);
        return response.data;
    },

    deleteComment: async (id: number) => {
        const response = await api.delete<ApiResponse<void>>(`/comments/${id}`);
        return response.data;
    },

    toggleLike: async (commentId: number) => {
        const response = await api.post<ApiResponse<boolean>>(`/comments/${commentId}/like`);
        return response.data;
    },

    // Ratings
    getMovieRating: async (movieSlug: string) => {
        const response = await api.get<ApiResponse<Rating>>(`/ratings/${movieSlug}`);
        return response.data;
    },

    addOrUpdateRating: async (movieSlug: string, score: number) => {
        const response = await api.post<ApiResponse<void>>('/ratings', { movieSlug, score });
        return response.data;
    }
};
