import api from './api';
import type { Movie, PageResponse, ApiResponse } from '../types';

export const movieService = {
    getAllMovies: async (page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>(`/movies/latest?page=${page}&size=${size}`);
        return response.data.data;
    },

    getLatestMovies: async (page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>(`/movies/latest?page=${page}&size=${size}`);
        return response.data.data;
    },

    getPopularMovies: async (page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>(`/movies/popular?page=${page}&size=${size}`);
        return response.data.data;
    },
    
    getMoviesByCategory: async (categorySlug: string, page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>(`/movies/category/${categorySlug}?page=${page}&size=${size}`);
        return response.data.data;
    },

    getMoviesByCountry: async (countrySlug: string, page = 0, size = 10) => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>(`/movies/country/${countrySlug}?page=${page}&size=${size}`);
        return response.data.data;
    },

    searchMovies: async (keyword: string, page = 1, size = 24): Promise<PageResponse<Movie>> => {
        const response = await api.get<ApiResponse<PageResponse<Movie>>>('/movies/search', {
            params: { q: keyword, page: page - 1, size }
        });
        return response.data.data;
    },

    filterMovies: async (params: {
        type?: string;
        category?: string;
        country?: string;
        year?: number;
        status?: string;
        sort?: string;
        page?: number;
        size?: number;
    }): Promise<PageResponse<Movie>> => {
        const { page = 1, size = 24, ...rest } = params;
        const response = await api.get<ApiResponse<PageResponse<Movie>>>('/movies/filter', {
            params: { ...rest, page: page - 1, size }
        });
        return response.data.data;
    },
    
    getMovieDetail: async (slug: string) => {
         const response = await api.get<ApiResponse<any>>(`/movies/${slug}`);
         return response.data.data;
    }
};
