import { useQuery } from '@tanstack/react-query';
import { personalizationService } from '../../services/personalization.service';
import { Link } from 'react-router-dom';
import { Play } from 'lucide-react';

export const FavoritesTab = () => {
    const { data: favorites, isLoading } = useQuery({
        queryKey: ['favorites'],
        queryFn: () => personalizationService.getFavorites()
    });

    if (isLoading) {
        return <div className="animate-pulse flex space-x-4">Đang tải...</div>;
    }

    if (!favorites || favorites.length === 0) {
        return <div className="text-gray-400 text-center pt-10">Bạn chưa có phim yêu thích nào.</div>;
    }

    return (
        <div>
            <h3 className="text-2xl font-bold text-white mb-6 font-serif">Phim yêu thích</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {favorites.map((fav) => (
                    <Link key={fav.movieSlug} to={`/movie/${fav.movieSlug}`} className="block group relative overflow-hidden rounded-xl bg-surface border border-white/5">
                        <div className="aspect-[2/3] w-full relative">
                            <img src={fav.thumbUrl} alt={fav.title} className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105" />
                            <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                                <Play className="w-12 h-12 text-neon" />
                            </div>
                        </div>
                        <div className="p-3">
                            <h4 className="text-sm font-bold text-white truncate">{fav.title}</h4>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
};
