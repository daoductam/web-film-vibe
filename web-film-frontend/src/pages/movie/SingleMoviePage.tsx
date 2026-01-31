import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Link } from 'react-router-dom';
import { Play } from 'lucide-react';

export const SingleMoviePage = () => {
    // Filter States
    const [filters, setFilters] = useState({
        category: '',
        country: '',
        year: '',
        status: ''
    });

    const [page, setPage] = useState(1);

    // Fetch Movies (SINGLE type)
    const { data: moviesData, isLoading } = useQuery({
        queryKey: ['single-movies', filters, page],
        queryFn: () => movieService.filterMovies({
            type: 'SINGLE',
            category: filters.category || undefined,
            country: filters.country || undefined,
            year: filters.year ? parseInt(filters.year) : undefined,
            status: filters.status || undefined,
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
                <section className="relative w-full h-[600px] flex items-center overflow-hidden">
                    <div className="absolute inset-0 z-0">
                        <div className="absolute inset-0 bg-gradient-to-t from-obsidian via-obsidian/30 to-transparent z-10"></div>
                        <div className="absolute inset-0 bg-gradient-to-r from-obsidian via-obsidian/60 to-transparent z-10"></div>
                        <div 
                            className="w-full h-full bg-cover bg-center transition-all duration-700" 
                            style={{ 
                                backgroundImage: `url("${featuredMovie?.posterUrl || featuredMovie?.thumbUrl || 'https://lh3.googleusercontent.com/aida-public/AB6AXuCR-i8dtBbQiiS4Fb598zX8V6j0GXygXiDa_gEJxZ_ohvvFQzxQcMLJv5SYo8HSfgsPQA4SaWAWk2v7Y6nZT6dcwLIjdxgXsdcW1BJTSxfNBLQYhn1MbUlcFwIM5h00YjHADRo-hh54ZtBlyGHhwcI-3bk7X8xKWbsWIBEqN58PpCddpvDwkV4zADrUOD-PoLuKbYC5Ocw-8VGjCvjsqYdGFYE0oK-H1T_jKqNYk8-M7Ub-yCnK7Kn7Y77WYxAj1s1E62TpqfW_xFU'}")`,
                                backgroundPosition: 'top center'
                            }}
                        ></div>
                    </div>
                    
                    <div className="relative z-20 container mx-auto px-6 pt-20 max-w-[1600px]">
                        {featuredMovie ? (
                            <div className="max-w-3xl space-y-6 animate-fade-in-up">
                                <div className="flex flex-wrap gap-3 items-center">
                                    <div className="px-3 py-1 rounded bg-neon text-obsidian text-xs font-bold tracking-wider shadow-neon-sm">
                                        PHIM LẺ NỔI BẬT
                                    </div>
                                    <span className="flex items-center gap-1 text-yellow-400 text-sm font-bold">
                                        <span className="material-symbols-outlined filled text-lg">star</span> {featuredMovie.viewCount ? (featuredMovie.viewCount / 1000).toFixed(1) + 'K' : 'New'}
                                    </span>
                                    <span className="text-gray-300 text-sm font-medium">{featuredMovie.year} • {featuredMovie.countries?.[0]?.name} • {featuredMovie.duration || '?? min'}</span>
                                    <span className="px-2 py-0.5 rounded border border-gray-600 text-gray-300 text-xs font-bold">{featuredMovie.quality}</span>
                                </div>
                                <h1 className="font-serif text-5xl md:text-7xl font-black text-transparent bg-clip-text bg-gradient-to-br from-white via-white to-gray-400 leading-none drop-shadow-2xl translate-y-2">
                                    {featuredMovie.title}
                                </h1>
                                <p className="text-lg text-gray-300 leading-relaxed font-light max-w-2xl translate-y-2 line-clamp-3">
                                    {featuredMovie.description?.replace(/<[^>]*>/g, '')}
                                </p>
                                <div className="flex flex-wrap gap-4 pt-4 translate-y-2">
                                    <Link to={`/movie/${featuredMovie.slug}`} className="flex items-center gap-3 px-8 py-3 rounded-xl bg-neon text-obsidian font-bold text-lg hover:bg-white transition-all shadow-neon hover:shadow-[0_0_30px_rgba(255,255,255,0.5)] group">
                                        <span className="material-symbols-outlined filled group-hover:scale-110 transition-transform">play_arrow</span>
                                        Xem ngay
                                    </Link>
                                    <button className="flex items-center gap-3 px-8 py-3 rounded-xl glass-card text-white font-bold text-lg hover:bg-white/10 transition-all">
                                        <span className="material-symbols-outlined">info</span>
                                        Chi tiết
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="h-64 flex items-center text-white">Loading featured...</div>
                        )}
                    </div>
                </section>

                {/* Filter Bar */}
                <section className="sticky top-20 z-40 w-full border-y border-white/5 bg-obsidian/80 backdrop-blur-xl">
                    <div className="max-w-[1600px] mx-auto px-6 py-4">
                        <div className="flex flex-col md:flex-row gap-4 justify-between items-center">
                            <div className="flex flex-wrap gap-4 w-full md:w-auto">
                                {/* Genre Filter */}
                                <div className="relative group">
                                    <select 
                                        className="appearance-none bg-surface border border-white/10 text-white rounded-lg px-4 py-2 pr-10 focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon text-sm w-full md:w-40 hover:bg-white/5 transition-colors cursor-pointer"
                                        value={filters.category}
                                        onChange={(e) => handleFilterChange('category', e.target.value)}
                                    >
                                        <option value="" className="bg-gray-900 text-white">Thể loại</option>
                                        <option value="hanh-dong" className="bg-gray-900 text-white">Hành động</option>
                                        <option value="tinh-cam" className="bg-gray-900 text-white">Tình cảm</option>
                                        <option value="co-trang" className="bg-gray-900 text-white">Cổ trang</option>
                                        <option value="tam-ly" className="bg-gray-900 text-white">Tâm lý</option>
                                        <option value="hai-huoc" className="bg-gray-900 text-white">Hài hước</option>
                                        <option value="hinh-su" className="bg-gray-900 text-white">Hình sự</option>
                                        <option value="kinh-di" className="bg-gray-900 text-white">Kinh dị</option>
                                    </select>
                                </div>

                                {/* Country Filter */}
                                <div className="relative group">
                                    <select 
                                        className="appearance-none bg-surface border border-white/10 text-white rounded-lg px-4 py-2 pr-10 focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon text-sm w-full md:w-40 hover:bg-white/5 transition-colors cursor-pointer"
                                        value={filters.country}
                                        onChange={(e) => handleFilterChange('country', e.target.value)}
                                    >
                                        <option value="" className="bg-gray-900 text-white">Quốc gia</option>
                                        <option value="trung-quoc" className="bg-gray-900 text-white">Trung Quốc</option>
                                        <option value="han-quoc" className="bg-gray-900 text-white">Hàn Quốc</option>
                                        <option value="au-my" className="bg-gray-900 text-white">Âu Mỹ</option>
                                        <option value="nhat-ban" className="bg-gray-900 text-white">Nhật Bản</option>
                                        <option value="viet-nam" className="bg-gray-900 text-white">Việt Nam</option>
                                        <option value="thai-lan" className="bg-gray-900 text-white">Thái Lan</option>
                                    </select>
                                </div>

                                {/* Year Filter */}
                                <div className="relative group">
                                    <select 
                                        className="appearance-none bg-surface border border-white/10 text-white rounded-lg px-4 py-2 pr-10 focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon text-sm w-full md:w-40 hover:bg-white/5 transition-colors cursor-pointer"
                                        value={filters.year}
                                        onChange={(e) => handleFilterChange('year', e.target.value)}
                                    >
                                        <option value="" className="bg-gray-900 text-white">Năm phát hành</option>
                                        <option value="2025" className="bg-gray-900 text-white">2025</option>
                                        <option value="2024" className="bg-gray-900 text-white">2024</option>
                                        <option value="2023" className="bg-gray-900 text-white">2023</option>
                                        <option value="2022" className="bg-gray-900 text-white">2022</option>
                                        <option value="2021" className="bg-gray-900 text-white">2021</option>
                                    </select>
                                </div>

                                {/* Status Filter (Might be less relevant for single movies but keeping for consistency or filtering 'upcoming') */}
                                <div className="relative group">
                                    <select 
                                        className="appearance-none bg-surface border border-white/10 text-white rounded-lg px-4 py-2 pr-10 focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon text-sm w-full md:w-40 hover:bg-white/5 transition-colors cursor-pointer"
                                        value={filters.status}
                                        onChange={(e) => handleFilterChange('status', e.target.value)}
                                    >
                                        <option value="" className="bg-gray-900 text-white">Trạng thái</option>
                                        <option value="ONGOING" className="bg-gray-900 text-white">Đang chiếu</option>
                                        <option value="COMPLETED" className="bg-gray-900 text-white">Hoàn thành</option>
                                    </select>
                                </div>
                            </div>

                            <div className="flex items-center gap-2 text-text-secondary text-sm whitespace-nowrap">
                                <span>Sắp xếp:</span>
                                <button className="text-neon font-bold hover:underline">Mới nhất</button>
                                <span>|</span>
                                <button className="hover:text-white transition-colors">Xem nhiều nhất</button>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Movie Grid */}
                <section className="max-w-[1600px] mx-auto px-6 py-12">
                    <h2 className="text-2xl font-serif font-bold text-white mb-8 border-l-4 border-neon pl-4">Phim Lẻ Mới Cập Nhật</h2>
                    
                    {isLoading ? (
                        <div className="flex justify-center py-20">
                            <div className="w-10 h-10 border-4 border-neon border-t-transparent rounded-full animate-spin"></div>
                        </div>
                    ) : (
                        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 xl:grid-cols-6 gap-6">
                            {moviesData?.content?.map((movie: any) => (
                                <Link key={movie.id} to={`/movie/${movie.slug}`} className="group relative cursor-pointer interactive-card rounded-2xl glass-card border border-white/5 bg-surface overflow-hidden block">
                                    <div className="aspect-[2/3] w-full relative">
                                        <img 
                                            alt={movie.title} 
                                            className="w-full h-full object-cover poster-image transition-all duration-300 rounded-t-2xl" 
                                            src={movie.posterUrl || movie.thumbUrl}
                                            loading="lazy"
                                        />
                                        <div className="video-preview absolute inset-0 bg-black flex items-center justify-center overflow-hidden opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                            {/* Preview Image (reuse poster for now as 'video' placeholder) */}
                                            <img 
                                                alt="Preview" 
                                                className="w-full h-full object-cover opacity-60" 
                                                src={movie.thumbUrl || movie.posterUrl}
                                            />
                                            <div className="absolute inset-0 bg-gradient-to-t from-black via-transparent to-transparent"></div>
                                            <span className="material-symbols-outlined text-4xl text-neon animate-pulse z-10"><Play className="fill-current w-12 h-12"/></span>
                                        </div>
                                        <div className="absolute top-2 right-2 z-20 px-2 py-0.5 bg-neon text-obsidian text-[10px] font-bold rounded shadow-neon-sm">
                                            {movie.quality}
                                        </div>
                                        <div className="absolute top-2 left-2 z-20 px-2 py-0.5 bg-black/60 backdrop-blur rounded text-white text-[10px] font-bold border border-white/10">
                                            {movie.countries?.[0]?.name || 'N/A'}
                                        </div>
                                    </div>
                                    
                                    {/* Base Info (Hidden on Hover) */}
                                    <div className="p-3 card-info-base group-hover:hidden">
                                        <h3 className="font-serif text-base text-white font-bold truncate">{movie.title}</h3>
                                        <p className="text-xs text-text-secondary mt-1">
                                            {(movie.totalEpisodes && movie.totalEpisodes > 0) 
                                                ? `${movie.totalEpisodes} Tập` 
                                                : (movie.currentEpisode || 'Đang cập nhật')}
                                            {movie.categories?.[0]?.name && ` • ${movie.categories[0].name}`}
                                        </p>
                                    </div>

                                    {/* Expanded Info (Shown on Hover) */}
                                    <div className="card-content absolute bottom-0 left-0 w-full p-4 bg-gradient-to-t from-obsidian via-obsidian to-transparent z-20 opacity-0 group-hover:opacity-100 group-hover:translate-y-0 translate-y-4 transition-all duration-300">
                                        <h3 className="font-serif text-base text-white font-bold mb-1 line-clamp-1">{movie.title}</h3>
                                        <div className="flex items-center gap-2 mb-2">
                                            <span className="text-neon text-xs font-bold">{movie.rating || 'N/A'}/10</span>
                                            <span className="text-[10px] text-gray-400">{movie.year}</span>
                                        </div>
                                        <p className="text-[10px] text-gray-300 line-clamp-2 mb-3">
                                            {movie.originTitle}
                                        </p>
                                        <button className="w-full flex items-center justify-center gap-1 bg-white text-black py-1.5 rounded-lg text-xs font-bold hover:bg-neon transition-colors">
                                            <Play className="w-3 h-3 fill-current" /> Xem ngay
                                        </button>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    )}

                    {/* Pagination */}
                    <div className="flex justify-center items-center gap-2 mt-16 mb-8">
                        <button 
                            onClick={() => setPage(p => Math.max(1, p - 1))}
                            disabled={page === 1}
                            className="size-10 flex items-center justify-center rounded-lg border border-white/10 hover:bg-white/10 text-white transition-colors disabled:opacity-50"
                        >
                            <span className="material-symbols-outlined text-sm">chevron_left</span>
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
                                    <span key={`dots-${index}`} className="size-10 flex items-center justify-center text-gray-500">...</span>
                                ) : (
                                    <button 
                                        key={p}
                                        onClick={() => typeof p === 'number' && setPage(p)}
                                        className={`size-10 flex items-center justify-center rounded-lg transition-colors ${page === p ? 'bg-neon text-obsidian font-bold hover:bg-white' : 'border border-white/10 hover:bg-white/10 text-white'}`}
                                    >
                                        {p}
                                    </button>
                                )
                            ));
                        })()}

                        <button 
                            onClick={() => setPage(p => Math.min(totalPages || 1, p + 1))}
                            disabled={page === (totalPages || 1)}
                            className="size-10 flex items-center justify-center rounded-lg border border-white/10 hover:bg-white/10 text-white transition-colors disabled:opacity-50"
                        >
                            <span className="material-symbols-outlined text-sm">chevron_right</span>
                        </button>
                    </div>
                </section>
            </main>

            <Footer />
        </div>
    );
};
