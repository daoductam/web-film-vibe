import axios from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse, AuthResponse } from '../types';
import { useAuthStore } from '../store/authStore';
import { useToastStore } from '../store/toastStore';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1',
    headers: { 'Content-Type': 'application/json' },
});

type SessionRequest = InternalAxiosRequestConfig & { _retry?: boolean; _userId?: number };

api.interceptors.request.use(config => {
    const { token, user } = useAuthStore.getState();
    (config as SessionRequest)._userId = user?.id;
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken() {
    const previousRefreshToken = useAuthStore.getState().refreshToken;
    try {
        if (!previousRefreshToken) throw new Error('No refresh token');
        const response = await axios.post<ApiResponse<AuthResponse>>(`${api.defaults.baseURL}/auth/refresh`, {}, {
            headers: { Authorization: `Bearer ${previousRefreshToken}` },
        });
        if (!response.data.success || !response.data.data?.accessToken) throw new Error('Invalid refresh response');
        if (useAuthStore.getState().refreshToken !== previousRefreshToken) throw new Error('Session changed');
        const { accessToken, refreshToken } = response.data.data;
        useAuthStore.getState().setTokens(accessToken, refreshToken);
        return accessToken;
    } catch (error) {
        if (useAuthStore.getState().refreshToken === previousRefreshToken) {
            useAuthStore.getState().logout();
            useToastStore.getState().showToast('Phiên đăng nhập hết hạn, vui lòng đăng nhập lại', 'warning');
        }
        throw error;
    }
}

api.interceptors.response.use(response => response, async error => {
    const request = error.config as SessionRequest | undefined;
    const authEndpoint = /\/auth\/(login|register|refresh)$/.test(request?.url ?? '');
    if (error.response?.status === 401 && request && !request._retry && !authEndpoint && request._userId === useAuthStore.getState().user?.id && useAuthStore.getState().refreshToken) {
        request._retry = true;
        if (!refreshPromise) refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null; });
        const accessToken = await refreshPromise;
        request.headers.Authorization = `Bearer ${accessToken}`;
        return api(request);
    }
    if (error.response?.status >= 500) useToastStore.getState().showToast('Lỗi máy chủ, vui lòng thử lại sau', 'error');
    return Promise.reject(error);
});

export default api;
