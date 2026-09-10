import api from './api';
import type { ApiResponse, PageResponse } from '../types';

export interface UserNotification {
    id: number;
    title: string;
    content: string;
    type: string;
    movieSlug: string | null;
    thumbUrl: string | null;
    isRead: boolean;
    createdAt: string;
}

type NotificationPayload = Omit<UserNotification, 'isRead'> & { isRead?: boolean; read?: boolean };
const PAGE_SIZE = 20;

export const notificationService = {
    async getNotifications(page: number, signal?: AbortSignal) {
        const { data } = await api.get<ApiResponse<PageResponse<NotificationPayload>>>('/notifications', {
            params: { page, size: PAGE_SIZE }, signal,
        });
        return {
            ...data.data,
            content: data.data.content.map(item => ({ ...item, isRead: item.isRead ?? item.read ?? false })),
        };
    },
    async getUnreadCount(signal?: AbortSignal) {
        const { data } = await api.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count', { signal });
        return data.data.unreadCount;
    },
    async markAsRead(id: number) {
        await api.patch(`/notifications/${id}/read`);
    },
    async markAllAsRead() {
        await api.post('/notifications/read-all');
    },
};
