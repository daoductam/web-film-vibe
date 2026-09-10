import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Play } from 'lucide-react';
import { personalizationService } from '../../services/personalization.service';
import { useAuthStore } from '../../store/authStore';

export function ContinueWatching() {
    const { user, token } = useAuthStore();
    const { data } = useQuery({ queryKey: ['history', user?.id], queryFn: personalizationService.getHistory, enabled: !!token });
    const history = data?.filter(item => item.durationMs <= 0 || item.progressMs < item.durationMs * 0.95).slice(0, 10);
    if (!token || !history?.length) return null;
    return <section className="mx-auto max-w-[1600px] px-4 md:px-6">
        <div className="mb-6 flex items-center justify-between gap-3"><h2 className="text-2xl font-bold text-white md:text-3xl">Tiếp tục xem</h2><Link to="/profile?tab=history" className="text-sm text-neon">Xem lịch sử</Link></div>
        <div className="flex gap-4 overflow-x-auto pb-4">
            {history.map(item => <Link key={item.movieSlug} to={`/movie/${encodeURIComponent(item.movieSlug)}?episode=${encodeURIComponent(item.lastEpisodeSlug || '')}`} className="group w-60 shrink-0 overflow-hidden rounded-xl border border-white/10 bg-white/5">
                <div className="relative aspect-video"><img src={item.thumbUrl} alt={item.title} loading="lazy" className="h-full w-full object-cover" /><Play className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 text-white" />
                    <div className="absolute bottom-0 h-1 bg-neon" style={{ width: `${Math.min(100, Math.max(0, item.progressMs / (item.durationMs || 1) * 100))}%` }} /></div>
                <div className="p-3"><h3 className="truncate font-semibold text-white group-hover:text-neon">{item.title}</h3><p className="text-sm text-gray-400">{item.lastEpisodeName || 'Tiếp tục xem'}</p></div>
            </Link>)}
        </div>
    </section>;
}
