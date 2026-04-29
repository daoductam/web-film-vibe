import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Link } from 'react-router-dom';
import { Play, TrendingUp, Calendar } from 'lucide-react';
import RatingStars from '../../components/movie/RatingStars';
import { AdvancedFilter } from '../../components/movie/AdvancedFilter';
import clsx from 'clsx';

export const SingleMoviePage = () => {
    // Filter States
    const [filters, setFilters] = useState({
        category: '',
        country: '',
        year: '',
        status: ''
    });

    const [sortBy, setSortBy] = useState('updatedAt,desc');
    const [page, setPage] = useState(1);

    // Fetch Movies (SINGLE type)
    const { data: moviesData, isLoading } = useQuery({
        queryKey: ['single-movies', filters, sortBy, page],
        queryFn: () => movieService.filterMovies({
            type: 'SINGLE',
            category: filters.category || undefined,
            country: filters.country || undefined,
            year: filters.year ? parseInt(filters.year) : undefined,
            status: filters.status || undefined,
            sort: sortBy,
            page,
            size: 24
        })
    });

    // Hero Data (Pick first movie as featured)
    const featuredMovie = moviesData?.content?.[0];

    const handleFilterChange = (key: string, value: string) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPage(1); // Reset to page 1
    };
    
    // Pagination
    const totalPages = moviesData?.page?.totalPages || moviesData?.totalPages || 0;

    return (
        <div className="bg-obsidian text-text-primary font-sans overflow-x-hidden antialiased selection:bg-neon selection:text-obsidian min-h-screen flex flex-col">
            <Navbar />

            <main className="flex-1">
                {/* Hero Section */}
                <section className="relative w-full h-[550px] md:h-[750px] flex items-center overflow-hidden">
                    <div className="absolute inset-0 z-0">
                        <div className="absolute inset-0 bg-gradient-to-t from-obsidian via-obsidian/40 to-transparent z-10"></div>
                        <div className="absolute inset-0 bg-gradient-to-r from-obsidian via-obsidian/70 to-transparent z-10"></div>
                        <div 
                            className="w-full h-full bg-cover bg-center transition-all duration-1000 scale-105" 
                            style={{ 
                                backgroundImage: `url("${featuredMovie?.posterUrl || featuredMovie?.thumbUrl || ''}")`,
                                backgroundPosition: 'center 20%'
                            }}
                        ></div>
                    </div>
                    
                    <div className="relative z-20 container mx-auto px-4 md:px-6 pt-32 md:pt-40 pb-12 max-w-[1600px]">
                        {featuredMovie ? (
                            <div className="max-w-3xl space-y-6 animate-fade-in-up">
                                <div className="flex flex-wrap gap-4 items-center">
                                    <div className="px-3 py-1.5 rounded-lg bg-neon text-obsidian text-[10px] md:text-xs font-black tracking-widest shadow-neon">
                                        PHIM LẺ NỔI BẬT
                                    </div>
                                    <div className="flex items-center gap-3 px-3 py-1 rounded-full bg-white/5 backdrop-blur-md border border-white/10">
                                        <RatingStars 
                                            movieSlug={featuredMovie.slug} 
                                            initialAverage={featuredMovie.averageRating} 
                                            initialCount={featuredMovie.ratingCount}
                                            size={18}
                                        />
                                    </div>
                                    <span className="text-gray-300 text-sm font-bold flex items-center gap-2">
                                        <span className="size-1 rounded-full bg-gray-500" />
                                        {featuredMovie.year}
                                        <span className="size-1 rounded-full bg-gray-500" />
                                        {featuredMovie.countries?.[0]?.name}
                                    </span>
                                    <span className="px-2 py-0.5 rounded border border-neon/30 text-neon text-[10px] font-black bg-neon/5">
                                        {featuredMovie.quality}
                                    </span>
                                </div>
                                <h1 className="font-serif text-4xl md:text-8xl font-black text-white leading-[0.9] drop-shadow-2xl">
                                    {featuredMovie.title}
                                </h1>
                                <p className="text-lg text-gray-300 leading-relaxed font-light max-w-2xl line-clamp-3 opacity-80">
                                    {featuredMovie.description?.replace(/<[^>]*>/g, '')}
                                </p>
                                <div className="flex gap-4 pt-8 pb-12">
                                    <Link to={`/movie/${featuredMovie.slug}`} className="flex items-center gap-3 px-10 py-4 rounded-2xl bg-neon text-obsidian font-black text-lg hover:bg-white transition-all shadow-neon group">
                                        <Play className="w-6 h-6 fill-current group-hover:scale-110 transition-transform" />
                                        XEM NGAY
                                    </Link>
                                </div>
                            </div>
                        ) : (
                            !isLoading && <div className="h-64 flex items-center text-white">Chưa có dữ liệu...</div>
                        )}
                    </div>
                </section>

                {/* Filter & Sort Bar */}
                <section className="sticky top-16 md:top-20 z-40 w-full border-y border-white/5 bg-obsidian/90 backdrop-blur-xl">
                    <div className="max-w-[1600px] mx-auto px-4 md:px-6 py-4">
                        <div className="flex flex-col md:flex-row gap-6 justify-between items-center">
                            <div className="flex flex-wrap gap-4 w-full md:w-auto">
                                <AdvancedFilter 
                                    onFilterChange={(newFilters) => {
                                        setFilters(prev => ({
                                            ...prev,
                                            category: newFilters.categorySlug || '',
                                            country: newFilters.countrySlug || '',
                                            year: newFilters.year || ''
                                        }));
                                        setPage(1);
                                    }}
                                />

                                <div className="relative">
                                    <select 
                                        className="appearance-none bg-white/5 border border-white/10 text-white rounded-xl px-4 py-2.5 pr-10 focus:outline-none focus:border-neon text-sm cursor-pointer hover:bg-white/10 transition-colors"
                                        value={filters.status}
                                        onChange={(e) => handleFilterChange('status', e.target.value)}
                                    >
                                        <option value="" className="bg-obsidian">Trạng thái</option>
                                        <option value="ONGOING" className="bg-obsidian">Đang chiếu</option>
                                        <option value="COMPLETED" className="bg-obsidian">Hoàn thành</option>
                                    </select>
                                </div>
                            </div>

                            <div className="flex items-center gap-4 bg-white/5 p-1 rounded-xl border border-white/10">
                                <button 
                                    onClick={() => { setSortBy('updatedAt,desc'); setPage(1); }}
                                    className={clsx(
                                        "flex items-center gap-2 px-4 py-2 rounded-lg text-xs md:text-sm font-bold transition-all",
                                        sortBy === 'updatedAt,desc' ? "bg-neon text-obsidian shadow-neon-sm" : "text-gray-400 hover:text-white"
                                    )}
                                >
                                    <Calendar size={14} /> Mới nhất
                                </button>
                                <button 
                                    onClick={() => { setSortBy('viewCount,desc'); setPage(1); }}
                                    className={clsx(
                                        "flex items-center gap-2 px-4 py-2 rounded-lg text-xs md:text-sm font-bold transition-all",
                                        sortBy === 'viewCount,desc' ? "bg-neon text-obsidian shadow-neon-sm" : "text-gray-400 hover:text-white"
                                    )}
                                >
                                    <TrendingUp size={14} /> Xem nhiều nhất
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Movie Grid */}
                <section className="max-w-[1600px] mx-auto px-4 md:px-6 py-16 md:py-24">
                    <h2 className="text-2xl md:text-5xl font-serif font-black text-white mb-12 italic tracking-tight">
                        {sortBy === 'viewCount,desc' ? 'PHIM LẺ XEM NHIỀU NHẤT' : 'PHIM LẺ MỚI CẬP NHẬT'}
                    </h2>
                    
                    {isLoading ? (
                        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
                            {[...Array(12)].map((_, i) => (
                                <div key={i} className="aspect-[2/3] rounded-2xl bg-white/5 animate-pulse" />
                            ))}
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-6 md:gap-8">
                            {moviesData?.content?.map((movie: any) => (
                                <Link 
                                    key={movie.id} 
                                    to={`/movie/${movie.slug}`} 
                                    className="group relative cursor-pointer interactive-card rounded-2xl glass-card border border-white/5 bg-surface overflow-hidden flex flex-col h-full"
                                >
                                    <div className="aspect-[2/3] w-full relative overflow-hidden">
                                        <img 
                                            alt={movie.title} 
                                            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" 
                                            src={movie.posterUrl || movie.thumbUrl}
                                            loading="lazy"
                                        />
                                        <div className="absolute top-2 right-2 z-20 px-2 py-0.5 bg-neon text-obsidian text-[10px] font-black rounded shadow-neon">
                                            {movie.quality}
                                        </div>
                                        <div className="absolute inset-0 bg-gradient-to-t from-black via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                                            <div className="size-12 rounded-full bg-neon text-obsidian flex items-center justify-center shadow-neon">
                                                <Play size={24} className="fill-current ml-1" />
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div className="p-4 flex-1 flex flex-col justify-between">
                                        <div>
                                            <h3 className="font-serif text-sm md:text-base text-white font-bold line-clamp-1 group-hover:text-neon transition-colors">{movie.title}</h3>
                                            <div className="flex items-center justify-between mt-2">
                                                <span className="text-[10px] md:text-xs text-gray-500 font-medium">
                                                    {movie.year} • {movie.countries?.[0]?.name || 'N/A'}
                                                </span>
                                                <span className="text-[10px] md:text-xs text-neon font-black flex items-center gap-1">
                                                    <TrendingUp size={10} />
                                                    {movie.viewCount > 1000 ? (movie.viewCount/1000).toFixed(1) + 'K' : movie.viewCount}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    )}

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex justify-center items-center gap-3 mt-20">
                            <button 
                                onClick={() => setPage(p => Math.max(1, p - 1))}
                                disabled={page === 1}
                                className="size-12 flex items-center justify-center rounded-xl border border-white/10 hover:bg-neon hover:text-obsidian hover:border-neon text-white transition-all disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-white"
                            >
                                <Play className="w-4 h-4 rotate-180 fill-current" />
                            </button>
                            
                            {/* Smart Pagination Logic */}
                            {(() => {
                                let pagesTarget = [];
                                if (totalPages <= 7) {
                                    pagesTarget = [...Array(totalPages)].map((_, i) => i + 1);
                                } else {
                                    if (page <= 4) {
                                        pagesTarget = [1, 2, 3, 4, 5, '...', totalPages];
                                    } else if (page >= totalPages - 3) {
                                        pagesTarget = [1, '...', totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages];
                                    } else {
                                        pagesTarget = [1, '...', page - 1, page, page + 1, '...', totalPages];
                                    }
                                }

                                return pagesTarget.map((p, index) => (
                                    p === '...' ? (
                                        <span key={`dots-${index}`} className="text-gray-500 px-2">...</span>
                                    ) : (
                                        <button 
                                            key={p}
                                            onClick={() => typeof p === 'number' && setPage(p)}
                                            className={clsx(
                                                "size-12 flex items-center justify-center rounded-xl transition-all font-bold",
                                                page === p ? "bg-neon text-obsidian shadow-neon" : "border border-white/10 text-white hover:bg-white/5"
                                            )}
                                        >
                                            {p}
                                        </button>
                                    )
                                ));
                            })()}

                            <button 
                                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                                disabled={page === totalPages}
                                className="size-12 flex items-center justify-center rounded-xl border border-white/10 hover:bg-neon hover:text-obsidian hover:border-neon text-white transition-all disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-white"
                            >
                                <Play className="w-4 h-4 fill-current" />
                            </button>
                        </div>
                    )}
                </section>
            </main>

            <Footer />
        </div>
    );
};
