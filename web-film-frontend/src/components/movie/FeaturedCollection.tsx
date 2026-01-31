import { ArrowRight, BadgeCheck } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Link } from 'react-router-dom';

export const FeaturedCollection = () => {
    // Fetch popular movies for featured section
    const { data: movies } = useQuery({
        queryKey: ['movies', 'featured'],
        queryFn: () => movieService.getPopularMovies(0, 4), // Get top 4 popular movies
    });

    if (!movies?.content || movies.content.length === 0) return null;

    // Safe destructuring with defaults/checks
    const mainMovie = movies.content[0];
    const sub1 = movies.content[1];
    const sub2 = movies.content[2];

    if (!mainMovie) return null; // Minimal requirement: 1 movie

    return (
        <section className="max-w-[1600px] mx-auto px-6">
            <h2 className="text-3xl font-serif font-bold text-white mb-8 flex items-center gap-3">
                <span className="block w-1 h-8 bg-neon rounded-full shadow-neon" />
                Bộ sưu tập đặc sắc
            </h2>
            
            <div className="grid grid-cols-1 md:grid-cols-4 md:grid-rows-2 gap-6 h-auto md:h-[700px]">
                {/* Main Featured Item */}
                <Link to={`/movie/${mainMovie.slug}`} className="md:col-span-2 md:row-span-2 relative group rounded-3xl overflow-hidden glass-card border-none block cursor-pointer">
                    <div 
                        className="absolute inset-0 bg-cover bg-center transition-transform duration-1000 group-hover:scale-105" 
                        style={{ backgroundImage: `url("${mainMovie.posterUrl || mainMovie.thumbUrl}")`, backgroundPosition: 'top center' }}
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-obsidian via-obsidian/40 to-transparent" />
                    
                    <div className="absolute bottom-0 left-0 p-8 w-full">
                        <span className="px-3 py-1 bg-neon text-obsidian text-xs font-bold rounded mb-3 inline-block shadow-neon-sm">#1 THỊNH HÀNH</span>
                        <h3 className="text-4xl md:text-5xl font-serif font-bold text-white mb-2 leading-tight">{mainMovie.title}</h3>
                        <p className="text-gray-300 line-clamp-2 max-w-md mb-4 text-sm" dangerouslySetInnerHTML={{ __html: mainMovie.description || '' }}></p>
                        <button className="flex items-center gap-2 text-neon font-bold tracking-wider text-sm hover:text-white transition-colors">
                            XEM CHI TIẾT <ArrowRight className="w-5 h-5" />
                        </button>
                    </div>
                </Link>

                {/* Sub Item 1 */}
                {sub1 && (
                <Link to={`/movie/${sub1.slug}`} className="md:col-span-1 md:row-span-1 relative group rounded-3xl overflow-hidden glass-card border-none block cursor-pointer">
                    <div 
                        className="absolute inset-0 bg-cover bg-center transition-transform duration-700 group-hover:scale-110" 
                        style={{ backgroundImage: `url("${sub1.posterUrl || sub1.thumbUrl}")` }}
                    />
                    <div className="absolute inset-0 bg-black/40 group-hover:bg-black/20 transition-colors" />
                    <div className="absolute bottom-4 left-4">
                        <h4 className="font-serif text-xl font-bold text-white group-hover:text-neon transition-colors line-clamp-1">{sub1.title}</h4>
                    </div>
                </Link>
                )}

                {/* Sub Item 2 */}
                {sub2 && (
                <Link to={`/movie/${sub2.slug}`} className="md:col-span-1 md:row-span-1 relative group rounded-3xl overflow-hidden glass-card border-none block cursor-pointer">
                    <div 
                        className="absolute inset-0 bg-cover bg-center transition-transform duration-700 group-hover:scale-110" 
                        style={{ backgroundImage: `url("${sub2.posterUrl || sub2.thumbUrl}")` }}
                    />
                    <div className="absolute inset-0 bg-black/40 group-hover:bg-black/20 transition-colors" />
                    <div className="absolute bottom-4 left-4">
                        <h4 className="font-serif text-xl font-bold text-white group-hover:text-neon transition-colors line-clamp-1">{sub2.title}</h4>
                    </div>
                </Link>
                )}

                {/* Special Collection Badge */}
                <div className="md:col-span-2 md:row-span-1 relative group rounded-3xl overflow-hidden glass-card border-none flex items-center justify-center">
                    <div 
                        className="absolute inset-0 bg-cover bg-center transition-transform duration-700 group-hover:scale-105 opacity-60 group-hover:opacity-40" 
                        style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuAYoUHiwv0BOOrNPgHo9Ro8hzEFu4jlf2xPcSwqtE_8kXTSXUsZc42podhTQc9E62YUnW6lDznfGneYJro_DiqE30n0JRsgdQwvkH3M85zlIC_tWz53CB_fC0HfSiwvjXQONsHgSYYI2NhTqUUIeZrjEz7fIt0YBfpkNjkkJ5pUxPUbZMMelyFOgN4QypZLF0vtt8Rxxe1wdE4JPKcap5aMTMwMAqoxJixT0vwQBz8sG3Y0HM89oaMFwqlRnQUJLdLnVP_tFWEVHlQ")' }}
                    />
                    <div className="absolute inset-0 bg-gradient-to-r from-obsidian to-transparent" />
                    <div className="relative z-10 p-8 w-full flex justify-between items-center">
                        <div>
                            <h4 className="font-serif text-2xl font-bold text-white mb-1">Lựa chọn của giới phê bình</h4>
                            <p className="text-text-secondary text-sm">Những tuyệt tác điện ảnh được chọn lọc</p>
                        </div>
                        <BadgeCheck className="w-12 h-12 text-neon animate-float opacity-80" />
                    </div>
                </div>
            </div>
        </section>
    );
};
