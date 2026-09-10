import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, useRef } from 'react';
import type { Episode } from '../../types';
import { StreamingPlayer } from '../../components/player/StreamingPlayer';
import type { PlaybackProgress } from '../../components/player/StreamingPlayer';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Play, Share2, Heart, MonitorPlay, Users } from 'lucide-react';
import { RelatedMovies } from './RelatedMovies';
import RatingStars from '../../components/movie/RatingStars';
import CommentSection from '../../components/movie/CommentSection';
import { useAuthStore } from '../../store/authStore';
import { personalizationService } from '../../services/personalization.service';
import { useToast } from '../../hooks/useToast';
import { MovieDetailSkeleton } from '../../components/movie/MovieDetailSkeleton';

export const MovieDetailPage = () => {
    const { slug } = useParams<{ slug: string }>();
    return <MovieDetailContent key={slug} slug={slug} />;
};

const MovieDetailContent = ({ slug }: { slug?: string }) => {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const queryClient = useQueryClient();
    const { showToast } = useToast();

    // Fetch movie detail
    const { data: movie, isLoading, refetch } = useQuery({
        queryKey: ['movie', slug],
        queryFn: () => movieService.getMovieDetail(slug || ''),
        enabled: !!slug,
    });

    const [isPlaying, setIsPlaying] = useState(false);
    const [chosenEpisode, setChosenEpisode] = useState<Episode | null>(null);
    const [autoNext, setAutoNext] = useState(true);
    const [isSavingFav, setIsSavingFav] = useState(false);
    const { token, user } = useAuthStore();
    const lastSaved = useRef(0);
    const saveQueue = useRef<Promise<unknown>>(Promise.resolve());
    const { data: favorites } = useQuery({ queryKey: ['favorites', user?.id], queryFn: personalizationService.getFavorites, enabled: !!token });
    const { data: history, isLoading: loadingHistory } = useQuery({ queryKey: ['history', user?.id], queryFn: personalizationService.getHistory, enabled: !!token });
    const savedHistory = history?.find(item => item.movieSlug === slug);
    const allEpisodes = movie?.servers?.flatMap(server => server.episodes) ?? [];
    const selectedEpisode = chosenEpisode ?? allEpisodes.find(episode => episode.slug === (searchParams.get('episode') || savedHistory?.lastEpisodeSlug)) ?? allEpisodes[0];
    const isFavorite = !!token && !!favorites?.some(item => item.movieSlug === slug);
    const resumeTime = selectedEpisode?.slug === savedHistory?.lastEpisodeSlug ? (savedHistory?.progressMs ?? 0) / 1000 : 0;
    const currentServer = movie?.servers?.find(server => server.episodes.some(episode => episode.id === selectedEpisode?.id));
    const nextEpisode = currentServer?.episodes[(currentServer.episodes.findIndex(episode => episode.id === selectedEpisode?.id) ?? -1) + 1];

    const selectEpisode = (episode: Episode) => {
        if (loadingHistory) return;
        setChosenEpisode(episode);
        setSearchParams({ episode: episode.slug }, { replace: true });
        setIsPlaying(true);
        lastSaved.current = 0;
        if (!episode.linkM3u8 && episode.linkEmbed) void saveProgress({ currentTime: episode.slug === savedHistory?.lastEpisodeSlug ? resumeTime : 0, duration: 0 }, true, episode);
    };

    const saveProgress = async ({ currentTime, duration }: PlaybackProgress, force = false, episode = selectedEpisode) => {
        if (!token || !movie || !episode || (!force && Date.now() - lastSaved.current < 10000)) return;
        // A player can finish unmounting after logout or account switching.
        if (useAuthStore.getState().user?.id !== user?.id) return;
        lastSaved.current = Date.now();
        saveQueue.current = saveQueue.current.catch(() => undefined).then(async () => {
          if (useAuthStore.getState().user?.id !== user?.id) return;
          try {
            await personalizationService.saveHistory({ movieSlug: movie.slug, title: movie.title,
                thumbUrl: movie.thumbUrl || movie.posterUrl, lastEpisodeSlug: episode.slug,
                lastEpisodeName: episode.name, progressMs: Math.round(currentTime * 1000), durationMs: Math.round(duration * 1000) });
            void queryClient.invalidateQueries({ queryKey: ['history'] });
        } catch {
            if (force) showToast('Chưa lưu được tiến độ xem. Vui lòng kiểm tra kết nối.', 'warning');
          }
        });
    };

    const handleToggleFavorite = async () => {
        if (!token) {
            showToast('Vui lòng đăng nhập để lưu phim vào yêu thích!', 'info');
            navigate('/login');
            return;
        }
        if (!movie?.slug) return;
        
        setIsSavingFav(true);
        try {
            if (isFavorite) {
                await personalizationService.removeFavorite(movie.slug);
                showToast('Đã xóa khỏi danh sách yêu thích', 'info');
            } else {
                await personalizationService.addFavorite({ 
                    movieSlug: movie.slug, 
                    title: movie.title,
                    thumbUrl: movie.thumbUrl || movie.posterUrl,
                    quality: movie.quality,
                    year: movie.year,
                    createdAt: new Date().toISOString() 
                });
                showToast('Đã thêm vào danh sách yêu thích!', 'success');
            }
            await queryClient.invalidateQueries({ queryKey: ['favorites'] });
        } catch (error) {
            console.error('Lỗi khi cập nhật yêu thích', error);
            showToast('Không thể cập nhật danh sách yêu thích', 'error');
        } finally {
            setIsSavingFav(false);
        }
    };

    if (isLoading) {
         return <MovieDetailSkeleton />;
    }

    if (!movie) return <div className="min-h-screen bg-obsidian text-white text-center pt-40"><Navbar /><p>Không thể tải phim.</p><button className="mt-4 text-neon" onClick={() => void refetch()}>Thử lại</button></div>;

    const handlePlay = () => {
        if (selectedEpisode?.linkM3u8 || selectedEpisode?.linkEmbed) {
            setChosenEpisode(selectedEpisode);
            setIsPlaying(true);
            if (!selectedEpisode.linkM3u8) void saveProgress({ currentTime: resumeTime, duration: (savedHistory?.durationMs ?? 0) / 1000 }, true);
        } else {
            showToast('Tập phim này chưa có nguồn phát.', 'info');
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

                <div className="relative z-20 pt-24 md:pt-28 max-w-[1600px] mx-auto px-4 md:px-6">
                    {/* Breadcrumbs */}
                    <div className="flex items-center gap-2 text-[10px] md:text-sm text-text-secondary mb-4 md:mb-6 font-medium overflow-x-auto hide-scrollbar whitespace-nowrap">
                        <a href="/" className="hover:text-neon">Trang chủ</a>
                        <span className="text-[10px]">{'>'}</span>
                        <a href="/movies" className="hover:text-neon">Phim lẻ</a>
                        <span className="text-[10px]">{'>'}</span>
                        <span className="text-white truncate">{movie.title}</span>
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
                                    <div className="flex items-center gap-1">
                                        <RatingStars 
                                            movieSlug={movie.slug} 
                                            initialAverage={movie.averageRating} 
                                            initialCount={movie.ratingCount}
                                            size={18}
                                        />
                                    </div>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-white">{movie.year}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-white">{movie.duration}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="px-2 py-0.5 rounded border border-white/20 text-xs font-bold bg-white/5 text-gray-300">{movie.quality}</span>
                                    <span className="text-text-secondary">|</span>
                                    <span className="text-gray-300">{movie.categories?.map(c => c.name).join(', ')}</span>
                                </div>
                            </div>

                            {/* Video Player Area */}
                            <div className="w-full aspect-video bg-black rounded-2xl overflow-hidden shadow-[0_0_50px_rgba(0,0,0,0.5)] border border-white/10 relative group">
                                {isPlaying && selectedEpisode?.linkM3u8 ? (
                                    <StreamingPlayer key={`${user?.id}-${selectedEpisode.id}`} src={selectedEpisode.linkM3u8} title={`${movie.title} - ${selectedEpisode.name}`}
                                        startTime={resumeTime} onProgress={saveProgress}
                                        onEnded={() => { if (autoNext && nextEpisode) selectEpisode(nextEpisode); }} />
                                ) : isPlaying && selectedEpisode?.linkEmbed ? (
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
                                                disabled={loadingHistory}
                                                aria-label={resumeTime > 0 ? 'Tiếp tục xem phim' : 'Phát phim'}
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
                                    <span className="text-xs md:text-sm font-bold text-gray-400">Chọn Server & Tập:</span>
                                    <label className="flex items-center gap-2 text-sm text-gray-300"><input type="checkbox" checked={autoNext} onChange={event => setAutoNext(event.target.checked)} />Tự chuyển tập</label>
                                </div>
                                
                                <div className="space-y-4">
                                {movie.servers && movie.servers.length > 0 ? (
                                    movie.servers.map(server => (
                                        <div key={server.serverName} className="space-y-2">
                                            <h4 className="text-neon text-[10px] md:text-xs font-bold uppercase tracking-wider">{server.serverName}</h4>
                                            <div className="flex flex-wrap gap-2">
                                                {server.episodes.map(ep => (
                                                    <button 
                                                        key={ep.id}
                                                        disabled={loadingHistory}
                                                        onClick={() => selectEpisode(ep)}
                                                        className={`px-3 py-1.5 rounded-lg text-xs md:text-sm font-bold transition-all ${selectedEpisode?.id === ep.id ? 'bg-neon text-obsidian shadow-neon-sm' : 'bg-white/10 text-gray-300 hover:bg-white/20 hover:text-white'}`}
                                                    >
                                                        {ep.name}
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <span className="text-gray-500 text-xs md:text-sm">Chưa có tập phim nào.</span>
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
                                    <p className="text-gray-300 leading-relaxed text-lg font-light whitespace-pre-line">{new DOMParser().parseFromString(movie.description || '', 'text/html').body.textContent}</p>
                                    <div className="flex flex-wrap gap-2 pt-2">
                                        {movie.categories?.map(cat => (
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
                                {selectedEpisode && (
                                    <CommentSection 
                                        movieSlug={movie.slug} 
                                        episodeSlug={selectedEpisode.slug} 
                                    />
                                )}
                            </div>

                        </div>

                        {/* Sidebar (Right Column) */}
                        <div className="lg:col-span-3 space-y-8">
                            <div className="glass-card p-5 rounded-2xl space-y-3">
                                <button 
                                onClick={handleToggleFavorite}
                                disabled={isSavingFav}
                                className={`flex items-center gap-2 px-6 py-3 rounded-xl border transition-all ${isFavorite ? 'bg-red-500/10 border-red-500 text-red-500' : 'border-white/10 text-white hover:bg-white/5'}`}
                            >
                                <Heart className={`w-5 h-5 ${isFavorite ? 'fill-current' : ''}`} />
                                <span>{isFavorite ? 'Đã yêu thích' : 'Yêu thích'}</span>
                            </button>
                                <button onClick={() => navigate(`/watch-party?movie=${encodeURIComponent(movie.slug)}`)} className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-white/10 text-white font-bold hover:bg-neon hover:text-obsidian transition-all group">
                                    <Users className="w-5 h-5" />
                                    Xem cùng bạn bè
                                </button>
                                <button onClick={async () => {
                                    try {
                                        if (navigator.share) await navigator.share({ title: movie.title, url: window.location.href });
                                        else { await navigator.clipboard.writeText(window.location.href); showToast('Đã sao chép liên kết phim!', 'success'); }
                                    } catch (error) { if (!(error instanceof DOMException && error.name === 'AbortError')) showToast('Không thể chia sẻ liên kết.', 'error'); }
                                }} className="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-xl bg-white/10 text-white font-bold hover:bg-neon hover:text-obsidian transition-all group">
                                    <Share2 className="w-5 h-5 group-hover:scale-110 transition-transform" />
                                    Chia sẻ
                                </button>
                            </div>

                            <div>
                                <h3 className="text-xl font-serif font-bold text-white mb-5 pl-2 border-l-4 border-neon">Phim gợi ý</h3>
                                <RelatedMovies categorySlug={movie.categories?.[0]?.slug} movieSlug={movie.slug} />
                            </div>
                        </div>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};
