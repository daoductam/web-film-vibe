import api from './api';
import type { ApiResponse, UpdateProfileRequest, UserProfile } from '../types';

export const userService = {
    getAvatarUrl: (avatarUrl?: string): string | undefined => {
        if (!avatarUrl) return undefined;
        if (/^https?:\/\//i.test(avatarUrl)) return avatarUrl;
        return `${api.defaults.baseURL?.replace(/\/$/, '')}/users/avatars/${encodeURIComponent(avatarUrl)}`;
    },

    changePassword: async (request: { currentPassword: string; newPassword: string; confirmPassword: string }): Promise<void> => {
        await api.put('/users/me/password', request);
    },
    getProfile: async (): Promise<UserProfile> => {
        const response = await api.get<ApiResponse<UserProfile>>('/users/me');
        return response.data.data;
    },
    
    updateProfile: async (request: UpdateProfileRequest): Promise<UserProfile> => {
        const response = await api.put<ApiResponse<UserProfile>>('/users/me', request);
        return response.data.data;
    },
    
    uploadAvatar: async (file: File): Promise<UserProfile> => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post<ApiResponse<UserProfile>>('/users/me/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data.data;
    }
};
