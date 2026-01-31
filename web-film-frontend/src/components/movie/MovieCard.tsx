import { Play, Plus, ThumbsUp } from 'lucide-react';
import type { Movie } from '../../types';
import { Link } from 'react-router-dom';

interface MovieCardProps {
    movie: Movie;
}

export const MovieCard = ({ movie }: MovieCardProps) => {
    return (
        <Link to={`/movie/${movie.slug}`} className="block snap-start shrink-0 w-[240px] group relative cursor-pointer interactive-card rounded-2xl glass-card border border-white/5 bg-surface overflow-hidden">

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

            {/* Base Info (Visible by default) */}
            <div className="p-4 card-info-base">
                <h3 className="font-serif text-lg text-white font-bold truncate">{movie.title}</h3>
                <p className="text-sm text-text-secondary">{movie.year} • {movie.type}</p>
            </div>

            {/* Hover Content */}
            <div className="card-content absolute bottom-0 left-0 w-full p-4 bg-gradient-to-t from-obsidian via-obsidian to-transparent z-20">
                <h3 className="font-serif text-lg text-white font-bold mb-1">{movie.title}</h3>
                <div className="flex items-center gap-2 mb-3">
                    <span className="text-neon text-xs font-bold">{movie.rating || 0}/10</span>
                    <span className="px-1.5 py-0.5 border border-white/20 rounded text-[10px] text-white">{movie.status}</span>
                    <span className="text-xs text-gray-400">{movie.duration}</span>
                </div>
                <div className="flex gap-2 mb-3">
                     {/* Safe check for categories as it might be null/empty in some API responses */}
                    <span className="text-[10px] text-gray-300 line-clamp-1">{movie.categories?.map(c => c.name).join(', ') || 'Phim mới'}</span>
                </div>
                
                <div className="flex gap-2">
                    <button className="flex-1 flex items-center justify-center gap-1 bg-white text-black py-1.5 rounded-lg text-xs font-bold hover:bg-neon transition-colors">
                        <Play className="w-3 h-3 fill-current" /> Xem
                    </button>
                    <button className="size-8 flex items-center justify-center rounded-full border border-gray-500 hover:border-white text-white transition-colors">
                        <Plus className="w-4 h-4" />
                    </button>
                    <button className="size-8 flex items-center justify-center rounded-full border border-gray-500 hover:border-white text-white transition-colors">
                        <ThumbsUp className="w-4 h-4" />
                    </button>
                </div>
            </div>
        </Link>
    );
};
