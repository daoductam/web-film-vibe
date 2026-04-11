import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useRef } from 'react';
import { MovieCard } from './MovieCard';
import type { Movie } from '../../types';

interface MovieSectionProps {
    title: string;
    movies?: Movie[]; // Optional because data might be loading
}

export const MovieSection = ({ title, movies = [] }: MovieSectionProps) => {
    const scrollRef = useRef<HTMLDivElement>(null);

    const scroll = (direction: 'left' | 'right') => {
        if (scrollRef.current) {
            const { current } = scrollRef;
            const scrollAmount = direction === 'left' ? -current.offsetWidth : current.offsetWidth;
            current.scrollBy({ left: scrollAmount, behavior: 'smooth' });
        }
    };

    if (!movies || movies.length === 0) return null;

    return (
        <section className="max-w-[1600px] mx-auto px-4 md:px-6">
            <div className="flex items-center justify-between mb-6 md:mb-8">
                <h2 className="text-2xl md:text-3xl font-serif font-bold text-white border-l-4 border-neon pl-4">{title}</h2>
                <div className="hidden sm:flex gap-2">
                    <button 
                        onClick={() => scroll('left')}
                        className="size-9 md:size-10 rounded-full border border-white/10 flex items-center justify-center hover:bg-white/10 text-white transition-colors"
                    >
                        <ChevronLeft className="w-5 h-5 md:w-6 md:h-6" />
                    </button>
                    <button 
                         onClick={() => scroll('right')}
                        className="size-9 md:size-10 rounded-full border border-white/10 flex items-center justify-center hover:bg-white/10 text-white transition-colors"
                    >
                        <ChevronRight className="w-5 h-5 md:w-6 md:h-6" />
                    </button>
                </div>
            </div>
            
            <div 
                ref={scrollRef}
                className="flex gap-6 overflow-x-auto hide-scrollbar pb-20 pt-10 snap-x px-4 -mx-4"
            >
                {movies.map((movie) => (
                    <MovieCard key={movie.id} movie={movie} />
                ))}
            </div>
        </section>
    );
};
