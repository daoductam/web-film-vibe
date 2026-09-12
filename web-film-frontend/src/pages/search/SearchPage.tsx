import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { MovieCard } from '../../components/movie/MovieCard';
import { Search, Loader2, ChevronLeft, ChevronRight, Clock, Sparkles } from 'lucide-react';

const RECENT_SEARCHES_KEY = 'cinestream_recent_searches';
const POPULAR_SEARCH_TAGS = ['Hành Động', 'Anime', 'Kinh Dị', 'Tình Cảm', 'Marvel', 'Chiến Tranh'];

export const SearchPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const query = searchParams.get('q') || '';
    const pageParam = parseInt(searchParams.get('page') || '1', 10);
    const page = isNaN(pageParam) || pageParam < 1 ? 1 : pageParam;

    const [searchInput, setSearchInput] = useState(query);
    const [recentSearches, setRecentSearches] = useState<string[]>([]);

    useEffect(() => {
        try {
            const saved = localStorage.getItem(RECENT_SEARCHES_KEY);
            if (saved) {
                setRecentSearches(JSON.parse(saved));
            }
        } catch (e) {
            console.error('Failed to load recent searches', e);
        }
    }, []);

    const saveSearch = (term: string) => {
        const cleaned = term.trim();
        if (!cleaned) return;
        setRecentSearches(prev => {
            const filtered = prev.filter(item => item.toLowerCase() !== cleaned.toLowerCase());
            const updated = [cleaned, ...filtered].slice(0, 6);
            try {
                localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
            } catch (e) {
                console.error('Failed to save search', e);
            }
            return updated;
        });
    };

    // Keep local input in sync if URL query changes
    useEffect(() => {
        setSearchInput(query);
        if (query.trim()) {
            saveSearch(query);
        }
    }, [query]);

    const [isAiMode, setIsAiMode] = useState(false);

    // Standard search query
    const { data: standardResults, isLoading: isStandardLoading, isFetching: isStandardFetching } = useQuery({
        queryKey: ['search', query, page],
        queryFn: () => movieService.searchMovies(query, page, 24),
        enabled: query.trim().length > 0 && !isAiMode,
    });

    // AI Semantic Search query
    const { data: aiResults, isLoading: isAiLoading, isFetching: isAiFetching } = useQuery({
        queryKey: ['ai-search', query, page],
        queryFn: () => movieService.searchWithAI(query, page, 24),
        enabled: query.trim().length > 0 && isAiMode,
    });

    const isLoading = isAiMode ? isAiLoading : isStandardLoading;
    const isFetching = isAiMode ? isAiFetching : isStandardFetching;

    const movies = isAiMode
        ? (aiResults?.movies?.content || [])
        : (standardResults?.content || []);

    const totalElements = isAiMode
        ? (aiResults?.movies?.page?.totalElements ?? aiResults?.movies?.totalElements ?? movies.length)
        : (standardResults?.page?.totalElements ?? standardResults?.totalElements ?? movies.length);

    const totalPages = isAiMode
        ? (aiResults?.movies?.page?.totalPages ?? aiResults?.movies?.totalPages ?? 1)
        : (standardResults?.page?.totalPages ?? standardResults?.totalPages ?? 1);

    const handleSearchSubmit = (e: React.FormEvent, customTerm?: string) => {
        if (e) e.preventDefault();
        const term = (customTerm !== undefined ? customTerm : searchInput).trim();
        if (term) {
            saveSearch(term);
            setSearchParams({ q: term, page: '1' });
        }
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 1 && newPage <= totalPages && newPage !== page) {
            setSearchParams({ q: query, page: newPage.toString() });
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    return (
        <div className="bg-obsidian min-h-screen text-text-primary flex flex-col">
            <Navbar />
            
            <main className="flex-1 pt-28 pb-20 max-w-[1600px] w-full mx-auto px-4 md:px-6">
                {/* Search Bar on Page */}
                <div className="max-w-3xl mx-auto mb-10">
                    <form onSubmit={handleSearchSubmit} className="relative group">
                        <input
                            type="text"
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            placeholder={isAiMode ? "Mô tả tự nhiên: 'tìm phim anime hài học đường có phép thuật'..." : "Nhập tên phim, diễn viên hoặc từ khóa..."}
                            className={`w-full bg-white/5 border rounded-2xl py-4 pl-14 pr-32 text-base md:text-lg text-white placeholder-text-secondary focus:outline-none transition-all shadow-xl backdrop-blur-md ${
                                isAiMode 
                                    ? 'border-purple-500/50 focus:border-purple-400 focus:ring-2 focus:ring-purple-500/30' 
                                    : 'border-white/10 focus:border-neon focus:ring-2 focus:ring-neon/30'
                            }`}
                        />
                        <Search className={`absolute left-5 top-1/2 -translate-y-1/2 w-6 h-6 transition-colors ${
                            isAiMode ? 'text-purple-400' : 'text-text-secondary group-focus-within:text-neon'
                        }`} />
                        <button
                            type="submit"
                            className={`absolute right-2.5 top-1/2 -translate-y-1/2 px-5 py-2.5 rounded-xl font-bold text-sm transition-all shadow-md ${
                                isAiMode
                                    ? 'bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-400 hover:to-pink-400 text-white shadow-purple-500/30'
                                    : 'bg-neon hover:bg-white text-obsidian shadow-neon hover:shadow-white-glow'
                            }`}
                        >
                            Tìm kiếm
                        </button>
                    </form>

                    {/* Mode Toggle & Quick Tags */}
                    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 text-xs">
                        <div className="flex flex-wrap items-center gap-2">
                            {recentSearches.length > 0 ? (
                                <>
                                    <span className="text-gray-400 flex items-center gap-1">
                                        <Clock size={12} className="text-neon" /> Gần đây:
                                    </span>
                                    {recentSearches.map(tag => (
                                        <button
                                            key={tag}
                                            onClick={() => {
                                                setSearchInput(tag);
                                                handleSearchSubmit(undefined as any, tag);
                                            }}
                                            className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-neon/10 hover:text-neon border border-white/10 transition-colors text-gray-300"
                                        >
                                            {tag}
                                        </button>
                                    ))}
                                </>
                            ) : (
                                <>
                                    <span className="text-gray-400 flex items-center gap-1">
                                        <Sparkles size={12} className="text-neon" /> Xu hướng:
                                    </span>
                                    {POPULAR_SEARCH_TAGS.map(tag => (
                                        <button
                                            key={tag}
                                            onClick={() => {
                                                setSearchInput(tag);
                                                handleSearchSubmit(undefined as any, tag);
                                            }}
                                            className="px-2.5 py-1 rounded-full bg-white/5 hover:bg-neon/10 hover:text-neon border border-white/10 transition-colors text-gray-300"
                                        >
                                            {tag}
                                        </button>
                                    ))}
                                </>
                            )}
                        </div>

                        {/* AI Semantic Search Mode Toggle */}
                        <button
                            type="button"
                            onClick={() => setIsAiMode(!isAiMode)}
                            className={`flex items-center gap-2 px-3 py-1.5 rounded-full border transition-all ${
                                isAiMode
                                    ? 'bg-purple-500/20 border-purple-500 text-purple-300 shadow-[0_0_12px_rgba(168,85,247,0.4)]'
                                    : 'bg-white/5 border-white/10 text-gray-400 hover:text-white hover:border-white/30'
                            }`}
                        >
                            <Sparkles size={13} className={isAiMode ? 'text-purple-400 animate-spin' : ''} />
                            <span className="font-semibold">AI Semantic Search (Llama 3.3)</span>
                            <span className={`px-1.5 py-0.2 text-[10px] rounded uppercase font-bold ${
                                isAiMode ? 'bg-purple-500 text-white' : 'bg-white/10 text-gray-400'
                            }`}>
                                {isAiMode ? 'BẬT' : 'TẮT'}
                            </span>
                        </button>
                    </div>
                </div>

                {/* AI Intent Banner */}
                {isAiMode && query.trim() && aiResults?.explanation && (
                    <div className="max-w-3xl mx-auto mb-8 p-4 rounded-2xl bg-gradient-to-r from-purple-950/40 via-[#161327] to-purple-950/40 border border-purple-500/30 flex items-start gap-3 shadow-xl animate-in fade-in">
                        <div className="p-2 rounded-xl bg-purple-500/20 text-purple-400 shrink-0">
                            <Sparkles size={20} />
                        </div>
                        <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2 text-xs font-bold text-purple-400 uppercase tracking-wider">
                                <span>Phân tích ý định tìm kiếm AI</span>
                                <span className="size-1.5 rounded-full bg-purple-400"></span>
                                <span className="text-gray-400 normal-case font-normal">Groq Llama 3.3</span>
                            </div>
                            <p className="text-white font-medium text-sm mt-1">
                                {aiResults.explanation}
                            </p>
                            {aiResults.parsedIntent?.categories && aiResults.parsedIntent.categories.length > 0 && (
                                <div className="flex flex-wrap gap-1.5 mt-2">
                                    <span className="text-gray-400 text-xs">Bộ lọc áp dụng:</span>
                                    {aiResults.parsedIntent.categories.map((c: string) => (
                                        <span key={c} className="px-2 py-0.5 rounded bg-purple-500/20 border border-purple-500/30 text-purple-300 text-[11px] font-mono">
                                            #{c}
                                        </span>
                                    ))}
                                    {aiResults.parsedIntent.country && (
                                        <span className="px-2 py-0.5 rounded bg-purple-500/20 border border-purple-500/30 text-purple-300 text-[11px] font-mono">
                                            @{aiResults.parsedIntent.country}
                                        </span>
                                    )}
                                    {aiResults.parsedIntent.year && (
                                        <span className="px-2 py-0.5 rounded bg-purple-500/20 border border-purple-500/30 text-purple-300 text-[11px] font-mono">
                                            {aiResults.parsedIntent.year}
                                        </span>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* Header */}
                {query.trim() && (
                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                        <div>
                            <h1 className="text-2xl md:text-3xl font-serif font-black text-white italic">
                                {isAiMode ? 'KẾT QUẢ TÌM KIẾM THEO NGỮ NGHĨA AI' : 'KẾT QUẢ TÌM KIẾM'}
                            </h1>
                            <p className="text-text-secondary mt-1">
                                Tìm thấy <span className="text-neon font-bold">{totalElements}</span> kết quả cho: <span className="text-neon font-bold">"{query}"</span>
                                {totalPages > 1 && (
                                    <span className="text-gray-400 text-sm ml-2">
                                        (Trang {page} / {totalPages})
                                    </span>
                                )}
                            </p>
                        </div>
                    </div>
                )}

                {/* Results Grid */}
                {isLoading ? (
                    <div className="flex flex-col items-center justify-center py-20 gap-4">
                        <Loader2 className="w-10 h-10 text-neon animate-spin" />
                        <p className="text-gray-500 font-medium">Đang tìm kiếm phim hay cho bạn...</p>
                    </div>
                ) : !query.trim() ? (
                    <div className="flex flex-col items-center justify-center py-24 text-center">
                        <div className="size-20 rounded-full bg-white/5 flex items-center justify-center mb-6">
                            <Search className="w-10 h-10 text-gray-500" />
                        </div>
                        <h2 className="text-xl font-bold text-white mb-2">Khám phá thế giới điện ảnh</h2>
                        <p className="text-gray-500 max-w-md mx-auto">
                            Nhập từ khóa vào ô tìm kiếm ở trên để bắt đầu tìm những bộ phim yêu thích của bạn.
                        </p>
                    </div>
                ) : movies.length > 0 ? (
                    <>
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-6">
                            {movies.map(movie => (
                                <MovieCard key={movie.id} movie={movie} />
                            ))}
                        </div>

                        {/* Pagination */}
                        {totalPages > 1 && (
                            <div className="flex items-center justify-center gap-2 mt-12">
                                <button
                                    onClick={() => handlePageChange(page - 1)}
                                    disabled={page <= 1 || isFetching}
                                    aria-label="Trang trước"
                                    className="size-10 flex items-center justify-center rounded-lg border border-white/10 hover:bg-white/10 text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                                >
                                    <ChevronLeft size={18} />
                                </button>

                                {(() => {
                                    let pagesTarget: (number | string)[] = [];
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
                                                onClick={() => typeof p === 'number' && handlePageChange(p)}
                                                disabled={isFetching}
                                                className={`size-10 flex items-center justify-center rounded-lg transition-colors text-sm ${
                                                    page === p
                                                        ? 'bg-neon text-obsidian font-bold hover:bg-white'
                                                        : 'border border-white/10 hover:bg-white/10 text-white'
                                                }`}
                                            >
                                                {p}
                                            </button>
                                        )
                                    ));
                                })()}

                                <button
                                    onClick={() => handlePageChange(page + 1)}
                                    disabled={page >= totalPages || isFetching}
                                    aria-label="Trang tiếp theo"
                                    className="size-10 flex items-center justify-center rounded-lg border border-white/10 hover:bg-white/10 text-white transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                                >
                                    <ChevronRight size={18} />
                                </button>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="flex flex-col items-center justify-center py-32 text-center">
                        <div className="size-20 rounded-full bg-white/5 flex items-center justify-center mb-6">
                            <Search className="w-10 h-10 text-gray-600" />
                        </div>
                        <h2 className="text-xl font-bold text-white mb-2">Không tìm thấy phim nào</h2>
                        <p className="text-gray-500 max-w-md mx-auto">
                            Rất tiếc, chúng tôi không tìm thấy phim nào khớp với từ khóa <span className="text-white font-semibold">"{query}"</span>. 
                            Hãy thử lại với từ khóa khác hoặc kiểm tra lại chính tả.
                        </p>
                    </div>
                )}
            </main>

            <Footer />
        </div>
    );
};
