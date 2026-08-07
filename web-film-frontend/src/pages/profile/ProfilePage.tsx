import { useState, useEffect } from 'react';
import { useAuthStore } from '../../store/authStore';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { FavoritesTab } from './FavoritesTab';
import { HistoryTab } from './HistoryTab';
import { SettingsTab } from './SettingsTab';
import { useNavigate } from 'react-router-dom';

export const ProfilePage = () => {
    const { user, token } = useAuthStore();
    const [activeTab, setActiveTab] = useState<'favorites' | 'history' | 'settings'>('favorites');
    const navigate = useNavigate();

    useEffect(() => {
        if (!token || !user) {
            navigate('/login');
        }
    }, [token, user, navigate]);

    if (!token || !user) {
        return <div className="min-h-screen bg-obsidian flex items-center justify-center text-white font-bold">Đang tải...</div>;
    }

    return (
        <div className="bg-obsidian min-h-screen text-text-primary">
            <Navbar />
            
            <main className="pt-28 pb-20 max-w-7xl mx-auto px-4 md:px-6">
                <div className="flex flex-col md:flex-row gap-8">
                    {/* Sidebar / Profile Info */}
                    <div className="md:w-1/4 shrink-0">
                        <div className="glass-card rounded-2xl p-6 text-center space-y-4">
                            <div className="size-24 rounded-full mx-auto bg-white/10 border-2 border-neon overflow-hidden">
                                {user.avatarUrl ? (
                                    <img src={`http://localhost:8081/api/v1/users/avatars/${user.avatarUrl}`} alt={user.fullName} className="w-full h-full object-cover" />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-3xl font-bold text-gray-500">
                                        {user.fullName?.charAt(0) || user.username.charAt(0)}
                                    </div>
                                )}
                            </div>
                            <div>
                                <h2 className="text-xl font-bold text-white">{user.fullName || user.username}</h2>
                                <p className="text-sm text-text-secondary">{user.email}</p>
                            </div>

                            <div className="flex flex-col gap-2 pt-4 border-t border-white/10 text-left">
                                <button 
                                    onClick={() => setActiveTab('favorites')}
                                    className={`px-4 py-3 rounded-xl font-bold transition-all ${activeTab === 'favorites' ? 'bg-neon text-obsidian' : 'text-gray-300 hover:bg-white/10'}`}
                                >
                                    Phim yêu thích
                                </button>
                                <button 
                                    onClick={() => setActiveTab('history')}
                                    className={`px-4 py-3 rounded-xl font-bold transition-all ${activeTab === 'history' ? 'bg-neon text-obsidian' : 'text-gray-300 hover:bg-white/10'}`}
                                >
                                    Lịch sử xem phim
                                </button>
                                <button 
                                    onClick={() => setActiveTab('settings')}
                                    className={`px-4 py-3 rounded-xl font-bold transition-all ${activeTab === 'settings' ? 'bg-neon text-obsidian' : 'text-gray-300 hover:bg-white/10'}`}
                                >
                                    Cài đặt tài khoản
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Main Content Area */}
                    <div className="md:w-3/4 flex-1">
                        <div className="glass-card rounded-2xl p-6 min-h-[500px]">
                            {activeTab === 'favorites' && <FavoritesTab />}
                            {activeTab === 'history' && <HistoryTab />}
                            {activeTab === 'settings' && <SettingsTab />}
                        </div>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
};
