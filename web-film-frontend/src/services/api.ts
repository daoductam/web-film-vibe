import axios from 'axios';
import { useAuthStore } from '../store/authStore';
import { useToastStore } from '../store/toastStore';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add the auth token
api.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().token;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

const onRefreshed = (token: string) => {
  refreshSubscribers.map((callback) => callback(token));
};

const addRefreshSubscriber = (callback: (token: string) => void) => {
  refreshSubscribers.push(callback);
};

// Response interceptor
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const { config, response } = error;
        const originalRequest = config;

        if (response && response.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve) => {
                  addRefreshSubscriber((token) => {
                    originalRequest.headers.Authorization = 'Bearer ' + token;
                    resolve(api(originalRequest));
                  });
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = useAuthStore.getState().refreshToken;
            if (refreshToken) {
                try {
                    const res = await axios.post(`${api.defaults.baseURL}/auth/refresh`, {}, {
                        headers: { Authorization: `Bearer ${refreshToken}` }
                    });
                    
                    if (res.data.code === 200) {
                        const { accessToken, refreshToken: newRefreshToken } = res.data.data;
                        useAuthStore.getState().setTokens(accessToken, newRefreshToken);
                        isRefreshing = false;
                        onRefreshed(accessToken);
                        refreshSubscribers = [];
                        
                        originalRequest.headers.Authorization = 'Bearer ' + accessToken;
                        return api(originalRequest);
                    }
                } catch (refreshError) {
                    isRefreshing = false;
                    useAuthStore.getState().logout();
                    useToastStore.getState().showToast('Phiên đăng nhập hết hạn, vui lòng đăng nhập lại', 'warning');
                    // window.location.href = '/login';
                }
            } else {
                useAuthStore.getState().logout();
            }
        }

        // Global error handling for 500 etc.
        if (response && response.status >= 500) {
            useToastStore.getState().showToast('Lỗi máy chủ, vui lòng thử lại sau', 'error');
        }

        return Promise.reject(error);
    }
);

export default api;
