import { Star, Play, Plus } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Link } from 'react-router-dom';

export const HeroSection = () => {
    // Prefer popular movies for Hero as they likely have better visuals, fallback to latest
    const { data: movies } = useQuery({
        queryKey: ['movies', 'hero'],
        queryFn: () => movieService.getPopularMovies(0, 5), 
    });

    // Use first movie if no image found, but try to find one with image first
    const heroMovie = movies?.content?.find(m => m.posterUrl || m.thumbUrl) || movies?.content?.[0];

    if (!heroMovie) return null;
    
    const bgImage = heroMovie.posterUrl || heroMovie.thumbUrl || 'https://via.placeholder.com/1920x1080/000000/FFFFFF?text=No+Image';

    return (
        <section className="relative w-full min-h-[500px] md:h-screen md:min-h-[800px] flex items-center overflow-hidden">
            <div className="absolute inset-0 z-0">
                <div className="absolute inset-0 bg-gradient-to-t from-obsidian via-obsidian/40 to-transparent z-10" />
                <div className="absolute inset-0 bg-gradient-to-r from-obsidian via-obsidian/70 to-transparent z-10" />
                <div 
                    className="w-full h-full bg-cover bg-center transform scale-105 animate-[float_20s_ease-in-out_infinite] origin-center" 
                    style={{ backgroundImage: `url("${bgImage}")` }}
                />
            </div>
            
            <div className="relative z-20 container mx-auto px-4 md:px-6 pt-24 md:pt-20 max-w-[1600px]">
                <div className="max-w-3xl space-y-6 md:space-y-8">
                    <div className="flex flex-wrap gap-2 md:gap-3 items-center opacity-0 animate-[fadeIn_0.8s_ease-out_forwards]">
                        <div className="px-2 md:px-3 py-1 rounded bg-black/40 backdrop-blur border border-neon/50 text-neon text-[10px] md:text-xs font-bold tracking-wider shadow-neon-sm">
                            ĐỘC QUYỀN TRÊN CINESTREAM
                        </div>
                        <span className="flex items-center gap-1 text-yellow-400 text-xs md:text-sm font-bold">
                            <Star className="w-3 h-3 md:w-4 md:h-4 fill-current" /> {heroMovie.rating || 'N/A'}
                        </span>
                        <span className="text-gray-300 text-xs md:text-sm font-medium">{heroMovie.year} • {heroMovie.quality}</span>
                        <span className="hidden xs:inline px-2 py-0.5 rounded border border-gray-600 text-gray-300 text-[10px] md:text-xs font-bold">4K IMAX</span>
                    </div>

                    <h1 className="font-serif text-3xl sm:text-4xl md:text-6xl lg:text-8xl font-black text-transparent bg-clip-text bg-gradient-to-br from-white via-white to-gray-400 leading-tight md:leading-none drop-shadow-2xl opacity-0 animate-[fadeInUp_1s_0.2s_ease-out_forwards] translate-y-4 line-clamp-2">
                        {heroMovie.title}
                    </h1>
                    
                    <div 
                        className="text-sm md:text-xl text-gray-300 leading-relaxed font-light max-w-2xl opacity-0 animate-[fadeInUp_1s_0.4s_ease-out_forwards] translate-y-4 line-clamp-2 md:line-clamp-3"
                        dangerouslySetInnerHTML={{ __html: heroMovie.description || '' }}
                    />
                    
                    <div className="flex flex-wrap gap-3 md:gap-4 pt-4 opacity-0 animate-[fadeInUp_1s_0.6s_ease-out_forwards] translate-y-4">
                        <Link to={`/movie/${heroMovie.slug}`} className="flex-1 sm:flex-none flex items-center justify-center gap-2 md:gap-3 px-6 md:px-8 py-3 md:py-4 rounded-xl bg-neon text-obsidian font-bold text-base md:text-lg hover:bg-white transition-all shadow-neon hover:shadow-[0_0_30px_rgba(255,255,255,0.5)] group">
                            <Play className="w-4 h-4 md:w-5 md:h-5 fill-current group-hover:scale-110 transition-transform" />
                            Xem ngay
                        </Link>
                        <button className="flex-1 sm:flex-none flex items-center justify-center gap-2 md:gap-3 px-6 md:px-8 py-3 md:py-4 rounded-xl glass-card text-white font-bold text-base md:text-lg hover:bg-white/10 transition-all">
                            <Plus className="w-4 h-4 md:w-5 md:h-5" />
                            Danh sách
                        </button>
                    </div>
                </div>
            </div>
            
            <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-obsidian to-transparent z-20" />
        </section>
    );
};
