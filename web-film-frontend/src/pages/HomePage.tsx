import { useQuery } from '@tanstack/react-query';
import { Navbar } from '../components/layout/Navbar';
import { Footer } from '../components/layout/Footer';
import { HeroSection } from '../components/movie/HeroSection';
import { MovieSection } from '../components/movie/MovieSection';
import { FeaturedCollection } from '../components/movie/FeaturedCollection';
import { movieService } from '../services/movie.service';

export const HomePage = () => {
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


    
    // Add debug/error handling
    if (loadingLatest || loadingPopular) {
        return (
            <div className="min-h-screen bg-obsidian flex items-center justify-center">
                 <div className="w-12 h-12 border-4 border-neon border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }
    
    // Check for errors
    if (!latestMovies && !popularMovies) {
         return (
            <div className="min-h-screen bg-obsidian flex items-center justify-center flex-col gap-4">
                 <h2 className="text-red-500 font-bold text-2xl">Không thể tải dữ liệu</h2>
                 <p className="text-gray-400">Vui lòng kiểm tra kết nối Server (http://localhost:8080/api/v1).</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-obsidian">
            <Navbar />
            <main className="min-h-screen">
                <HeroSection />
                
                <div className="relative z-30 -mt-20 pb-20 space-y-24">
                    {/* Inject Real Data */}
                    {latestMovies?.content && (
                         <MovieSection title="Phim mới cập nhật" movies={latestMovies.content} />
                    )}
                    
                    <FeaturedCollection />
                    
                    {popularMovies?.content && (
                        <MovieSection title="Phổ biến trên CineStream" movies={popularMovies.content} />
                    )}
                </div>
            </main>
            <Footer />
        </div>
    );
};
