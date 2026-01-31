import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useState, useEffect } from 'react';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Play, Star, Share2, Download, Bookmark, MessageCircle, MonitorPlay } from 'lucide-react';
import { RelatedMovies } from './RelatedMovies';

export const MovieDetailPage = () => {
    const { slug } = useParams<{ slug: string }>();

    // Fetch movie detail
    const { data: movie, isLoading } = useQuery({
        queryKey: ['movie', slug],
        queryFn: () => movieService.getMovieDetail(slug || ''),
        enabled: !!slug,
    });

    const [isPlaying, setIsPlaying] = useState(false);
    const [selectedEpisode, setSelectedEpisode] = useState<any>(null); // Use Episode type properly if imported

    // Flatten episodes from all servers for easier access (or prefer first server)
    const allEpisodes = movie?.servers?.flatMap((s:any) => s.episodes) || [];

    // Reset state when movie changes
    useEffect(() => {
        if (allEpisodes.length > 0) {
            // Default to first episode of first server
            setSelectedEpisode(allEpisodes[0]);
        }
    }, [movie]);

    if (isLoading) {
         return (
            <div className="min-h-screen bg-obsidian flex items-center justify-center">
                 <div className="w-12 h-12 border-4 border-neon border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!movie) return <div className="text-white text-center pt-40">Film not found</div>;

    const handlePlay = () => {
        if (selectedEpisode?.linkEmbed) {
            setIsPlaying(true);
        } else {
            alert("Phim này chưa có link server!");
        }
    };

    return (
        <div className="bg-obsidian text-text-primary font-sans overflow-x-hidden antialiased selection:bg-neon selection:text-obsidian">
            <Navbar />
            
            <main className="min-h-screen relative pb-20">
                {/* Background Backdrop */}
                <div className="fixed inset-0 z-0 pointer-events-none">
                    <div 
                        className="absolute inset-0 bg-cover bg-center bg-no-repeat" 
                        style={{ backgroundImage: `url("${movie.posterUrl || movie.thumbUrl}")` }}
                    />
                    <div className="absolute inset-0 bg-gradient-to-b from-obsidian/70 via-obsidian/90 to-obsidian z-10" />
                    <div className="absolute inset-0 bg-obsidian/40 backdrop-blur-[4px] z-10" />
                </div>

                <div className="relative z-20 pt-28 max-w-[1600px] mx-auto px-6">
                    {/* Breadcrumbs */}
                    <div className="flex items-center gap-2 text-sm text-text-secondary mb-6 font-medium">
                        <a href="/" className="hover:text-neon">Trang chủ</a>
                        <span className="text-xs">{'>'}</span>
                        <a href="/movies" className="hover:text-neon">Phim lẻ</a>
                        <span className="text-xs">{'>'}</span>
                        <span className="text-white">{movie.title}</span>
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
                        {/* Main Content (Left Column) */}
                        <div className="lg:col-span-9 space-y-8">
                            
                            {/* Movie Header Info */}
                            <div className="space-y-4">
                                <h1 className="font-serif text-4xl md:text-6xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white via-white to-gray-400 leading-tight drop-shadow-2xl">
                                    {movie.title}
                                </h1>
                                <div className="flex flex-wrap items-center gap-4 text-sm md:text-base">
                                    <span className="flex items-center gap-1 text-neon font-bold">
                                        <Star className="w-4 h-4 fill-current" /> {movie.rating || 'N/A'} IMDb
                                    </span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-white">{movie.year}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-white">{movie.duration}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="px-2 py-0.5 rounded border border-white/20 text-xs font-bold bg-white/5 text-gray-300">{movie.quality}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-gray-300">{movie.categories?.map((c: any) => c.name).join(', ')}</span>
                                </div>
                            </div>

                            {/* Video Player Area */}
                            <div className="w-full aspect-video bg-black rounded-2xl overflow-hidden shadow-[0_0_50px_rgba(0,0,0,0.5)] border border-white/10 relative group">
                                {isPlaying && selectedEpisode?.linkEmbed ? (
                                    <iframe 
                                        src={selectedEpisode.linkEmbed} 
                                        className="w-full h-full" 
                                        allowFullScreen 
                                        title={movie.title}
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                    />
                                ) : (
                                    <>
                                        <div 
                                            className="absolute inset-0 bg-cover bg-center opacity-60 group-hover:opacity-40 transition-opacity duration-500" 
                                            style={{ backgroundImage: `url("${movie.posterUrl || movie.thumbUrl}")` }}
                                        />
                                        <div className="absolute inset-0 flex items-center justify-center">
                                            <button 
                                                onClick={handlePlay}
                                                className="size-20 md:size-24 bg-neon hover:bg-white text-obsidian rounded-full flex items-center justify-center transition-all duration-300 hover:scale-110 shadow-[0_0_30px_rgba(0,243,255,0.4)] group-hover:shadow-[0_0_50px_rgba(255,255,255,0.6)] z-20"
                                            >
                                                <Play className="w-10 h-10 ml-1 fill-current" />
                                            </button>
                                        </div>
                                    </>
                                )}
                            </div>

                            {/* Server/Episode Selection */}
                            <div className="flex flex-col gap-4 bg-white/5 border border-white/5 rounded-xl p-4 backdrop-blur-md">
                                <div className="flex items-center justify-between">
                                    <span className="text-sm font-bold text-gray-400">Chọn Server & Tập Phim:</span>
                                    <button className="text-sm text-red-400 hover:text-red-300 flex items-center gap-1 font-medium">
                                        <span className="material-symbols-outlined text-base">report</span> Báo lỗi
                                    </button>
                                </div>
                                
                                <div className="space-y-4">
                                {movie.servers && movie.servers.length > 0 ? (
                                    movie.servers.map((server: any) => (
                                        <div key={server.serverName} className="space-y-2">
                                            <h4 className="text-neon text-xs font-bold uppercase tracking-wider">{server.serverName}</h4>
                                            <div className="flex flex-wrap gap-2">
                                                {server.episodes.map((ep: any) => (
                                                    <button 
                                                        key={ep.id}
                                                        onClick={() => { setSelectedEpisode(ep); setIsPlaying(true); }}
                                                        className={`px-3 py-1.5 rounded-lg text-sm font-bold transition-all ${selectedEpisode?.id === ep.id ? 'bg-neon text-obsidian shadow-neon-sm' : 'bg-white/10 text-gray-300 hover:bg-white/20 hover:text-white'}`}
                                                    >
                                                        {ep.name}
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <span className="text-gray-500 text-sm">Chưa có tập phim nào.</span>
                                )}
                                </div>
                            </div>

                            {/* Movie Details Grid */}
                            <div className="grid md:grid-cols-3 gap-8 pt-4">
                                {/* Story & Info */}
                                <div className="md:col-span-2 space-y-6">
                                    <h2 className="text-2xl font-serif font-bold text-white flex items-center gap-2">
                                        <span className="w-1 h-6 bg-neon rounded-full"></span>
                                        Nội dung phim
                                    </h2>
                                    <div 
                                        className="text-gray-300 leading-relaxed text-lg font-light"
                                        dangerouslySetInnerHTML={{ __html: movie.description }}
                                    />
                                    <div className="flex flex-wrap gap-2 pt-2">
                                        {movie.categories?.map((cat: any) => (
                                            <span key={cat.id} className="px-3 py-1 bg-white/5 border border-white/10 rounded-full text-xs text-text-secondary hover:text-neon cursor-pointer transition-colors">
                                                {cat.name}
                                            </span>
                                        ))}
                                    </div>
                                </div>

                                {/* Cast & Director */}
                                <div className="space-y-6">
                                    <h3 className="text-lg font-bold text-white border-b border-white/10 pb-2">Đạo diễn & Diễn viên</h3>
                                    
                                    {/* Director */}
                                    <div className="flex items-center gap-3 group cursor-pointer">
                                        <div className="size-10 rounded-full bg-white/10 flex items-center justify-center">
                                            <MonitorPlay className="w-5 h-5 text-gray-400" />
                                        </div>
                                        <div>
                                            <p className="text-white text-sm font-bold group-hover:text-neon transition-colors">{movie.director}</p>
                                            <p className="text-text-secondary text-xs">Đạo diễn</p>
                                        </div>
                                    </div>

                                    {/* Casts List */}
                                    <div className="space-y-4">
                                         {movie.casts?.split(',').slice(0, 5).map((actor: string, idx: number) => (
                                            <div key={idx} className="flex items-center gap-3 group cursor-pointer">
                                                <div className="size-10 rounded-full bg-white/5 flex items-center justify-center text-xs font-bold text-gray-500 border border-white/10">
                                                    {actor.charAt(0)}
                                                </div>
                                                <div>
                                                    <p className="text-white text-sm font-bold group-hover:text-neon transition-colors">{actor.trim()}</p>
                                                    <p className="text-text-secondary text-xs">Diễn viên</p>
                                                </div>
                                            </div>
                                         ))}
                                    </div>
                                </div>
                            </div>
                            
                            {/* Comments Section */}
                            <div className="pt-10 border-t border-white/10">
                                <h2 className="text-2xl font-serif font-bold text-white mb-6">Bình luận & Đánh giá</h2>
                                <div className="bg-surface border border-white/5 rounded-2xl p-6 mb-8">
                                    <textarea 
                                        className="w-full bg-black/30 border border-white/10 rounded-xl p-4 text-white focus:border-neon focus:ring-1 focus:ring-neon transition-all resize-none h-24 placeholder-gray-500 focus:outline-none" 
                                        placeholder="Chia sẻ cảm nghĩ của bạn về bộ phim này..."
                                    />
                                    <div className="flex justify-between items-center mt-4">
                                        <div className="flex gap-2 text-text-secondary text-sm">
                                            <button className="hover:text-white"><MessageCircle className="w-5 h-5" /></button>
                                        </div>
                                        <button className="bg-neon hover:bg-white text-obsidian px-6 py-2 rounded-lg font-bold text-sm transition-colors">
                                            Gửi bình luận
                                        </button>
                                    </div>
                                </div>
                            </div>

                        </div>

                        {/* Sidebar (Right Column) */}
                        <div className="lg:col-span-3 space-y-8">
                            <div className="glass-card p-5 rounded-2xl space-y-3">
                                <button className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-white/10 text-white font-bold hover:bg-neon hover:text-obsidian transition-all group">
                                    <Bookmark className="w-5 h-5 group-hover:scale-110 transition-transform" />
                                    Lưu vào danh sách
                                </button>
                                <button className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-white/10 text-white font-bold hover:bg-neon hover:text-obsidian transition-all group">
                                    <Download className="w-5 h-5 group-hover:scale-110 transition-transform" />
                                    Tải xuống
                                </button>
                                <button className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-white/10 text-white font-bold hover:bg-neon hover:text-obsidian transition-all group">
                                    <Share2 className="w-5 h-5 group-hover:scale-110 transition-transform" />
                                    Chia sẻ
                                </button>
                            </div>

                            <div>
                                <h3 className="text-xl font-serif font-bold text-white mb-5 pl-2 border-l-4 border-neon">Phim gợi ý</h3>
                                <RelatedMovies categorySlug={movie.categories?.[0]?.slug} />
                            </div>
                        </div>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};
