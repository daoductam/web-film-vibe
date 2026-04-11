import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Link } from 'react-router-dom';
import { Play, TrendingUp, Calendar, Filter } from 'lucide-react';
import clsx from 'clsx';

export const PopularPage = () => {
    // Filter States
    const [filters, setFilters] = useState({
        category: '',
        country: '',
        year: '',
        status: ''
    });

    // Sort State (Default to Views for "Popular")
    const [sortBy, setSortBy] = useState('viewCount,desc'); 

    // Fetch Movies - Limit to 50 for a "Top List" feel
    const { data: moviesData, isLoading } = useQuery({
        queryKey: ['popular-movies', filters, sortBy],
        queryFn: () => movieService.filterMovies({
            category: filters.category || undefined,
            country: filters.country || undefined,
            year: filters.year ? parseInt(filters.year) : undefined,
            status: filters.status || undefined,
            page: 0,
            size: 50, 
            sort: sortBy 
        })
    });

    const movies = moviesData?.content || [];
    const featuredMovie = movies[0];

    const handleFilterChange = (key: string, value: string) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    return (
        <div className="bg-obsidian text-text-primary font-sans overflow-x-hidden antialiased selection:bg-neon selection:text-obsidian min-h-screen flex flex-col">
            <Navbar />

            <main className="flex-1">
                {/* Hero Section - Optimized for Mobile */}
                <section className="relative w-full h-[450px] md:h-[600px] flex items-center overflow-hidden">
                    <div className="absolute inset-0 z-0">
                        <div className="absolute inset-0 bg-gradient-to-t from-obsidian via-obsidian/40 to-transparent z-10" />
                        <div className="absolute inset-0 bg-gradient-to-r from-obsidian via-obsidian/70 to-transparent z-10" />
                        <div 
                            className="w-full h-full bg-cover bg-center transition-all duration-1000 scale-105" 
                            style={{ 
                                backgroundImage: `url("${featuredMovie?.posterUrl || featuredMovie?.thumbUrl || ''}")`,
                                backgroundPosition: 'center 20%'
                            }}
                        />
                    </div>
                    
                    <div className="relative z-20 container mx-auto px-4 md:px-6 pt-16 md:pt-20 max-w-[1600px]">
                        {featuredMovie ? (
                            <div className="max-w-3xl space-y-4 md:space-y-6 animate-fade-in-up">
                                <div className="flex flex-wrap gap-2 md:gap-3 items-center">
                                    <div className="px-2 md:px-3 py-1 rounded bg-neon text-obsidian text-[10px] md:text-xs font-bold tracking-wider shadow-neon">
                                        TOP 1 XU HƯỚNG
                                    </div>
                                    <span className="flex items-center gap-1 text-white text-xs md:text-sm font-bold bg-white/10 backdrop-blur-md px-2 py-1 rounded">
                                        <TrendingUp size={14} className="text-neon" /> {(featuredMovie.viewCount || 0).toLocaleString()} lượt xem
                                    </span>
                                </div>
                                <h1 className="font-serif text-3xl sm:text-4xl md:text-7xl font-black text-white leading-tight drop-shadow-2xl">
                                    {featuredMovie.title}
                                </h1>
                                <p className="hidden md:block text-lg text-gray-300 leading-relaxed font-light max-w-2xl line-clamp-2">
                                    {featuredMovie.description?.replace(/<[^>]*>/g, '')}
                                </p>
                                <div className="flex gap-4 pt-2">
                                    <Link to={`/movie/${featuredMovie.slug}`} className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-8 py-3 rounded-xl bg-neon text-obsidian font-bold text-base md:text-lg hover:bg-white transition-all shadow-neon group">
                                        <Play className="w-5 h-5 fill-current" /> Xem ngay
                                    </Link>
                                </div>
                            </div>
                        ) : (
                            !isLoading && <div className="h-64 flex items-center text-white">Chưa có dữ liệu...</div>
                        )}
                    </div>
                </section>

                {/* Filter & Sort Bar - Responsive Design */}
                <section className="sticky top-16 z-40 w-full border-y border-white/5 bg-obsidian/90 backdrop-blur-xl">
                    <div className="max-w-[1600px] mx-auto px-4 md:px-6 py-2 md:py-4">
                        <div className="flex flex-col lg:flex-row gap-4 justify-between items-center">
                            
                            {/* Horizontal scrolling filters on mobile */}
                            <div className="flex items-center gap-3 w-full overflow-x-auto hide-scrollbar py-1 lg:py-0">
                                <div className="flex items-center gap-2 text-neon shrink-0 lg:mr-2">
                                    <Filter size={16} />
                                    <span className="text-xs font-bold uppercase tracking-tighter hidden sm:inline">Lọc:</span>
                                </div>
                                
                                <select 
                                    className="bg-white/5 border border-white/10 text-white rounded-lg px-3 py-1.5 text-xs md:text-sm focus:outline-none focus:border-neon min-w-[110px] cursor-pointer"
                                    value={filters.category}
                                    onChange={(e) => handleFilterChange('category', e.target.value)}
                                >
                                    <option value="" className="bg-obsidian">Thể loại</option>
                                    <option value="hanh-dong" className="bg-obsidian">Hành động</option>
                                    <option value="tinh-cam" className="bg-obsidian">Tình cảm</option>
                                    <option value="hoat-hinh" className="bg-obsidian">Hoạt hình</option>
                                    <option value="kinh-di" className="bg-obsidian">Kinh dị</option>
                                </select>

                                <select 
                                    className="bg-white/5 border border-white/10 text-white rounded-lg px-3 py-1.5 text-xs md:text-sm focus:outline-none focus:border-neon min-w-[110px] cursor-pointer"
                                    value={filters.country}
                                    onChange={(e) => handleFilterChange('country', e.target.value)}
                                >
                                    <option value="" className="bg-obsidian">Quốc gia</option>
                                    <option value="trung-quoc" className="bg-obsidian">Trung Quốc</option>
                                    <option value="han-quoc" className="bg-obsidian">Hàn Quốc</option>
                                    <option value="nhat-ban" className="bg-obsidian">Nhật Bản</option>
                                    <option value="au-my" className="bg-obsidian">Âu Mỹ</option>
                                </select>

                                <select 
                                    className="bg-white/5 border border-white/10 text-white rounded-lg px-3 py-1.5 text-xs md:text-sm focus:outline-none focus:border-neon min-w-[110px] cursor-pointer"
                                    value={filters.year}
                                    onChange={(e) => handleFilterChange('year', e.target.value)}
                                >
                                    <option value="" className="bg-obsidian">Năm</option>
                                    {[2025, 2024, 2023, 2022, 2021].map(y => (
                                        <option key={y} value={y} className="bg-obsidian">{y}</option>
                                    ))}
                                </select>
                            </div>

                            {/* Sort Toggles */}
                            <div className="flex items-center justify-between w-full lg:w-auto gap-4 border-t lg:border-none border-white/5 pt-2 lg:pt-0">
                                <div className="flex bg-white/5 p-1 rounded-xl border border-white/10">
                                    <button 
                                        onClick={() => setSortBy('viewCount,desc')}
                                        className={clsx(
                                            "flex items-center gap-2 px-4 py-1.5 rounded-lg text-xs md:text-sm font-bold transition-all",
                                            sortBy === 'viewCount,desc' ? "bg-neon text-obsidian shadow-neon-sm" : "text-gray-400 hover:text-white"
                                        )}
                                    >
                                        <TrendingUp size={14} /> Phổ biến
                                    </button>
                                    <button 
                                        onClick={() => setSortBy('updatedAt,desc')}
                                        className={clsx(
                                            "flex items-center gap-2 px-4 py-1.5 rounded-lg text-xs md:text-sm font-bold transition-all",
                                            sortBy === 'updatedAt,desc' ? "bg-neon text-obsidian shadow-neon-sm" : "text-gray-400 hover:text-white"
                                        )}
                                    >
                                        <Calendar size={14} /> Mới nhất
                                    </button>
                                </div>
                                <div className="hidden sm:flex items-center gap-2">
                                    <div className="size-2 rounded-full bg-neon animate-pulse" />
                                    <span className="text-[10px] text-neon font-black uppercase tracking-widest italic">Top 50</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Movie Grid - Responsive Columns */}
                <section className="max-w-[1600px] mx-auto px-4 md:px-6 py-8 md:py-16">
                    <div className="flex items-baseline gap-4 mb-8">
                        <h2 className="text-2xl md:text-3xl font-serif font-black text-white italic">
                            {sortBy === 'viewCount,desc' ? 'BẢNG XẾP HẠNG THỊNH HÀNH' : 'NỘI DUNG MỚI CẬP NHẬT'}
                        </h2>
                        <span className="text-neon/40 font-black text-xl italic md:text-2xl">#Top50</span>
                    </div>
                    
                    {isLoading ? (
                        <div className="flex justify-center py-20">
                            <div className="w-10 h-10 border-4 border-neon border-t-transparent rounded-full animate-spin"></div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 md:gap-8">
                            {movies.map((movie: any, index: number) => (
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
                                        {/* Rank Overlay */}
                                        <div className="absolute top-0 left-0 z-20 w-10 h-10 bg-black/80 backdrop-blur-md border-b border-r border-neon/30 flex items-center justify-center">
                                            <span className="text-xl font-serif font-black italic text-white group-hover:text-neon transition-colors">
                                                {index + 1}
                                            </span>
                                        </div>
                                        
                                        <div className="absolute top-2 right-2 z-20 px-2 py-0.5 bg-neon text-obsidian text-[10px] font-black rounded shadow-neon">
                                            {movie.quality}
                                        </div>

                                        <div className="absolute inset-0 bg-gradient-to-t from-black via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center">
                                            <div className="size-12 rounded-full bg-neon text-obsidian flex items-center justify-center shadow-neon">
                                                <Play size={24} className="fill-current ml-1" />
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div className="p-3 md:p-4 flex-1 flex flex-col justify-between">
                                        <div>
                                            <h3 className="font-serif text-sm md:text-base text-white font-bold line-clamp-1 group-hover:text-neon transition-colors">{movie.title}</h3>
                                            <div className="flex items-center justify-between mt-2">
                                                <span className="text-[10px] md:text-xs text-gray-500 font-medium">
                                                    {movie.year} • {movie.type === 'SERIES' ? (movie.totalEpisodes ? `${movie.totalEpisodes} Tập` : 'Nhiều tập') : 'Phim lẻ'}
                                                </span>
                                                <span className="text-[10px] md:text-xs text-neon font-black flex items-center gap-1">
                                                    <Play size={10} className="fill-current" />
                                                    {movie.viewCount > 1000 ? (movie.viewCount/1000).toFixed(1) + 'K' : movie.viewCount}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    )}

                    {!isLoading && movies.length === 0 && (
                        <div className="text-center py-32 space-y-4">
                            <div className="text-gray-600 text-6xl">∅</div>
                            <p className="text-gray-500 font-medium">Không tìm thấy nội dung phù hợp với bộ lọc này...</p>
                            <button 
                                onClick={() => setFilters({category: '', country: '', year: '', status: ''})}
                                className="text-neon hover:underline font-bold"
                            >
                                Đặt lại bộ lọc
                            </button>
                        </div>
                    )}
                </section>
            </main>

            <Footer />
        </div>
    );
};
