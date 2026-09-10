import { useQuery } from '@tanstack/react-query';
import { Navbar } from '../components/layout/Navbar';
import { Footer } from '../components/layout/Footer';
import { HeroSection } from '../components/movie/HeroSection';
import { MovieSection } from '../components/movie/MovieSection';
import { FeaturedCollection } from '../components/movie/FeaturedCollection';
import { movieService } from '../services/movie.service';
import { useAuthStore } from '../store/authStore';
import { ContinueWatching } from '../components/movie/ContinueWatching';

export const HomePage = () => {
    const { token, user } = useAuthStore();
    const isAuthenticated = !!token;

    // Fetch latest movies
    const { data: latestMovies, isLoading: loadingLatest } = useQuery({
        queryKey: ['movies', 'latest'],
        queryFn: () => movieService.getLatestMovies(0, 10),
    });

    // Fetch action movies (Example of filtered fetch)
    // You might want to update fetch logic based on actual available categories in your DB
    // Or fetch generic collections if filtering by type isn't fully ready
    const { data: popularMovies, isLoading: loadingPopular } = useQuery({
        queryKey: ['movies', 'popular'],
        queryFn: () => movieService.getPopularMovies(0, 10), 
    });

    // Fetch personalized recommendations (Neo4j Graph-based)
    const { data: recommendedMovies, isLoading: loadingRecommended } = useQuery({
        queryKey: ['movies', 'recommended', user?.id],
        queryFn: () => movieService.getPersonalizedRecommendations(),
        enabled: isAuthenticated,
    });
    
    // Check for errors
    if (!loadingLatest && !loadingPopular && !latestMovies && !popularMovies) {
         return (
            <div className="min-h-screen bg-obsidian flex items-center justify-center flex-col gap-4">
                 <h2 className="text-red-500 font-bold text-2xl">Không thể tải dữ liệu</h2>
                 <p className="text-gray-400">Vui lòng kiểm tra kết nối và thử lại.</p>
                 <button onClick={() => window.location.reload()} className="text-neon">Thử lại</button>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-obsidian">
            <Navbar />
            <main className="min-h-screen">
                <HeroSection />
                
                <div className="relative z-30 mt-10 md:-mt-20 pb-20 space-y-16 md:space-y-24">
                    <ContinueWatching />
                    {/* Personalized Recommendations Section */}
                    {isAuthenticated && recommendedMovies && recommendedMovies.length > 0 && (
                        <MovieSection 
                            title="Gợi ý dành riêng cho bạn" 
                            movies={recommendedMovies} 
                            isLoading={loadingRecommended} 
                        />
                    )}

                    {/* Latest Movies Section */}
                    <MovieSection 
                        title="Phim mới cập nhật" 
                        movies={latestMovies?.content} 
                        isLoading={loadingLatest} 
                    />
                    
                    <FeaturedCollection />
                    
                    {/* Popular Movies Section */}
                    <MovieSection 
                        title="Phổ biến trên CineStream" 
                        movies={popularMovies?.content} 
                        isLoading={loadingPopular} 
                    />
                </div>
            </main>
            <Footer />
        </div>
    );
};
