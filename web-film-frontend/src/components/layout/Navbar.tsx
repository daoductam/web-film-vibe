import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Menu, X, Play } from 'lucide-react';
import clsx from 'clsx';

export const Navbar = () => {
    const [scrolled, setScrolled] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isSearchOpen, setIsSearchOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 20);
        };
        window.addEventListener('scroll', handleScroll);
        
        // Prevent body scroll when menu is open
        if (isMenuOpen) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }

        return () => {
            window.removeEventListener('scroll', handleScroll);
            document.body.style.overflow = 'unset';
        };
    }, [isMenuOpen]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
            setIsSearchOpen(false);
            setIsMenuOpen(false);
        }
    };

    const navLinks = [
        { name: 'Trang chủ', path: '/' },
        { name: 'Phim bộ', path: '/series' },
        { name: 'Phim lẻ', path: '/movies' },
        { name: 'Mới & Phổ biến', path: '/popular' },
    ];

    return (
        <>
            <header className={clsx(
                "fixed top-0 left-0 right-0 z-[100] transition-all duration-300",
                scrolled || isMenuOpen ? "glass-nav h-16" : "h-20 bg-gradient-to-b from-obsidian/80 to-transparent"
            )}>
                <div className="max-w-[1600px] mx-auto px-4 md:px-6 h-full flex items-center justify-between gap-4">
                    {/* Logo Section */}
                    <div className="flex items-center gap-4">
                        {/* Mobile Menu Toggle */}
                        <button 
                            onClick={() => setIsMenuOpen(!isMenuOpen)}
                            className="md:hidden p-2 text-white hover:text-neon transition-colors"
                        >
                            {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
                        </button>

                        <Link to="/" className="flex items-center gap-2 cursor-pointer group">
                            <div className="size-8 text-neon group-hover:text-white transition-colors duration-300 drop-shadow-[0_0_8px_rgba(0,243,255,0.6)]">
                                <svg fill="currentColor" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                                    <path clipRule="evenodd" fillRule="evenodd" d="M39.475 21.6262C40.358 21.4363 40.6863 21.5589 40.7581 21.5934C40.7876 21.655 40.8547 21.857 40.8082 22.3336C40.7408 23.0255 40.4502 24.0046 39.8572 25.2301C38.6799 27.6631 36.5085 30.6631 33.5858 33.5858C30.6631 36.5085 27.6632 38.6799 25.2301 39.8572C24.0046 40.4502 23.0255 40.7407 22.3336 40.8082C21.8571 40.8547 21.6551 40.7875 21.5934 40.7581C21.5589 40.6863 21.4363 40.358 21.6262 39.475C21.8562 38.4054 22.4689 36.9657 23.5038 35.2817C24.7575 33.2417 26.5497 30.9744 28.7621 28.762C30.9744 26.5497 33.2417 24.7574 35.2817 23.5037C36.9657 22.4689 38.4054 21.8562 39.475 21.6262ZM4.41189 29.2403L18.7597 43.5881C19.8813 44.7097 21.4027 44.9179 22.7217 44.7893C24.0585 44.659 25.5148 44.1631 26.9723 43.4579C29.9052 42.0387 33.2618 39.5667 36.4142 36.4142C39.5667 33.2618 42.0387 29.9052 43.4579 26.9723C44.1631 25.5148 44.659 24.0585 44.7893 22.7217C44.9179 21.4027 44.7097 19.8813 43.5881 18.7597L29.2403 4.41187C27.8527 3.02428 25.8765 3.02573 24.2861 3.36776C22.6081 3.72863 20.7334 4.58419 18.8396 5.74801C16.4978 7.18716 13.9881 9.18353 11.5858 11.5858C9.18354 13.988 7.18717 16.4978 5.74802 18.8396C4.58421 20.7334 3.72865 22.6081 3.36778 24.2861C3.02574 25.8765 3.02429 27.8527 4.41189 29.2403Z" />
                                </svg>
                            </div>
                            <h1 className="text-white text-xl md:text-2xl font-serif font-black tracking-tight">Cine<span className="text-neon">Stream</span></h1>
                        </Link>
                    </div>

                    {/* Desktop Menu */}
                    <nav className="hidden md:flex items-center space-x-8">
                        {navLinks.map((link) => (
                            <Link key={link.path} to={link.path} className="text-sm font-medium hover:text-neon transition-colors">
                                {link.name}
                            </Link>
                        ))}
                    </nav>

                    {/* Actions */}
                    <div className="flex items-center gap-2 md:gap-4">
                        {/* Search Bar (Desktop) */}
                        <form onSubmit={handleSearch} className="hidden lg:flex relative group">
                            <input 
                                type="text" 
                                className="w-48 xl:w-64 bg-white/5 border border-white/10 rounded-full px-4 py-2 pl-10 text-sm focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all text-white placeholder-text-secondary" 
                                placeholder="Tìm kiếm phim..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                            <Search className="absolute left-3 top-2.5 w-4 h-4 text-text-secondary group-focus-within:text-neon" />
                        </form>

                        {/* Mobile Search Toggle */}
                        <button 
                            onClick={() => setIsSearchOpen(!isSearchOpen)}
                            className="lg:hidden p-2 text-white hover:text-neon transition-colors"
                        >
                            <Search size={22} />
                        </button>

                        <Link to="/login" className="hidden md:flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-sm font-bold text-white transition-all backdrop-blur-md">
                            Đăng nhập
                        </Link>
                        <button className="px-4 py-2 rounded-full bg-neon hover:bg-white hover:text-obsidian text-obsidian text-xs md:text-sm font-bold transition-all shadow-neon hover:shadow-white-glow whitespace-nowrap">
                            Dùng thử <span className="hidden xs:inline">miễn phí</span>
                        </button>
                    </div>
                </div>

                {/* Mobile Search Overlay */}
                {isSearchOpen && (
                    <div className="absolute top-full left-0 right-0 bg-obsidian/95 backdrop-blur-xl border-b border-white/10 p-4 md:hidden animate-in slide-in-from-top duration-300">
                        <form onSubmit={handleSearch} className="relative">
                            <input 
                                autoFocus
                                type="text" 
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 pl-12 text-sm focus:outline-none focus:border-neon text-white" 
                                placeholder="Tên phim, diễn viên..." 
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                            <Search className="absolute left-4 top-3.5 w-5 h-5 text-neon" />
                        </form>
                    </div>
                )}
            </header>

            {/* Mobile Navigation Menu - Moved outside header for absolute stacking */}
            <div className={clsx(
                "fixed inset-0 bg-[#050505] z-[9999] md:hidden flex flex-col transition-all duration-500",
                isMenuOpen ? "translate-y-0 opacity-100" : "-translate-y-full opacity-0 pointer-events-none"
            )}>
                {/* Mobile Menu Header */}
                <div className="h-16 flex items-center justify-between px-4 border-b border-white/10 shrink-0">
                    <Link to="/" onClick={() => setIsMenuOpen(false)} className="flex items-center gap-2">
                         <div className="size-8 text-neon">
                            <svg fill="currentColor" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                                <path d="M39.475 21.6262C40.358 21.4363 40.6863 21.5589 40.7581 21.5934C40.7876 21.655 40.8547 21.857 40.8082 22.3336C40.7408 23.0255 40.4502 24.0046 39.8572 25.2301C38.6799 27.6631 36.5085 30.6631 33.5858 33.5858C30.6631 36.5085 27.6632 38.6799 25.2301 39.8572C24.0046 40.4502 23.0255 40.7407 22.3336 40.8082C21.8571 40.8547 21.6551 40.7875 21.5934 40.7581C21.5589 40.6863 21.4363 40.358 21.6262 39.475C21.8562 38.4054 22.4689 36.9657 23.5038 35.2817C24.7575 33.2417 26.5497 30.9744 28.7621 28.762C30.9744 26.5497 33.2417 24.7574 35.2817 23.5037C36.9657 22.4689 38.4054 21.8562 39.475 21.6262ZM4.41189 29.2403L18.7597 43.5881C19.8813 44.7097 21.4027 44.9179 22.7217 44.7893C24.0585 44.659 25.5148 44.1631 26.9723 43.4579C29.9052 42.0387 33.2618 39.5667 36.4142 36.4142C39.5667 33.2618 42.0387 29.9052 43.4579 26.9723C44.1631 25.5148 44.659 24.0585 44.7893 22.7217C44.9179 21.4027 44.7097 19.8813 43.5881 18.7597L29.2403 4.41187C27.8527 3.02428 25.8765 3.02573 24.2861 3.36776C22.6081 3.72863 20.7334 4.58419 18.8396 5.74801C16.4978 7.18716 13.9881 9.18353 11.5858 11.5858C9.18354 13.988 7.18717 16.4978 5.74802 18.8396C4.58421 20.7334 3.72865 22.6081 3.36778 24.2861C3.02574 25.8765 3.02429 27.8527 4.41189 29.2403Z" />
                            </svg>
                        </div>
                        <h1 className="text-white text-xl font-serif font-black tracking-tight">Cine<span className="text-neon">Stream</span></h1>
                    </Link>
                    <button onClick={() => setIsMenuOpen(false)} className="p-2 text-neon">
                        <X size={28} />
                    </button>
                </div>

                {/* Mobile Menu Content */}
                <nav className="flex-1 overflow-y-auto p-6 flex flex-col bg-[#050505]">
                    <div className="space-y-4">
                        {navLinks.map((link, i) => (
                            <Link 
                                key={link.path} 
                                to={link.path} 
                                onClick={() => setIsMenuOpen(false)}
                                className={clsx(
                                    "text-3xl font-serif font-black text-white hover:text-neon transition-all flex items-center justify-between py-4 border-b border-white/5",
                                    isMenuOpen ? "translate-x-0 opacity-100" : "-translate-x-10 opacity-0"
                                )}
                                style={{ transitionDelay: `${i * 50}ms` }}
                            >
                                {link.name}
                                <Play size={24} className="text-neon" />
                            </Link>
                        ))}
                    </div>

                    <div className="mt-auto space-y-4 pt-10">
                        <Link 
                            to="/login" 
                            onClick={() => setIsMenuOpen(false)}
                            className="bg-white/5 border border-white/10 text-white w-full py-4 rounded-2xl flex items-center justify-center font-bold text-lg"
                        >
                            Đăng nhập thành viên
                        </Link>
                        <button className="bg-neon text-obsidian w-full py-4 rounded-2xl flex items-center justify-center font-black text-lg shadow-neon">
                            Dùng thử miễn phí
                        </button>
                    </div>
                </nav>
            </div>
        </>
    );
};
