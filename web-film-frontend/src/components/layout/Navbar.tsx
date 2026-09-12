import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Menu, X, Play, User, LogOut, Bell, Clock, Trash2, ArrowRight, Star } from 'lucide-react';
import clsx from 'clsx';
import { useAuthStore } from '../../store/authStore';
import { useUnreadNotifications } from '../../hooks/useUnreadNotifications';
import { userService } from '../../services/user.service';
import { movieService } from '../../services/movie.service';
import { useDebounce } from '../../hooks/useDebounce';
import type { Movie } from '../../types';

const RECENT_SEARCHES_KEY = 'cinestream_recent_searches';
const MAX_RECENT_SEARCHES = 6;

export const Navbar = () => {
    const [scrolled, setScrolled] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isSearchOpen, setIsSearchOpen] = useState(false);
    const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [suggestions, setSuggestions] = useState<Movie[]>([]);
    const [isSearchingSuggestions, setIsSearchingSuggestions] = useState(false);
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [recentSearches, setRecentSearches] = useState<string[]>([]);

    const desktopSearchRef = useRef<HTMLDivElement>(null);
    const mobileSearchRef = useRef<HTMLDivElement>(null);
    const navigate = useNavigate();
    const { user, logout } = useAuthStore();
    const { data: unreadCount = 0 } = useUnreadNotifications();

    const debouncedSearch = useDebounce(searchQuery.trim(), 280);

    // Load recent searches from localStorage
    useEffect(() => {
        try {
            const saved = localStorage.getItem(RECENT_SEARCHES_KEY);
            if (saved) {
                setRecentSearches(JSON.parse(saved));
            }
        } catch (e) {
            console.error('Failed to parse recent searches', e);
        }
    }, []);

    // Save recent search
    const saveRecentSearch = (term: string) => {
        const cleaned = term.trim();
        if (!cleaned) return;
        setRecentSearches(prev => {
            const filtered = prev.filter(item => item.toLowerCase() !== cleaned.toLowerCase());
            const updated = [cleaned, ...filtered].slice(0, MAX_RECENT_SEARCHES);
            try {
                localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
            } catch (e) {
                console.error('Failed to save recent search', e);
            }
            return updated;
        });
    };

    // Remove single recent search
    const removeRecentSearch = (term: string, e: React.MouseEvent) => {
        e.stopPropagation();
        setRecentSearches(prev => {
            const updated = prev.filter(item => item !== term);
            localStorage.setItem(RECENT_SEARCHES_KEY, JSON.stringify(updated));
            return updated;
        });
    };

    // Clear all recent searches
    const clearAllRecentSearches = (e: React.MouseEvent) => {
        e.stopPropagation();
        setRecentSearches([]);
        localStorage.removeItem(RECENT_SEARCHES_KEY);
    };

    // Click outside listener to close dropdown
    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (
                desktopSearchRef.current && !desktopSearchRef.current.contains(e.target as Node) &&
                mobileSearchRef.current && !mobileSearchRef.current.contains(e.target as Node)
            ) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Fetch instant search suggestions
    useEffect(() => {
        let isCancelled = false;
        if (debouncedSearch.length >= 2) {
            setIsSearchingSuggestions(true);
            movieService.searchMovies(debouncedSearch, 1, 5)
                .then(res => {
                    if (!isCancelled) {
                        setSuggestions(res?.content || []);
                    }
                })
                .catch(() => {
                    if (!isCancelled) setSuggestions([]);
                })
                .finally(() => {
                    if (!isCancelled) setIsSearchingSuggestions(false);
                });
        } else {
            setSuggestions([]);
            setIsSearchingSuggestions(false);
        }
        return () => {
            isCancelled = true;
        };
    }, [debouncedSearch]);

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

    const handleSearch = (e?: React.FormEvent, customTerm?: string) => {
        if (e) e.preventDefault();
        const term = (customTerm !== undefined ? customTerm : searchQuery).trim();
        if (term) {
            saveRecentSearch(term);
            navigate(`/search?q=${encodeURIComponent(term)}`);
            setIsDropdownOpen(false);
            setIsSearchOpen(false);
            setIsMenuOpen(false);
        }
    };

    const handleSelectMovie = (slug: string, title: string) => {
        saveRecentSearch(title);
        setIsDropdownOpen(false);
        setIsSearchOpen(false);
        setIsMenuOpen(false);
        navigate(`/movie/${slug}`);
    };

    const navLinks = [
        { name: 'Trang chủ', path: '/' },
        { name: 'Phim bộ', path: '/series' },
        { name: 'Phim lẻ', path: '/movies' },
        { name: 'Mới & Phổ biến', path: '/popular' },
        { name: 'Phòng xem chung 🎬', path: '/watch-party' },
    ];

    // Autocomplete dropdown menu component
    const renderSearchDropdown = () => {
        if (!isDropdownOpen) return null;

        const hasRecent = recentSearches.length > 0;
        const hasSuggestions = suggestions.length > 0;
        const isQueryLongEnough = searchQuery.trim().length >= 2;

        if (!hasRecent && !isQueryLongEnough && !isSearchingSuggestions) {
            return null;
        }

        return (
            <div className="absolute top-full left-0 right-0 mt-2 bg-[#121418]/95 backdrop-blur-2xl border border-white/10 rounded-2xl shadow-2xl overflow-hidden z-50 animate-in fade-in slide-in-from-top-2 duration-200">
                {/* Recent Searches (shown when query is short or empty) */}
                {!isQueryLongEnough && hasRecent && (
                    <div className="p-3">
                        <div className="flex items-center justify-between px-3 py-1.5 text-xs text-text-secondary font-medium">
                            <span className="flex items-center gap-1.5 uppercase tracking-wider text-[11px] text-gray-400">
                                <Clock size={13} className="text-neon" /> Lịch sử tìm kiếm
                            </span>
                            <button
                                onClick={clearAllRecentSearches}
                                className="text-gray-500 hover:text-red-400 text-[11px] flex items-center gap-1 transition-colors"
                            >
                                <Trash2 size={11} /> Xóa tất cả
                            </button>
                        </div>
                        <div className="flex flex-wrap gap-1.5 mt-2 px-2">
                            {recentSearches.map(term => (
                                <div
                                    key={term}
                                    onClick={() => {
                                        setSearchQuery(term);
                                        handleSearch(undefined, term);
                                    }}
                                    className="group flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/5 hover:bg-neon/10 border border-white/5 hover:border-neon/30 text-xs text-gray-300 hover:text-neon cursor-pointer transition-all"
                                >
                                    <span>{term}</span>
                                    <button
                                        onClick={(e) => removeRecentSearch(term, e)}
                                        className="text-gray-500 group-hover:text-red-400 hover:scale-110 p-0.5 rounded-full transition-all"
                                    >
                                        <X size={12} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Instant Search Results */}
                {isQueryLongEnough && (
                    <div className="p-2">
                        <div className="px-3 py-2 text-[11px] uppercase tracking-wider text-gray-400 font-semibold flex items-center justify-between border-b border-white/5">
                            <span>Gợi ý phim nhanh</span>
                            {isSearchingSuggestions && (
                                <span className="text-neon flex items-center gap-1 text-[11px] lowercase">
                                    <span className="size-1.5 rounded-full bg-neon animate-ping"></span>
                                    đang tìm...
                                </span>
                            )}
                        </div>

                        {isSearchingSuggestions && !hasSuggestions ? (
                            <div className="py-6 text-center text-xs text-gray-400 flex items-center justify-center gap-2">
                                <span className="size-3 rounded-full border-2 border-neon border-t-transparent animate-spin"></span>
                                Đang tìm kiếm...
                            </div>
                        ) : hasSuggestions ? (
                            <div className="divide-y divide-white/5">
                                {suggestions.map(movie => (
                                    <div
                                        key={movie.id}
                                        onClick={() => handleSelectMovie(movie.slug, movie.title)}
                                        className="flex items-center gap-3 p-2.5 rounded-xl hover:bg-white/10 cursor-pointer transition-all group"
                                    >
                                        <img
                                            src={movie.thumbUrl || movie.posterUrl}
                                            alt={movie.title}
                                            className="w-10 h-14 rounded-md object-cover flex-shrink-0 border border-white/10 group-hover:border-neon/50 shadow"
                                        />
                                        <div className="flex-1 min-w-0">
                                            <h4 className="text-sm font-semibold text-white group-hover:text-neon transition-colors truncate">
                                                {movie.title}
                                            </h4>
                                            <p className="text-xs text-gray-400 truncate">
                                                {movie.originTitle || movie.director || 'CineStream'}
                                            </p>
                                            <div className="flex items-center gap-2 mt-1 text-[11px] text-gray-500">
                                                {movie.year && <span>{movie.year}</span>}
                                                {movie.quality && (
                                                    <span className="px-1.5 py-0.5 rounded bg-neon/10 text-neon font-medium border border-neon/20 text-[10px]">
                                                        {movie.quality}
                                                    </span>
                                                )}
                                                {movie.averageRating && movie.averageRating > 0 && (
                                                    <span className="flex items-center gap-0.5 text-amber-400">
                                                        <Star size={10} fill="currentColor" />
                                                        {movie.averageRating.toFixed(1)}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                        <ArrowRight size={16} className="text-gray-500 group-hover:text-neon group-hover:translate-x-0.5 transition-all opacity-0 group-hover:opacity-100 mr-1" />
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="py-6 text-center text-xs text-gray-500">
                                Không tìm thấy phim phù hợp với "{searchQuery}"
                            </div>
                        )}

                        {/* View all button */}
                        <button
                            type="button"
                            onClick={() => handleSearch()}
                            className="w-full mt-2 py-2.5 px-4 rounded-xl bg-neon/10 hover:bg-neon text-neon hover:text-obsidian font-bold text-xs flex items-center justify-center gap-1.5 transition-all"
                        >
                            <span>Xem tất cả kết quả cho "{searchQuery}"</span>
                            <ArrowRight size={14} />
                        </button>
                    </div>
                )}
            </div>
        );
    };

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
                        <div ref={desktopSearchRef} className="hidden lg:block relative group">
                            <form onSubmit={handleSearch} className="relative">
                                <input 
                                    type="text" 
                                    className="w-56 xl:w-72 bg-white/5 border border-white/10 rounded-full px-4 py-2 pl-10 text-sm focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all text-white placeholder-text-secondary" 
                                    placeholder="Tìm kiếm phim..."
                                    value={searchQuery}
                                    onFocus={() => setIsDropdownOpen(true)}
                                    onChange={(e) => {
                                        setSearchQuery(e.target.value);
                                        setIsDropdownOpen(true);
                                    }}
                                />
                                <Search className="absolute left-3 top-2.5 w-4 h-4 text-text-secondary group-focus-within:text-neon" />
                            </form>
                            {renderSearchDropdown()}
                        </div>

                        {/* Mobile Search Toggle */}
                        <button 
                            onClick={() => setIsSearchOpen(!isSearchOpen)}
                            className="lg:hidden p-2 text-white hover:text-neon transition-colors"
                        >
                            <Search size={22} />
                        </button>

                        {user && <Link to="/notifications" aria-label={`Thông báo${unreadCount ? `, ${unreadCount} chưa đọc` : ''}`} className="relative p-2 text-white hover:text-neon transition-colors">
                            <Bell size={22} />
                            {unreadCount > 0 && <span className="absolute -top-1 -right-1 min-w-4 h-4 px-1 rounded-full bg-neon text-obsidian text-[10px] font-bold flex items-center justify-center">{unreadCount > 99 ? '99+' : unreadCount}</span>}
                        </Link>}

                        {user ? (
                            <div className="relative">
                                <button 
                                    onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                                    className="flex items-center gap-2"
                                >
                                    <div className="size-8 md:size-10 rounded-full border border-neon bg-white/10 overflow-hidden flex items-center justify-center">
                                        {user.avatarUrl ? (
                                            <img src={userService.getAvatarUrl(user.avatarUrl)} alt="avatar" className="w-full h-full object-cover" />
                                        ) : (
                                            <User className="w-5 h-5 text-gray-300" />
                                        )}
                                    </div>
                                </button>

                                {isUserMenuOpen && (
                                    <div className="absolute right-0 mt-2 w-48 bg-surface border border-white/10 rounded-xl shadow-xl overflow-hidden animate-in slide-in-from-top-2">
                                        <Link to="/profile" className="flex items-center gap-2 px-4 py-3 text-sm text-white hover:bg-white/10 transition-colors">
                                            <User size={16} /> Hồ sơ
                                        </Link>
                                        <button 
                                            onClick={() => {
                                                logout();
                                                setIsUserMenuOpen(false);
                                                navigate('/');
                                            }}
                                            className="w-full flex items-center gap-2 px-4 py-3 text-sm text-red-400 hover:bg-white/10 transition-colors text-left"
                                        >
                                            <LogOut size={16} /> Đăng xuất
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <>
                                <Link to="/login" className="hidden md:flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-sm font-bold text-white transition-all backdrop-blur-md">
                                    Đăng nhập
                                </Link>
                                <button className="px-4 py-2 rounded-full bg-neon hover:bg-white hover:text-obsidian text-obsidian text-xs md:text-sm font-bold transition-all shadow-neon hover:shadow-white-glow whitespace-nowrap">
                                    Dùng thử <span className="hidden xs:inline">miễn phí</span>
                                </button>
                            </>
                        )}
                    </div>
                </div>

                {/* Mobile Search Overlay */}
                {isSearchOpen && (
                    <div ref={mobileSearchRef} className="absolute top-full left-0 right-0 bg-obsidian/95 backdrop-blur-xl border-b border-white/10 p-4 md:hidden animate-in slide-in-from-top duration-300">
                        <form onSubmit={handleSearch} className="relative">
                            <input 
                                autoFocus
                                type="text" 
                                className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 pl-12 text-sm focus:outline-none focus:border-neon text-white" 
                                placeholder="Tên phim, diễn viên..." 
                                value={searchQuery}
                                onFocus={() => setIsDropdownOpen(true)}
                                onChange={(e) => {
                                    setSearchQuery(e.target.value);
                                    setIsDropdownOpen(true);
                                }}
                            />
                            <Search className="absolute left-4 top-3.5 w-5 h-5 text-neon" />
                        </form>
                        {renderSearchDropdown()}
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
                        {user && <Link to="/notifications" onClick={() => setIsMenuOpen(false)} className="bg-white/5 border border-white/10 text-white w-full py-4 rounded-2xl flex items-center justify-center gap-2 font-bold text-lg">
                            <Bell size={20} /> Thông báo {unreadCount > 0 && <span className="text-neon">({unreadCount})</span>}
                        </Link>}
                        {user ? (
                            <>
                                <Link 
                                    to="/profile" 
                                    onClick={() => setIsMenuOpen(false)}
                                    className="bg-white/5 border border-white/10 text-white w-full py-4 rounded-2xl flex items-center justify-center font-bold text-lg"
                                >
                                    Hồ sơ của tôi
                                </Link>
                                <button 
                                    onClick={() => { logout(); setIsMenuOpen(false); navigate('/'); }}
                                    className="bg-red-500/20 text-red-400 border border-red-500/30 w-full py-4 rounded-2xl flex items-center justify-center font-bold text-lg"
                                >
                                    Đăng xuất
                                </button>
                            </>
                        ) : (
                            <>
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
                            </>
                        )}
                    </div>
                </nav>
            </div>
        </>
    );
};
