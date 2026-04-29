export const TYPES_VERSION = '1.0.0';

export interface Category {
    id: number;
    name: string;
    slug: string;
}

export interface Country {
    id: number;
    name: string;
    slug: string;
    code: string;
}

export interface Movie {
    id: number;
    title: string;
    originTitle: string;
    slug: string;
    thumbUrl: string;
    posterUrl: string;
    year: number;
    description: string;
    status: string;
    type: string;
    viewCount: number;
    averageRating?: number;
    ratingCount?: number;
    quality: string;
    language: string;
    duration: string;
    totalEpisodes: number;
    currentEpisode: string;
    director: string;
    casts: string;
    categories: Category[];
    countries: Country[];
    createdAt: string;
    updatedAt: string;
}

export interface Comment {
    id: number;
    username: string;
    fullName: string;
    avatarUrl: string;
    content: string;
    parentId?: number;
    likeCount: number;
    isLiked: boolean;
    movieSlug: string;
    episodeSlug: string;
    episodeName: string;
    replies: Comment[];
    createdAt: string;
    updatedAt: string;
}

export interface Rating {
    averageRating: number;
    totalRatings: number;
    userRating?: number;
}

export interface Episode {
    id: number;
    name: string;
    slug: string;
    serverName: string;
    linkEmbed: string;
    linkM3u8: string;
}

export interface ServerEpisodeGroup {
    serverName: string;
    episodes: Episode[];
}

export interface MovieDetail extends Movie {
    servers: ServerEpisodeGroup[];
}

export interface PageResponse<T> {
    content: T[];
    page?: {
        size: number;
        number: number;
        totalElements: number;
        totalPages: number;
    };
    pageNumber?: number;
    pageSize?: number;
    totalElements?: number;
    totalPages?: number;
    last?: boolean;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface UserProfile {
    username: string;
    email: string;
    fullName: string;
    role: string;
    avatarUrl: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: UserProfile;
}

export interface UpdateProfileRequest {
    fullName: string;
}

export * from './personalization';
export * from './ai';
