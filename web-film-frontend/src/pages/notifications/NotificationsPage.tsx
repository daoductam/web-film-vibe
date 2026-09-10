import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { Bell, CheckCheck, Loader2, RefreshCw } from 'lucide-react';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { useAuthStore } from '../../store/authStore';
import { notificationService, type UserNotification } from '../../services/notification.service';
import { useUnreadNotifications } from '../../hooks/useUnreadNotifications';

function notificationDate(value: string) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? '' : date.toLocaleString('vi-VN');
}

function NotificationInbox({ userId }: { userId: number }) {
    const [page, setPage] = useState(0);
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const unread = useUnreadNotifications();
    const inbox = useQuery({
        queryKey: ['notifications', userId, 'inbox', page],
        queryFn: ({ signal }) => notificationService.getNotifications(page, signal),
        gcTime: 0,
    });
    const refresh = () => queryClient.invalidateQueries({ queryKey: ['notifications', userId] });
    const markRead = useMutation({
        mutationFn: notificationService.markAsRead,
        onSuccess: () => refresh(),
    });
    const markAll = useMutation({
        mutationFn: notificationService.markAllAsRead,
        onSuccess: () => refresh(),
    });
    const openNotification = async (notification: UserNotification) => {
        if (!notification.isRead) {
            try {
                await markRead.mutateAsync(notification.id);
            } catch {
                return;
            }
        }
        if (notification.movieSlug) navigate(`/movie/${encodeURIComponent(notification.movieSlug)}`);
    };
    const totalPages = inbox.data?.page?.totalPages ?? inbox.data?.totalPages ?? 0;
    const busy = markRead.isPending || markAll.isPending;

    return <>
        <div className="flex flex-wrap items-center justify-between gap-4 mb-8">
            <div><h1 className="text-3xl font-bold">Thông báo</h1><p className="text-text-secondary mt-2">Cập nhật mới nhất về những bộ phim bạn quan tâm.</p></div>
            <div className="flex flex-wrap gap-3">
                <button onClick={() => void refresh()} disabled={inbox.isFetching} className="flex items-center gap-2 rounded-xl border border-white/10 px-4 py-2 disabled:opacity-50"><RefreshCw size={18} /> Làm mới</button>
                <button onClick={() => markAll.mutate()} disabled={busy || !unread.data} className="flex items-center gap-2 rounded-xl bg-neon/10 text-neon px-4 py-2 disabled:opacity-50"><CheckCheck size={18} /> Đọc tất cả</button>
            </div>
        </div>
        {(markRead.isError || markAll.isError) && <p role="alert" className="mb-4 text-red-400">Không thể đánh dấu đã đọc. Vui lòng thử lại.</p>}
        {inbox.isPending ? <div role="status" className="flex justify-center gap-3 py-20"><Loader2 className="animate-spin text-neon" /> Đang tải thông báo...</div>
            : inbox.isError ? <div role="alert" className="rounded-2xl border border-white/10 p-10 text-center"><p>Không thể tải thông báo.</p><button onClick={() => void inbox.refetch()} className="mt-4 text-neon">Thử lại</button></div>
                : !inbox.data.content.length ? <div className="text-center py-20 text-text-secondary"><Bell size={48} className="mx-auto mb-4" /><p>Chưa có thông báo nào.</p><p className="mt-2 text-sm">Thông báo về phim yêu thích sẽ xuất hiện ở đây.</p></div>
                    : <ul className="space-y-3">{inbox.data.content.map(notification => <li key={notification.id}>
                        <button onClick={() => void openNotification(notification)} disabled={busy} className={`w-full text-left flex gap-4 items-center p-4 rounded-2xl border transition-colors hover:bg-white/10 disabled:opacity-60 ${notification.isRead ? 'border-white/5 bg-white/[0.02]' : 'border-neon/20 bg-neon/5'}`}>
                            {notification.thumbUrl ? <img src={notification.thumbUrl} alt="" loading="lazy" className="w-16 h-20 rounded-lg object-cover shrink-0" /> : <Bell className="w-16 shrink-0 text-neon" />}
                            <span className="min-w-0 flex-1"><span className="block font-semibold">{notification.title}</span><span className="block text-sm text-text-secondary mt-1">{notification.content}</span><time dateTime={notification.createdAt} className="block text-xs text-text-secondary mt-2">{notificationDate(notification.createdAt)}</time></span>
                            {!notification.isRead && <span className="size-2 rounded-full bg-neon shrink-0" aria-label="Chưa đọc" />}
                        </button>
                    </li>)}</ul>}
        {totalPages > 1 && <nav aria-label="Phân trang thông báo" className="flex justify-center items-center gap-5 mt-8">
            <button disabled={page === 0 || inbox.isFetching} onClick={() => setPage(value => value - 1)} className="px-4 py-2 border border-white/10 rounded-lg disabled:opacity-40">Trước</button>
            <span>Trang {page + 1} / {totalPages}</span>
            <button disabled={page + 1 >= totalPages || inbox.isFetching} onClick={() => setPage(value => value + 1)} className="px-4 py-2 border border-white/10 rounded-lg disabled:opacity-40">Sau</button>
        </nav>}
    </>;
}

export function NotificationsPage() {
    const { user, token } = useAuthStore();
    return <div className="min-h-screen bg-obsidian text-white">
        <Navbar />
        <main className="max-w-4xl mx-auto px-4 pt-28 pb-16 min-h-[75vh]">
            {user && token ? <NotificationInbox key={user.id} userId={user.id} /> : <div className="text-center py-20"><Bell size={48} className="mx-auto mb-4 text-neon" /><h1 className="text-2xl font-bold">Thông báo của bạn</h1><p className="text-text-secondary mt-3 mb-6">Đăng nhập để xem thông báo về phim yêu thích.</p><Link to="/login" className="inline-block rounded-xl bg-neon text-obsidian font-bold px-6 py-3">Đăng nhập</Link></div>}
        </main>
        <Footer />
    </div>;
}
