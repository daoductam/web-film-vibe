import React, { useState, useEffect } from 'react';
import { MessageSquare, Send } from 'lucide-react';
import { Comment } from '../../types';
import { socialService } from '../../services/social.service';
import CommentItem from './CommentItem';
import { motion, AnimatePresence } from 'framer-motion';

interface CommentSectionProps {
    movieSlug: string;
    episodeSlug: string;
}

const CommentSection: React.FC<CommentSectionProps> = ({ movieSlug, episodeSlug }) => {
    const [comments, setComments] = useState<Comment[]>([]);
    const [newCommentContent, setNewCommentContent] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(false);
    const [totalComments, setTotalComments] = useState(0);

    useEffect(() => {
        fetchComments(0, true);
    }, [episodeSlug]);

    const fetchComments = async (pageNum: number, refresh = false) => {
        setLoading(true);
        try {
            const response = await socialService.getCommentsByEpisode(episodeSlug, pageNum);
            if (response.success) {
                const newComments = response.data.content;
                setComments(prev => refresh ? newComments : [...prev, ...newComments]);
                setHasMore(response.data.page ? response.data.page.number < response.data.page.totalPages - 1 : false);
                setTotalComments(response.data.page ? response.data.page.totalElements : 0);
                setPage(pageNum);
            }
        } catch (error) {
            console.error('Failed to fetch comments:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddComment = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newCommentContent.trim() || isSubmitting) return;

        setIsSubmitting(true);
        try {
            const response = await socialService.addComment({
                movieSlug,
                episodeSlug,
                content: newCommentContent
            });
            if (response.success) {
                setComments(prev => [response.data, ...prev]);
                setNewCommentContent('');
                setTotalComments(prev => prev + 1);
            }
        } catch (error) {
            console.error('Failed to add comment:', error);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleReplySuccess = (parentComment: Comment, newReply: Comment) => {
        setComments(prev => prev.map(c => {
            if (c.id === parentComment.id) {
                return {
                    ...c,
                    replies: [...(c.replies || []), newReply]
                };
            }
            return c;
        }));
    };

    const handleDeleteSuccess = (commentId: number) => {
        setComments(prev => {
            // Check if it's a main comment
            const existsInMain = prev.find(c => c.id === commentId);
            if (existsInMain) {
                return prev.filter(c => c.id !== commentId);
            }
            // Check in replies
            return prev.map(c => ({
                ...c,
                replies: (c.replies || []).filter(r => r.id !== commentId)
            }));
        });
        setTotalComments(prev => prev - 1);
    };

    return (
        <div className="bg-gray-950/50 rounded-xl border border-gray-900 overflow-hidden">
            <div className="p-6 border-b border-gray-900 bg-gray-900/30 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <MessageSquare className="text-indigo-500" size={20} />
                    <h3 className="font-bold text-lg text-gray-100">Bình luận</h3>
                    <span className="bg-gray-800 text-gray-400 text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wider">
                        {totalComments} Thảo luận
                    </span>
                </div>
            </div>

            <div className="p-6">
                {/* Main Input */}
                <form onSubmit={handleAddComment} className="mb-8 p-4 bg-gray-900/50 rounded-xl border border-gray-800 focus-within:border-indigo-500/50 transition-all shadow-inner">
                    <textarea 
                        value={newCommentContent}
                        onChange={(e) => setNewCommentContent(e.target.value)}
                        placeholder="Tham gia thảo luận về tập phim này..."
                        className="w-full bg-transparent border-none text-gray-200 placeholder-gray-600 focus:ring-0 resize-none min-h-[80px]"
                        rows={3}
                    />
                    <div className="flex justify-between items-center mt-3 pt-3 border-t border-gray-800/50">
                        <p className="text-[10px] text-gray-500 italic">
                            Hãy chia sẻ cảm nhận của bạn một cách văn minh nhé!
                        </p>
                        <button 
                            type="submit"
                            disabled={isSubmitting || !newCommentContent.trim()}
                            className="bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-600/30 text-white rounded-lg px-4 py-2 flex items-center gap-2 text-sm font-semibold transition-all shadow-lg shadow-indigo-500/20 active:scale-95"
                        >
                            {isSubmitting ? (
                                <div className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                            ) : (
                                <Send size={16} />
                            )}
                            Gửi
                        </button>
                    </div>
                </form>

                {/* Comments List */}
                <div className="space-y-2 divide-y divide-gray-900/50">
                    <AnimatePresence mode="popLayout">
                        {comments.length > 0 ? (
                            comments.map(comment => (
                                <motion.div
                                    key={comment.id}
                                    layout
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, scale: 0.95 }}
                                >
                                    <CommentItem 
                                        comment={comment}
                                        movieSlug={movieSlug}
                                        episodeSlug={episodeSlug}
                                        onReplySuccess={handleReplySuccess}
                                        onDeleteSuccess={handleDeleteSuccess}
                                    />
                                </motion.div>
                            ))
                        ) : !loading && (
                            <div className="py-12 flex flex-col items-center justify-center text-gray-600 gap-3">
                                <MessageSquare size={48} className="opacity-10" />
                                <p className="text-sm">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
                            </div>
                        )}
                    </AnimatePresence>
                </div>

                {hasMore && (
                    <div className="mt-8 flex justify-center">
                        <button 
                            onClick={() => fetchComments(page + 1)}
                            disabled={loading}
                            className="text-indigo-400 hover:text-indigo-300 text-xs font-semibold uppercase tracking-widest border border-indigo-400/20 px-6 py-2 rounded-full bg-indigo-400/5 hover:bg-indigo-400/10 transition-all disabled:opacity-50"
                        >
                            {loading ? 'Đang tải...' : 'Xem thêm bình luận'}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default CommentSection;
