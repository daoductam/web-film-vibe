import React, { useState, useEffect } from 'react';
import { Star } from 'lucide-react';
import { socialService } from '../../services/social.service';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { motion, AnimatePresence } from 'framer-motion';

function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

interface RatingStarsProps {
    movieSlug: string;
    initialAverage?: number;
    initialCount?: number;
    initialUserRating?: number;
    size?: number;
    interactive?: boolean;
}

const RatingStars: React.FC<RatingStarsProps> = ({
    movieSlug,
    initialAverage = 0,
    initialCount = 0,
    initialUserRating,
    size = 20,
    interactive = true,
}) => {
    const [average, setAverage] = useState(initialAverage);
    const [count, setCount] = useState(initialCount);
    const [userRating, setUserRating] = useState(initialUserRating);
    const [hoverRating, setHoverRating] = useState(0);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!initialAverage || !initialCount) {
            fetchRating();
        }
    }, [movieSlug]);

    const fetchRating = async () => {
        try {
            const response = await socialService.getMovieRating(movieSlug);
            if (response.success) {
                setAverage(response.data.averageRating);
                setCount(response.data.totalRatings);
                setUserRating(response.data.userRating);
            }
        } catch (error) {
            console.error('Failed to fetch rating:', error);
        }
    };

    const handleRate = async (score: number) => {
        if (!interactive || loading) return;
        setLoading(true);
        try {
            const response = await socialService.addOrUpdateRating(movieSlug, score);
            if (response.success) {
                setUserRating(score);
                // Refresh overall stats
                fetchRating();
            }
        } catch (error) {
            console.error('Failed to rate:', error);
            // In a real app, show a toast here (e.g. "Login to rate")
        } finally {
            setLoading(false);
        }
    };

    const renderStar = (index: number) => {
        const starValue = index + 1;
        const isSelected = (hoverRating || userRating || 0) >= starValue;
        const isHalf = !hoverRating && !userRating && average >= starValue - 0.5 && average < starValue;

        return (
            <motion.button
                key={index}
                whileHover={interactive ? { scale: 1.2 } : {}}
                whileTap={interactive ? { scale: 0.9 } : {}}
                className={cn(
                    "focus:outline-none transition-colors",
                    interactive ? "cursor-pointer" : "cursor-default",
                    isSelected ? "text-yellow-400" : "text-gray-600"
                )}
                onMouseEnter={() => interactive && setHoverRating(starValue)}
                onMouseLeave={() => interactive && setHoverRating(0)}
                onClick={() => handleRate(starValue)}
                disabled={loading}
            >
                <Star
                    size={size}
                    fill={(isSelected || isHalf) ? "currentColor" : "none"}
                    strokeWidth={2}
                />
            </motion.button>
        );
    };

    return (
        <div className="flex flex-col gap-1">
            <div className="flex items-center gap-1">
                {[...Array(5)].map((_, i) => renderStar(i))}
                <span className="ml-2 text-sm font-medium text-gray-300">
                    {average.toFixed(1)} <span className="text-gray-500 text-xs">({count} lượt)</span>
                </span>
            </div>
            <AnimatePresence>
                {userRating && (
                    <motion.p 
                        initial={{ opacity: 0, y: -5 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="text-[10px] text-yellow-500/80 font-medium"
                    >
                        Bạn đã đánh giá {userRating} sao
                    </motion.p>
                )}
            </AnimatePresence>
        </div>
    );
};

export default RatingStars;
