import { Navigate, Outlet, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { LayoutDashboard, Film, Database, Users, LogOut } from 'lucide-react';

export const AdminLayout = () => {
    const { user, token, logout } = useAuthStore();
    const location = useLocation();

    // Check if user is logged in and is ADMIN
    if (!token || !user) {
        return <Navigate to="/login" replace />;
    }
    
    // In our backend, role might be 'ROLE_ADMIN' or just 'ADMIN'
    if (user.role !== 'ROLE_ADMIN' && user.role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    const navigation = [
        { name: 'Tổng quan', path: '/admin', icon: LayoutDashboard },
        { name: 'Quản lý Phim', path: '/admin/movies', icon: Film },
        { name: 'Crawl Dữ liệu', path: '/admin/crawl', icon: Database },
        { name: 'Người dùng', path: '/admin/users', icon: Users },
    ];

    return (
        <div className="flex h-screen bg-obsidian text-text-primary font-sans">
            {/* Sidebar */}
            <aside className="w-64 bg-surface border-r border-white/10 flex flex-col shrink-0">
                <div className="h-20 flex items-center px-6 border-b border-white/10 shrink-0">
                    <Link to="/" className="text-xl font-serif font-black tracking-tight text-white flex items-center gap-2">
                        <div className="size-8 bg-neon rounded flex items-center justify-center text-obsidian">A</div>
                        AdminPanel
                    </Link>
                </div>
                
                <nav className="flex-1 py-6 px-4 space-y-2 overflow-y-auto custom-scrollbar">
                    {navigation.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname === item.path;
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-colors ${isActive ? 'bg-neon text-obsidian font-bold' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}
                            >
                                <Icon className="w-5 h-5" />
                                {item.name}
                            </Link>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-white/10 shrink-0">
                    <button 
                        onClick={() => logout()}
                        className="flex items-center gap-3 w-full px-4 py-3 text-red-400 hover:bg-red-500/10 rounded-xl transition-colors font-medium"
                    >
                        <LogOut className="w-5 h-5" />
                        Đăng xuất
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col overflow-hidden">
                <header className="h-20 bg-surface border-b border-white/10 flex items-center justify-between px-8 shrink-0">
                    <h2 className="text-xl font-bold text-white">
                        {navigation.find(n => n.path === location.pathname)?.name || 'Admin'}
                    </h2>
                    <div className="flex items-center gap-3">
                        <span className="text-sm text-gray-400">Xin chào, <strong className="text-white">{user.fullName || user.username}</strong></span>
                        <div className="size-10 rounded-full bg-neon/20 flex items-center justify-center text-neon font-bold border border-neon/50">
                            {user.fullName?.charAt(0) || user.username.charAt(0)}
                        </div>
                    </div>
                </header>
                
                <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};
