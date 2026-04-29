import { Users, Film, Play, Star } from 'lucide-react';

export const DashboardPage = () => {
    const stats = [
        { name: 'Tổng số phim', value: '1,234', icon: Film, color: 'text-blue-400', bg: 'bg-blue-400/10' },
        { name: 'Tập phim', value: '45,678', icon: Play, color: 'text-neon', bg: 'bg-neon/10' },
        { name: 'Người dùng', value: '8,901', icon: Users, color: 'text-green-400', bg: 'bg-green-400/10' },
        { name: 'Đánh giá', value: '3,456', icon: Star, color: 'text-yellow-400', bg: 'bg-yellow-400/10' },
    ];

    return (
        <div className="space-y-8">
            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {stats.map((stat, i) => {
                    const Icon = stat.icon;
                    return (
                        <div key={i} className="glass-card rounded-2xl p-6 flex items-center gap-4">
                            <div className={`size-14 rounded-xl flex items-center justify-center shrink-0 ${stat.bg} ${stat.color}`}>
                                <Icon className="w-7 h-7" />
                            </div>
                            <div>
                                <p className="text-sm font-medium text-gray-400">{stat.name}</p>
                                <p className="text-2xl font-bold text-white">{stat.value}</p>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Quick Actions & Recent Info Placeholder */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="glass-card rounded-2xl p-6 border border-white/5">
                    <h3 className="text-lg font-bold text-white mb-4">Phim mới cập nhật</h3>
                    <div className="space-y-4">
                        {[1,2,3].map(i => (
                            <div key={i} className="flex items-center gap-4 p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-colors cursor-pointer">
                                <div className="w-12 h-16 bg-gray-800 rounded flex-shrink-0 animate-pulse"></div>
                                <div>
                                    <h4 className="text-sm font-bold text-white">Movie Title {i}</h4>
                                    <p className="text-xs text-neon mt-1">Tập {i*2} mới nhất</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="glass-card rounded-2xl p-6 border border-white/5">
                    <h3 className="text-lg font-bold text-white mb-4">Lịch sử hệ thống</h3>
                    <div className="space-y-4">
                        {[1,2,3].map(i => (
                            <div key={i} className="flex items-center gap-4 p-3 border-l-2 border-neon pl-4">
                                <div>
                                    <p className="text-sm text-gray-300">Admin đã cập nhật cấu hình Crawler.</p>
                                    <p className="text-xs text-gray-500 mt-1">2 giờ trước</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};
