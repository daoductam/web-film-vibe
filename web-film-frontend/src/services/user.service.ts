import api from './api';
import type { ApiResponse, UpdateProfileRequest, UserProfile } from '../types';

export const userService = {
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
