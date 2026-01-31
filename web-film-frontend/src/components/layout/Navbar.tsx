import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search } from 'lucide-react';
import clsx from 'clsx';

export const Navbar = () => {
    const [scrolled, setScrolled] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 20);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    return (
        <header className={clsx(
            "fixed top-0 left-0 right-0 z-50 transition-all duration-300",
            scrolled ? "glass-nav h-16" : "h-20 bg-gradient-to-b from-obsidian/80 to-transparent"
        )}>
            <div className="max-w-[1600px] mx-auto px-6 h-full flex items-center justify-between gap-8">
                <Link to="/" className="flex items-center gap-2 cursor-pointer group">
                    <div className="size-8 text-neon group-hover:text-white transition-colors duration-300 drop-shadow-[0_0_8px_rgba(0,243,255,0.6)]">
                        <svg fill="currentColor" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                            <path clipRule="evenodd" fillRule="evenodd" d="M39.475 21.6262C40.358 21.4363 40.6863 21.5589 40.7581 21.5934C40.7876 21.655 40.8547 21.857 40.8082 22.3336C40.7408 23.0255 40.4502 24.0046 39.8572 25.2301C38.6799 27.6631 36.5085 30.6631 33.5858 33.5858C30.6631 36.5085 27.6632 38.6799 25.2301 39.8572C24.0046 40.4502 23.0255 40.7407 22.3336 40.8082C21.8571 40.8547 21.6551 40.7875 21.5934 40.7581C21.5589 40.6863 21.4363 40.358 21.6262 39.475C21.8562 38.4054 22.4689 36.9657 23.5038 35.2817C24.7575 33.2417 26.5497 30.9744 28.7621 28.762C30.9744 26.5497 33.2417 24.7574 35.2817 23.5037C36.9657 22.4689 38.4054 21.8562 39.475 21.6262ZM4.41189 29.2403L18.7597 43.5881C19.8813 44.7097 21.4027 44.9179 22.7217 44.7893C24.0585 44.659 25.5148 44.1631 26.9723 43.4579C29.9052 42.0387 33.2618 39.5667 36.4142 36.4142C39.5667 33.2618 42.0387 29.9052 43.4579 26.9723C44.1631 25.5148 44.659 24.0585 44.7893 22.7217C44.9179 21.4027 44.7097 19.8813 43.5881 18.7597L29.2403 4.41187C27.8527 3.02428 25.8765 3.02573 24.2861 3.36776C22.6081 3.72863 20.7334 4.58419 18.8396 5.74801C16.4978 7.18716 13.9881 9.18353 11.5858 11.5858C9.18354 13.988 7.18717 16.4978 5.74802 18.8396C4.58421 20.7334 3.72865 22.6081 3.36778 24.2861C3.02574 25.8765 3.02429 27.8527 4.41189 29.2403Z" />
                        </svg>
                    </div>
                    <h1 className="text-white text-2xl font-serif font-black tracking-tight">Cine<span className="text-neon">Stream</span></h1>
                </Link>

                {/* Desktop Menu */}
                <div className="hidden md:flex items-center space-x-8">
                    <Link to="/" className="text-sm font-medium hover:text-neon transition-colors">Trang chủ</Link>
                    <Link to="/series" className="text-sm font-medium hover:text-neon transition-colors">Phim bộ</Link>
                    <Link to="/movies" className="text-sm font-medium hover:text-neon transition-colors">Phim lẻ</Link>
                    <Link to="/popular" className="text-sm font-medium hover:text-neon transition-colors">Mới & Phổ biến</Link>
                </div>

                <div className="flex items-center gap-4">
                    <div className="hidden md:flex relative group">
                        <input 
                            type="text" 
                            className="w-64 bg-white/5 border border-white/10 rounded-full px-4 py-2 pl-10 text-sm focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all text-white placeholder-text-secondary" 
                            placeholder="Tìm kiếm phim..." 
                        />
                        <Search className="absolute left-3 top-2.5 w-4 h-4 text-text-secondary group-focus-within:text-neon" />
                    </div>
                    <Link to="/login" className="hidden sm:flex items-center gap-2 px-5 py-2 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-sm font-bold text-white transition-all backdrop-blur-md">
                        Đăng nhập
                    </Link>
                    <button className="px-5 py-2 rounded-full bg-neon hover:bg-white hover:text-obsidian text-obsidian text-sm font-bold transition-all shadow-[0_0_15px_rgba(0,243,255,0.4)] hover:shadow-[0_0_25px_rgba(255,255,255,0.6)]">
                        Dùng thử miễn phí
                    </button>
                </div>
            </div>
        </header>
    );
};
