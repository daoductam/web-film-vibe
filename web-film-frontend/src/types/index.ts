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
    duration: string; // or time in backend? usually duration string
    totalEpisodes: number;
    currentEpisode: string;
    director: string;
    casts: string; // or actors
    categories: Category[];
    countries: Country[];
    createdAt: string;
    updatedAt: string;
    // Adding optional fields for detail page mapping if needed
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
    serverName: string; // This might be redundant inside ServerEpisodeGroup but useful
    linkEmbed: string;
    linkM3u8: string;
}

export interface ServerEpisodeGroup {
    serverName: string;
    episodes: Episode[];
}

export interface MovieDetail extends Movie {
    servers: ServerEpisodeGroup[];
    // episodes property is NOT returned by backend directly, it's inside servers
}

export interface PageResponse<T> {
    content: T[];
    // Spring Boot Page serialization often puts these in a nested 'page' object if simplified,
    // or properly at root. Based on 'temp_single_count.json', it is in 'page' object.
    page?: {
        size: number;
        number: number;
        totalElements: number;
        totalPages: number;
    };
    // Keep these for compatibility if some endpoints return differently, but mark as optional
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

