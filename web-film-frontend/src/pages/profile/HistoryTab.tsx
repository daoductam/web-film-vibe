import { useQuery } from '@tanstack/react-query';
import { personalizationService } from '../../services/personalization.service';
import { Link } from 'react-router-dom';
import { Clock } from 'lucide-react';

export const HistoryTab = () => {
    const { data: history, isLoading } = useQuery({
        queryKey: ['history'],
        queryFn: () => personalizationService.getHistory()
    });

    if (isLoading) {
        return <div className="animate-pulse">Đang tải lịch sử...</div>;
    }

    if (!history || history.length === 0) {
        return <div className="text-gray-400 text-center pt-10">Bạn chưa xem bộ phim nào gần đây.</div>;
    }

    return (
        <div>
            <h3 className="text-2xl font-bold text-white mb-6 font-serif">Lịch sử xem phim</h3>
            <div className="space-y-4">
                {history.map((item, idx) => (
                    <Link key={item.movieSlug + idx} to={`/movie/${item.movieSlug}`} className="flex flex-col sm:flex-row gap-4 p-4 rounded-xl bg-white/5 hover:bg-white/10 transition-colors border border-white/5 group">
                        <div className="w-32 aspect-video bg-black rounded-lg overflow-hidden shrink-0 relative">
                             <img src={item.thumbUrl} alt={item.title} className="w-full h-full object-cover opacity-80 group-hover:opacity-100 transition-opacity" />
                             <div className="absolute bottom-0 left-0 h-1 bg-neon" style={{ width: `${(item.progressMs / (item.durationMs || 1)) * 100}%` }}></div>
                        </div>
                        <div className="flex-1 flex flex-col justify-center">
                            <h4 className="text-lg font-bold text-white mb-1">{item.title}</h4>
                            <p className="text-sm text-neon font-medium">{item.lastEpisodeName}</p>
                            <div className="flex items-center gap-1 text-xs text-text-secondary mt-2">
                                <Clock className="w-3 h-3" />
                                {new Date(item.updatedAt).toLocaleString('vi-VN')}
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
};
