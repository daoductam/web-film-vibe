import api from './api';
import type { PageResponse } from '../types';

export interface UserSummary {
  id: number;
  username: string;
  fullName?: string;
  avatarUrl?: string;
}

export interface MovieSummary {
  id: number;
  title: string;
  slug: string;
  posterUrl?: string;
  thumbUrl?: string;
}

export interface EpisodeSummary {
  id: number;
  name: string;
  slug: string;
}

export interface WatchRoom {
  id: number;
  code: string;
  name: string;
  host: UserSummary;
  movie: MovieSummary;
  episode?: EpisodeSummary;
  roomType: 'PUBLIC' | 'PRIVATE';
  maxMembers: number;
  currentMemberCount: number;
  status: 'WAITING' | 'PLAYING' | 'PAUSED' | 'ENDED';
  createdAt: string;
}

export interface WatchRoomMember {
  id: number;
  roomId: number;
  userId: number;
  username: string;
  fullName?: string;
  avatarUrl?: string;
  role: 'HOST' | 'CO_HOST' | 'MEMBER';
  joinedAt: string;
  leftAt?: string;
}

export interface VideoState {
  currentTime: number;
  isPlaying: boolean;
  playbackRate: number;
  lastSyncAt: string;
}

export interface ChatMessage {
  id: number;
  userId?: number;
  username: string;
  avatarUrl?: string;
  content: string;
  messageType: 'CHAT' | 'SYSTEM' | 'SPOILER' | 'AI_RESPONSE';
  timestamp: string;
}

export interface WatchPartyHistory {
  id: number;
  roomId: number;
  roomName: string;
  roomCode: string;
  movieId: number;
  movieTitle: string;
  movieSlug: string;
  moviePosterUrl?: string;
  watchDurationSeconds: number;
  joinedAt: string;
  leftAt: string;
}

export interface CreateWatchRoomRequest {
  name: string;
  movieId: number;
  episodeId?: number;
  roomType: 'PUBLIC' | 'PRIVATE';
  maxMembers?: number;
}

export const watchPartyService = {
  createRoom: async (request: CreateWatchRoomRequest) => {
    const res = await api.post('/watch-rooms', request);
    return res.data.data as WatchRoom;
  },

  getPublicRooms: async (page = 0, size = 10) => {
    const res = await api.get(`/watch-rooms/public?page=${page}&size=${size}`);
    return res.data.data as PageResponse<WatchRoom>;
  },

  getRoomById: async (id: number) => {
    const res = await api.get(`/watch-rooms/${id}`);
    return res.data.data as WatchRoom;
  },

  getRoomByCode: async (code: string) => {
    const res = await api.get(`/watch-rooms/code/${encodeURIComponent(code)}`);
    return res.data.data as WatchRoom;
  },

  joinRoom: async (id: number) => {
    const res = await api.post(`/watch-rooms/${id}/join`);
    return res.data.data as WatchRoomMember;
  },

  leaveRoom: async (id: number) => {
    await api.post(`/watch-rooms/${id}/leave`);
  },

  endRoom: async (id: number) => {
    await api.put(`/watch-rooms/${id}/end`);
  },

  getVideoState: async (id: number) => {
    const res = await api.get(`/watch-rooms/${id}/state`);
    return res.data.data as VideoState;
  },

  updateMemberRole: async (id: number, userId: number, role: 'CO_HOST' | 'MEMBER') => {
    await api.put(`/watch-rooms/${id}/members/${userId}/role?role=${role}`);
  },

  getMessages: async (id: number, page = 0, size = 50) => {
    const res = await api.get(`/watch-rooms/${id}/messages?page=${page}&size=${size}`);
    return res.data.data as PageResponse<ChatMessage>;
  },

  getWatchPartyHistory: async (page = 0, size = 10) => {
    const res = await api.get(`/watch-rooms/history?page=${page}&size=${size}`);
    return res.data.data;
  }
};
