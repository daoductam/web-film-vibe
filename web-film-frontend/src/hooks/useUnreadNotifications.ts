import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { notificationService } from '../services/notification.service';

export function useUnreadNotifications() {
    const user = useAuthStore(state => state.user);
    const token = useAuthStore(state => state.token);
    return useQuery({
        queryKey: ['notifications', user?.id, 'unread-count'],
        queryFn: ({ signal }) => notificationService.getUnreadCount(signal),
        enabled: Boolean(user && token),
        staleTime: 30_000,
        refetchInterval: 60_000,
        gcTime: 0,
    });
}
