import { Play, Heart, ThumbsUp } from 'lucide-react';
import type { Movie } from '../../types';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { personalizationService } from '../../services/personalization.service';
import { useToast } from '../common/Toast';
import { useState } from 'react';

interface MovieCardProps {
    movie: Movie;
}

export const MovieCard = ({ movie }: MovieCardProps) => {
    const { token } = useAuthStore();
    const navigate = useNavigate();
    const { showToast } = useToast();
    const [isSaving, setIsSaving] = useState(false);

    const handleToggleFavorite = async (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();

        if (!token) {
            showToast('Vui lòng đăng nhập để lưu phim yêu thích!', 'info');
            navigate('/login');
            return;
        }

        setIsSaving(true);
        try {
            await personalizationService.addFavorite({
                movieSlug: movie.slug,
                title: movie.title,
                thumbUrl: movie.thumbUrl || movie.posterUrl,
                quality: movie.quality,
                year: movie.year,
                createdAt: new Date().toISOString()
            });
            showToast(`Đã thêm "${movie.title}" vào yêu thích!`, 'success');
        } catch (error) {
            console.error('Failed to add favorite:', error);
            showToast('Không thể thêm vào yêu thích', 'error');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <Link to={`/movie/${movie.slug}`} className="block w-full min-w-[140px] md:min-w-[180px] group relative cursor-pointer interactive-card rounded-2xl glass-card border border-white/5 bg-surface overflow-hidden">

            <div className="aspect-[2/3] w-full relative">
                <img 
                    src={movie.posterUrl || movie.thumbUrl} 
                    alt={movie.title} 
                    className="w-full h-full object-cover poster-image transition-all duration-300 rounded-t-2xl" 
                />
                
                <div className="video-preview absolute inset-0 bg-black flex items-center justify-center overflow-hidden">
                    <img 
                        src={movie.posterUrl || movie.thumbUrl} 
                        alt="Preview" 
                        className="w-full h-full object-cover opacity-60" 
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-black via-transparent to-transparent" />
                    <span className="material-symbols-outlined text-4xl text-neon animate-pulse z-10">
                        <Play className="fill-current w-12 h-12" />
                    </span>
                </div>
                
                {movie.quality && (
                    <div className="absolute top-3 right-3 z-20 px-2 py-1 bg-black/60 backdrop-blur rounded border border-neon/30 text-neon text-[10px] font-bold shadow-neon-sm">
                        {movie.quality}
                    </div>
                )}
            </div>

            {/* Base Info */}
            <div className="p-3 md:p-4 card-info-base">
                <h3 className="font-serif text-sm md:text-lg text-white font-black truncate group-hover:text-neon transition-colors">{movie.title}</h3>
                <p className="text-[10px] md:text-sm text-text-secondary font-bold">{movie.year} • {movie.type === 'SERIES' ? 'Bộ' : 'Lẻ'}</p>
            </div>

            {/* Hover Content */}
            <div className="card-content absolute bottom-0 left-0 w-full p-4 bg-gradient-to-t from-obsidian via-obsidian to-transparent z-20">
                <h3 className="font-serif text-lg text-white font-bold mb-1">{movie.title}</h3>
                <div className="flex items-center gap-2 mb-3">
                    <span className="text-neon text-xs font-bold">{(movie.averageRating || 0).toFixed(1)}/10</span>
                    <span className="px-1.5 py-0.5 border border-white/20 rounded text-[10px] text-white">{movie.status}</span>
                    <span className="text-xs text-gray-400">{movie.duration}</span>
                </div>
                <div className="flex gap-2 mb-3">
                    <span className="text-[10px] text-gray-300 line-clamp-1">{movie.categories?.map(c => c.name).join(', ') || 'Phim mới'}</span>
                </div>
                
                <div className="flex gap-2">
                    <button className="flex-1 flex items-center justify-center gap-1 bg-white text-black py-1.5 rounded-lg text-xs font-bold hover:bg-neon transition-colors">
                        <Play className="w-3 h-3 fill-current" /> Xem
                    </button>
                    <button 
                        onClick={handleToggleFavorite}
                        disabled={isSaving}
                        className={`size-8 flex items-center justify-center rounded-full border border-gray-500 hover:border-neon hover:text-neon transition-colors ${isSaving ? 'opacity-50 cursor-not-allowed' : ''}`}
                    >
                        <Heart className="w-4 h-4" />
                    </button>
                </div>
            </div>
        </Link>
    );
};
