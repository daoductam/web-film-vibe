import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { personalizationService } from '../../services/personalization.service';
import { Link } from 'react-router-dom';
import { Play, Trash2 } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';

export const FavoritesTab = () => {
    const userId = useAuthStore(state => state.user?.id);
    const queryClient = useQueryClient();
    const { data: favorites, isLoading, isError, refetch } = useQuery({
        queryKey: ['favorites', userId],
        queryFn: () => personalizationService.getFavorites(),
    });
    const remove = useMutation({
        mutationFn: (slug: string) => personalizationService.removeFavorite(slug),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['favorites'] }),
    });
    if (isLoading) return <p role="status" className="text-gray-400">Đang tải phim yêu thích...</p>;
    if (isError) return <div role="alert">Không thể tải phim yêu thích. <button onClick={() => refetch()} className="text-neon">Thử lại</button></div>;

    return (
        <div>
            <h3 className="text-2xl font-bold text-white mb-6 font-serif">Phim yêu thích</h3>
            {remove.isError && <p role="alert" className="text-red-400 mb-4">Không thể bỏ yêu thích. Vui lòng thử lại.</p>}
            {!favorites?.length ? <p className="text-gray-400 text-center pt-10">Bạn chưa có phim yêu thích nào.</p> : (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                    {favorites.map(fav => (
                        <article key={fav.movieSlug} className="overflow-hidden rounded-xl bg-surface border border-white/5">
                            <Link to={`/movie/${encodeURIComponent(fav.movieSlug)}`} className="block group">
                                <div className="aspect-[2/3] relative">
                                    <img src={fav.thumbUrl} alt={fav.title} loading="lazy" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                                    <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center"><Play className="w-12 h-12 text-neon" /></div>
                                </div>
                                <h4 className="p-3 text-sm font-bold text-white truncate">{fav.title}</h4>
                            </Link>
                            <button aria-label={`Bỏ yêu thích ${fav.title}`} onClick={() => remove.mutate(fav.movieSlug)} disabled={remove.isPending}
                                className="flex items-center gap-2 px-3 pb-3 text-xs text-gray-400 hover:text-red-400 disabled:opacity-40"><Trash2 size={14} /> Bỏ yêu thích</button>
                        </article>
                    ))}
                </div>
            )}
        </div>
    );
};
