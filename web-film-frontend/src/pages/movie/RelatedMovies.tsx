import { useQuery } from '@tanstack/react-query';
import { movieService } from '../../services/movie.service';
import { Star } from 'lucide-react';

interface RelatedMoviesProps {
    categorySlug?: string;
}

export const RelatedMovies = ({ categorySlug }: RelatedMoviesProps) => {
    const { data: movies, isLoading } = useQuery({
        queryKey: ['movies', 'related', categorySlug],
        queryFn: () => {
            if (categorySlug) {
                return movieService.getMoviesByCategory(categorySlug, 0, 5);
            }
            return movieService.getPopularMovies(0, 5);
        },
    });

    if (isLoading) {
         return (
             <div className="flex flex-col gap-4">
                 {[1, 2, 3].map((i) => (
                     <div key={i} className="flex gap-3 p-2 animate-pulse">
                         <div className="w-20 aspect-[2/3] rounded-lg bg-white/10"></div>
                         <div className="flex-1 py-1 space-y-2">
                             <div className="h-4 bg-white/10 rounded w-3/4"></div>
                             <div className="h-3 bg-white/10 rounded w-1/2"></div>
                         </div>
                     </div>
                 ))}
             </div>
         );
    }

    if (!movies?.content || movies.content.length === 0) return <p className="text-text-secondary text-sm">Không có phim gợi ý.</p>;

    return (
        <div className="flex flex-col gap-4">
            {movies.content.map((movie) => (
                <a key={movie.id} href={`/movie/${movie.slug}`} className="flex gap-3 group p-2 rounded-xl hover:bg-white/5 transition-colors cursor-pointer">
                    <div className="w-20 aspect-[2/3] rounded-lg overflow-hidden relative">
                        <img 
                            src={movie.posterUrl || movie.thumbUrl} 
                            alt={movie.title}
                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" 
                        />
                    </div>
                    <div className="flex-1 py-1">
                        <h4 className="text-white font-bold text-sm leading-tight group-hover:text-neon transition-colors line-clamp-2">
                            {movie.title}
                        </h4>
                        <p className="text-text-secondary text-xs mt-1">{movie.year} • {movie.quality}</p>
                        <div className="flex items-center gap-1 mt-2 text-neon text-xs font-bold">
                            <Star className="w-3 h-3 fill-current" /> {movie.rating || 'N/A'}
                        </div>
                    </div>
                </a>
            ))}
        </div>
    );
};
