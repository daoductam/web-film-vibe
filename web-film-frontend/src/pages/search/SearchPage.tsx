import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { MovieCard } from '../../components/movie/MovieCard';
import { Search, Loader2 } from 'lucide-react';

export const SearchPage = () => {
    const [searchParams] = useSearchParams();
    const query = searchParams.get('q') || '';

    const { data: searchResults, isLoading } = useQuery({
        queryKey: ['search', query],
        queryFn: () => movieService.searchMovies(query, 1, 24),
        enabled: query.length > 0
    });

    const movies = searchResults?.content || [];

    return (
        <div className="bg-obsidian min-h-screen text-text-primary">
            <Navbar />
            
            <main className="pt-28 pb-20 max-w-[1600px] mx-auto px-4 md:px-6">
                {/* Header */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
                    <div>
                        <h1 className="text-2xl md:text-3xl font-serif font-black text-white italic">
                            KẾT QUẢ TÌM KIẾM
                        </h1>
                        <p className="text-text-secondary mt-1">
                            Tìm thấy {searchResults?.totalElements || 0} kết quả cho: <span className="text-neon font-bold">"{query}"</span>
                        </p>
                    </div>
                </div>

                {/* Results Grid */}
                {isLoading ? (
                    <div className="flex flex-col items-center justify-center py-20 gap-4">
                        <Loader2 className="w-10 h-10 text-neon animate-spin" />
                        <p className="text-gray-500 font-medium">Đang tìm kiếm phim hay cho bạn...</p>
                    </div>
                ) : movies.length > 0 ? (
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-6">
                        {movies.map(movie => (
                            <MovieCard key={movie.id} movie={movie} />
                        ))}
                    </div>
                ) : (
                    <div className="flex flex-col items-center justify-center py-32 text-center">
                        <div className="size-20 rounded-full bg-white/5 flex items-center justify-center mb-6">
                            <Search className="w-10 h-10 text-gray-600" />
                        </div>
                        <h2 className="text-xl font-bold text-white mb-2">Không tìm thấy phim nào</h2>
                        <p className="text-gray-500 max-w-md mx-auto">
                            Rất tiếc, chúng tôi không tìm thấy phim nào khớp với từ khóa <span className="text-white">"{query}"</span>. 
                            Hãy thử lại với từ khóa khác hoặc kiểm tra lại chính tả.
                        </p>
                    </div>
                )}
            </main>

            <Footer />
        </div>
    );
};
