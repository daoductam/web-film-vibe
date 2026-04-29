import api from './api';
import type { ApiResponse, Category, Country } from '../types';

export const taxonomyService = {
    getCategories: async (): Promise<Category[]> => {
        const response = await api.get<ApiResponse<Category[]>>('/categories');
        return response.data.data;
    },
    
    getCountries: async (): Promise<Country[]> => {
        const response = await api.get<ApiResponse<Country[]>>('/countries');
        return response.data.data;
    }
};
