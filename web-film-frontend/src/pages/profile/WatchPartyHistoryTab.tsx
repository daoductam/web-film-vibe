import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { watchPartyService, type WatchPartyHistory } from '../../services/watchParty.service';
import type { PageResponse } from '../../types';
import { useAuthStore } from '../../store/authStore';

export const WatchPartyHistoryTab = () => {
    const userId = useAuthStore(state => state.user?.id);
    const [page, setPage] = useState(0);
    const { data, isLoading, isError, refetch } = useQuery<PageResponse<WatchPartyHistory>>({
        queryKey: ['watch-party-history', userId, page],
        queryFn: () => watchPartyService.getWatchPartyHistory(page, 10),
    });
    if (isLoading) return <p role="status" className="text-gray-400">Đang tải lịch sử xem chung...</p>;
    if (isError) return <div role="alert">Không thể tải lịch sử. <button onClick={() => refetch()} className="text-neon">Thử lại</button></div>;
    const entries = data?.content ?? [];
    const totalPages = data?.page?.totalPages ?? data?.totalPages ?? 0;
    return (
        <div>
            <h3 className="text-2xl font-bold mb-6 font-serif">Lịch sử xem chung</h3>
            {entries.length === 0 ? <p className="text-gray-400 py-10 text-center">Bạn chưa tham gia phòng xem chung nào.</p> : (
                <div className="space-y-4">
                    {entries.map(item => (
                        <article key={item.id} className="rounded-xl p-4 bg-white/5 border border-white/10">
                            <h4 className="font-bold">{item.roomName}</h4>
                            <Link to={`/movie/${encodeURIComponent(item.movieSlug)}`} className="text-neon hover:underline">{item.movieTitle}</Link>
                            <p className="text-sm text-gray-400 mt-2">Đã xem {Math.floor(Math.max(0, item.watchDurationSeconds) / 60)} phút · {new Date(item.joinedAt).toLocaleString('vi-VN')}</p>
                        </article>
                    ))}
                </div>
            )}
            {totalPages > 1 && <nav aria-label="Phân trang lịch sử xem chung" className="flex justify-between items-center mt-6 gap-3">
                <button disabled={page === 0} onClick={() => setPage(page - 1)} className="px-4 py-2 bg-white/10 rounded-lg disabled:opacity-40">Trang trước</button>
                <span className="text-sm text-gray-400">{page + 1} / {totalPages}</span>
                <button disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)} className="px-4 py-2 bg-white/10 rounded-lg disabled:opacity-40">Trang sau</button>
            </nav>}
        </div>
    );
};
