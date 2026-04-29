import { useState, useRef, useEffect } from 'react';
import { aiService } from '../../services/ai.service';
import type { AIChatMessage } from '../../types';
import { Bot, X, Send, User, Loader2 } from 'lucide-react';

export const AIChatWidget = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [history, setHistory] = useState<AIChatMessage[]>([
        { role: 'assistant', content: 'Chào bạn! Tôi là CineGuru. Bạn muốn xem thể loại phim gì hôm nay?' }
    ]);
    const [isLoading, setIsLoading] = useState(false);
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

        try {
            const response = await aiService.chat({ query, history: newHistory });
            setHistory([...newHistory, { role: 'assistant', content: response.reply }]);
        } catch (error) {
            setHistory([...newHistory, { role: 'assistant', content: 'Xin lỗi, hiện tại tôi đang gặp chút sự cố kết nối. Vui lòng thử lại sau nhé!' }]);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed bottom-6 right-6 z-50">
            {/* Chat Button */}
            {!isOpen && (
                <button 
                    onClick={() => setIsOpen(true)}
                    className="size-14 rounded-full bg-neon text-obsidian flex items-center justify-center shadow-[0_0_20px_rgba(0,243,255,0.4)] hover:scale-110 transition-transform"
                >
                    <Bot className="w-7 h-7" />
                </button>
            )}

            {/* Chat Window */}
            {isOpen && (
                <div className="w-[350px] sm:w-[400px] h-[500px] max-h-[80vh] flex flex-col bg-surface border border-white/10 rounded-2xl shadow-2xl overflow-hidden glass-card">
                    {/* Header */}
                    <div className="flex items-center justify-between px-4 py-3 bg-white/5 border-b border-white/10">
                        <div className="flex items-center gap-2">
                            <Bot className="w-5 h-5 text-neon" />
                            <h3 className="font-bold text-white">AI CineGuru</h3>
                        </div>
                        <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-white transition-colors">
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Messages Area */}
                    <div className="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
                        {history.map((msg, idx) => (
                            <div key={idx} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                {msg.role === 'assistant' && (
                                    <div className="size-8 rounded-full bg-neon/20 flex items-center justify-center shrink-0">
                                        <Bot className="w-4 h-4 text-neon" />
                                    </div>
                                )}
                                <div className={`px-4 py-2 rounded-2xl text-sm ${msg.role === 'user' ? 'bg-neon text-obsidian rounded-br-sm' : 'bg-white/10 text-white rounded-bl-sm'} max-w-[80%]`}>
                                    <p className="whitespace-pre-wrap">{msg.content}</p>
                                </div>
                            </div>
                        ))}
                        {isLoading && (
                            <div className="flex gap-3 justify-start">
                                <div className="size-8 rounded-full bg-neon/20 flex items-center justify-center shrink-0">
                                    <Bot className="w-4 h-4 text-neon" />
                                </div>
                                <div className="px-4 py-3 rounded-2xl bg-white/10 rounded-bl-sm flex items-center gap-2">
                                    <Loader2 className="w-4 h-4 text-neon animate-spin" />
                                    <span className="text-sm text-gray-300">Đang nghĩ...</span>
                                </div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <div className="p-3 bg-white/5 border-t border-white/10">
                        <form onSubmit={handleSubmit} className="flex gap-2 relative">
                            <input 
                                type="text" 
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                placeholder="Hỏi phim (VD: phim hành động Mỹ)..."
                                className="w-full bg-obsidian border border-white/10 rounded-full pl-4 pr-12 py-2.5 text-sm text-white focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all"
                            />
                            <button 
                                type="submit" 
                                disabled={isLoading || !query.trim()}
                                className="absolute right-1 top-1 bottom-1 size-8 flex items-center justify-center rounded-full bg-neon text-obsidian hover:bg-white transition-colors disabled:opacity-50"
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
