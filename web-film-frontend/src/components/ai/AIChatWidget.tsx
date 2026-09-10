import { useState, useRef, useEffect } from 'react';
import { aiService } from '../../services/ai.service';
import type { AIChatMessage } from '../../types';
import { X, Send, Loader2 } from 'lucide-react';

export const AIChatWidget = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [history, setHistory] = useState<AIChatMessage[]>([
        { 
            role: 'assistant', 
            content: 'Chào Senpai! Cine-chan có thể giúp gì cho anh hôm nay ạ? Anh hãy thử mô tả cốt truyện của bộ phim anh muốn tìm xem sao nhé!~ *mắt lấp lánh*' 
        }
    ]);
    const [isLoading, setIsLoading] = useState(false);
    const [expression, setExpression] = useState<'idle' | 'thinking' | 'success' | 'sad'>('idle');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [history, isOpen]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!query.trim() || isLoading) return;

        const newHistory: AIChatMessage[] = [...history, { role: 'user', content: query }];
        setHistory(newHistory);
        setQuery('');
        setIsLoading(true);
        setExpression('thinking');

        try {
            const response = await aiService.chat({ query, history: newHistory });
            
            // Adjust expression based on movie count and query type
            if (response.isMovieQuery) {
                if (response.movies?.content && response.movies.content.length > 0) {
                    setExpression('success');
                } else {
                    setExpression('sad');
                }
            } else {
                setExpression('idle');
            }

            setHistory([
                ...newHistory, 
                { 
                    role: 'assistant', 
                    content: response.aiMessage,
                    movies: response.movies?.content || []
                }
            ]);
        } catch {
            setExpression('sad');
            setHistory([
                ...newHistory, 
                { 
                    role: 'assistant', 
                    content: 'Huhu, hệ thống của Cine-chan đang gặp chút sự cố rồi Senpai ơi... Lát nữa anh thử lại nha!~ *khóc nhè*' 
                }
            ]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed bottom-6 right-6 z-50">
            {/* Chat Button */}
            {!isOpen && (
                <button 
                    onClick={() => {
                        setIsOpen(true);
                        setExpression('idle');
                    }}
                    className="size-16 rounded-full bg-neon text-obsidian flex items-center justify-center shadow-[0_0_20px_rgba(0,243,255,0.5)] hover:scale-110 transition-transform relative overflow-hidden group border border-neon/50"
                >
                    <img 
                        src="/mascot/cine_chan_idle.png" 
                        alt="Cine-chan"
                        className="w-12 h-12 object-cover group-hover:scale-110 transition-transform duration-300 drop-shadow-md"
                    />
                </button>
            )}

            {/* Chat Window */}
            {isOpen && (
                <div className="w-[350px] sm:w-[400px] h-[550px] max-h-[85vh] flex flex-col bg-surface border border-white/10 rounded-2xl shadow-2xl overflow-visible glass-card relative">
                    
                    {/* Cine-chan Mascot Floating Left (Desktop only) */}
                    <div className="absolute -left-[140px] bottom-0 w-[130px] select-none pointer-events-none transition-all duration-300 hidden md:block hover:scale-105">
                        <img 
                            src={`/mascot/cine_chan_${expression}.png`} 
                            alt="Cine-chan mascot"
                            className="w-full h-auto drop-shadow-[0_0_25px_rgba(0,243,255,0.4)] animate-pulse-slow"
                        />
                    </div>

                    {/* Header */}
                    <div className="flex items-center justify-between px-4 py-3 bg-white/5 border-b border-white/10 rounded-t-2xl">
                        <div className="flex items-center gap-2.5">
                            <div className="size-8 rounded-full border border-neon/30 overflow-hidden shrink-0 bg-neon/10 flex items-center justify-center">
                                <img 
                                    src={`/mascot/cine_chan_${expression}.png`} 
                                    alt="Cine-chan Avatar"
                                    className="w-7 h-7 object-cover drop-shadow-sm"
                                />
                            </div>
                            <div>
                                <h3 className="font-bold text-white text-sm leading-tight">Cine-chan</h3>
                                <span className="text-[10px] text-neon/80 font-medium">Trợ lý AI đáng yêu</span>
                            </div>
                        </div>
                        <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-white transition-colors p-1.5 rounded-full hover:bg-white/5">
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Messages Area */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
                        {history.map((msg, idx) => (
                            <div key={idx} className={`flex gap-2.5 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                {msg.role === 'assistant' && (
                                    <div className="size-8 rounded-full border border-neon/20 bg-neon/5 flex items-center justify-center shrink-0 overflow-hidden">
                                        <img 
                                            src="/mascot/cine_chan_idle.png" 
                                            alt="Cine-chan"
                                            className="w-7 h-7 object-cover"
                                        />
                                    </div>
                                )}
                                <div className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed max-w-[82%] ${
                                    msg.role === 'user' 
                                        ? 'bg-neon text-obsidian rounded-tr-none font-medium shadow-[0_2px_10px_rgba(0,243,255,0.2)]' 
                                        : 'bg-white/10 text-white rounded-tl-none border border-white/5'
                                }`}>
                                    <p className="whitespace-pre-wrap">{msg.content}</p>

                                    {/* Movie Cards Row (Horizontal Scroll) */}
                                    {msg.role === 'assistant' && msg.movies && msg.movies.length > 0 && (
                                        <div className="mt-3 flex gap-3 overflow-x-auto pb-2 scrollbar-thin scrollbar-thumb-neon/30 scrollbar-track-transparent">
                                            {msg.movies.map((movie) => (
                                                <a 
                                                    key={movie.id} 
                                                    href={`/movie/${movie.slug}`}
                                                    className="w-24 shrink-0 flex flex-col bg-obsidian/60 border border-white/10 rounded-xl overflow-hidden hover:border-neon hover:bg-obsidian transition-all group"
                                                >
                                                    <div className="relative aspect-[2/3] w-full overflow-hidden bg-white/5">
                                                        <img 
                                                            src={movie.thumbUrl} 
                                                            alt={movie.title}
                                                            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                                                        />
                                                    </div>
                                                    <div className="p-1.5 flex-1 flex flex-col justify-between">
                                                        <span className="text-[10px] font-medium text-white line-clamp-2 group-hover:text-neon transition-colors leading-tight">
                                                            {movie.title}
                                                        </span>
                                                        <span className="text-[9px] text-gray-400 mt-1 block">
                                                            {movie.year} • {movie.quality}
                                                        </span>
                                                    </div>
                                                </a>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                        {isLoading && (
                            <div className="flex gap-2.5 justify-start animate-pulse">
                                <div className="size-8 rounded-full border border-neon/20 bg-neon/5 flex items-center justify-center shrink-0 overflow-hidden">
                                    <img 
                                        src={`/mascot/cine_chan_${expression}.png`} 
                                        alt="Cine-chan"
                                        className="w-7 h-7 object-cover"
                                    />
                                </div>
                                <div className="px-4 py-3 rounded-2xl bg-white/10 rounded-tl-none border border-white/5 flex items-center gap-2">
                                    <Loader2 className="w-4 h-4 text-neon animate-spin" />
                                    <span className="text-sm text-gray-300">Cine-chan đang nghĩ nha...</span>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <div className="p-3 bg-white/5 border-t border-white/10 rounded-b-2xl">
                        <form onSubmit={handleSubmit} className="flex gap-2 relative">
                            <input 
                                type="text" 
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                placeholder="Hãy mô tả phim anh muốn tìm nha Senpai..."
                                className="w-full bg-obsidian border border-white/10 rounded-full pl-4 pr-12 py-2.5 text-sm text-white focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all"
                            />
                            <button 
                                type="submit" 
                                disabled={isLoading || !query.trim()}
                                className="absolute right-1.5 top-1.5 bottom-1.5 size-8 flex items-center justify-center rounded-full bg-neon text-obsidian hover:bg-white transition-colors disabled:opacity-50"
                            >
                                <Send className="w-4 h-4" />
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};
