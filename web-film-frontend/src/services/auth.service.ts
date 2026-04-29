import api from './api';
import type { ApiResponse, AuthResponse, UserProfile } from '../types';

export interface LoginRequest {
    username: string;
    password?: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password?: string;
    fullName: string;
}

export const authService = {
    login: async (request: LoginRequest): Promise<AuthResponse> => {
        const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', request);
        return response.data.data;
    },
    
    register: async (request: RegisterRequest): Promise<UserProfile> => {
        const response = await api.post<ApiResponse<UserProfile>>('/auth/register', request);
        return response.data.data;
    }
};
