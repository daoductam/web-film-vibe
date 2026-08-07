import api from './api';
import type { ApiResponse, Favorite, SyncFavoriteRequest, WatchHistory, WatchHistoryRequest } from '../types';

export const personalizationService = {
    // Favorites
    getFavorites: async (): Promise<Favorite[]> => {
        const response = await api.get<ApiResponse<Favorite[]>>('/users/me/favorites');
        return response.data.data;
    },
    
    addFavorite: async (request: SyncFavoriteRequest): Promise<Favorite> => {
        const response = await api.post<ApiResponse<Favorite>>('/users/me/favorites', request);
        return response.data.data;
    },
    
    removeFavorite: async (slug: string): Promise<void> => {
        await api.delete(`/users/me/favorites/${slug}`);
    },
    
    syncFavorites: async (requests: SyncFavoriteRequest[]): Promise<Favorite[]> => {
        const response = await api.post<ApiResponse<Favorite[]>>('/users/me/favorites/sync', requests);
        return response.data.data;
    },

    // Watch History
    getHistory: async (): Promise<WatchHistory[]> => {
        const response = await api.get<ApiResponse<WatchHistory[]>>('/users/me/history');
        return response.data.data;
    },
    
    saveHistory: async (request: WatchHistoryRequest): Promise<WatchHistory> => {
        const response = await api.post<ApiResponse<WatchHistory>>('/users/me/history', request);
        return response.data.data;
    },
    
    syncHistory: async (requests: WatchHistoryRequest[]): Promise<WatchHistory[]> => {
        const response = await api.post<ApiResponse<WatchHistory[]>>('/users/me/history/sync', requests);
        return response.data.data;
    }
};
