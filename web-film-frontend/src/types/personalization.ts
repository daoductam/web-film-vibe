export interface WatchHistory {
    movieSlug: string;
    title: string;
    thumbUrl: string;
    lastEpisodeSlug: string;
    lastEpisodeName: string;
    progressMs: number;
    durationMs: number;
    updatedAt: string;
}

export interface WatchHistoryRequest {
    movieSlug: string;
    episodeSlug: string;
    progressMs: number;
    durationMs: number;
}

export interface Favorite {
    movieSlug: string;
    title: string;
    thumbUrl: string;
    quality?: string;
    year?: number;
    createdAt: string;
}

export interface SyncFavoriteRequest {
    movieSlug: string;
    title: string;
    thumbUrl: string;
    quality?: string;
    year?: number;
    createdAt: string; // ISO String
}
