import React, { useState } from 'react';
import { ThumbsUp, Reply, Trash2, ChevronDown, ChevronUp } from 'lucide-react';
import { Comment } from '../../types';
import { socialService } from '../../services/social.service';
import { formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';
import { motion, AnimatePresence } from 'framer-motion';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}

interface CommentItemProps {
    comment: Comment;
    onReplySuccess: (parentComment: Comment, newReply: Comment) => void;
    onDeleteSuccess: (commentId: number) => void;
    movieSlug: string;
    episodeSlug: string;
    isReply?: boolean;
}

const CommentItem: React.FC<CommentItemProps> = ({ 
    comment, 
    onReplySuccess, 
    onDeleteSuccess, 
    movieSlug, 
    episodeSlug, 
    isReply = false 
}) => {
    const [isLiked, setIsLiked] = useState(comment.isLiked);
    const [likeCount, setLikeCount] = useState(comment.likeCount);
    const [isReplying, setIsReplying] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showReplies, setShowReplies] = useState(true);

    const handleLike = async () => {
        try {
            const response = await socialService.toggleLike(comment.id);
            if (response.success) {
                setIsLiked(response.data);
                setLikeCount(prev => response.data ? prev + 1 : prev - 1);
            }
        } catch (error) {
            console.error('Failed to like:', error);
        }
    };

    const handleReply = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!replyContent.trim() || isSubmitting) return;

        setIsSubmitting(true);
        try {
            const response = await socialService.addComment({
                movieSlug,
                episodeSlug,
                content: replyContent,
                parentId: comment.id
            });
            if (response.success) {
                onReplySuccess(comment, response.data);
                setReplyContent('');
                setIsReplying(false);
            }
        } catch (error) {
            console.error('Failed to reply:', error);
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm('Bạn có chắc chắn muốn xóa bình luận này?')) return;
        try {
            const response = await socialService.deleteComment(comment.id);
            if (response.success) {
                onDeleteSuccess(comment.id);
            }
        } catch (error) {
            console.error('Failed to delete:', error);
        }
    };

    return (
        <div className={cn("flex flex-col gap-3 py-4", isReply ? "ml-12 border-l border-gray-800 pl-4" : "")}>
            <div className="flex gap-3">
                {/* Avatar */}
                <div className="w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center font-bold text-white overflow-hidden flex-shrink-0">
                    {comment.avatarUrl ? (
                        <img src={comment.avatarUrl} alt={comment.username} className="w-full h-full object-cover" />
                    ) : (
                        comment.fullName?.charAt(0) || comment.username.charAt(0)
                    )}
                </div>

                {/* Content Area */}
                <div className="flex-1 flex flex-col gap-1">
                    <div className="flex items-center gap-2">
                        <span className="font-semibold text-gray-200 text-sm">{comment.fullName || comment.username}</span>
                        {comment.episodeName && comment.episodeName !== 'Full' && (
                            <span className="px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-400 text-[10px] font-bold border border-indigo-500/30">
                                {comment.episodeName}
                            </span>
                        )}
                        <span className="text-[10px] text-gray-500">
                            {formatDistanceToNow(new Date(comment.createdAt), { addSuffix: true, locale: vi })}
                        </span>
                    </div>
                    
                    <p className="text-gray-300 text-sm leading-relaxed">{comment.content}</p>

                    {/* Actions */}
                    <div className="flex items-center gap-4 mt-1">
                        <button 
                            onClick={handleLike}
                            className={cn(
                                "flex items-center gap-1 text-xs transition-colors",
                                isLiked ? "text-indigo-400" : "text-gray-500 hover:text-gray-300"
                            )}
                        >
                            <ThumbsUp size={14} fill={isLiked ? "currentColor" : "none"} />
                            <span>{likeCount}</span>
                        </button>

                        {!isReply && (
                            <button 
                                onClick={() => setIsReplying(!isReplying)}
                                className="flex items-center gap-1 text-xs text-gray-500 hover:text-gray-300 transition-colors"
                            >
                                <Reply size={14} />
                                <span>Phản hồi</span>
                            </button>
                        )}

                        {/* Note: In real app, check if current user is the owner */}
                        <button 
                            onClick={handleDelete}
                            className="text-xs text-gray-600 hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100"
                        >
                            <Trash2 size={14} />
                        </button>
                    </div>
                </div>
            </div>

            {/* Reply Form */}
            <AnimatePresence>
                {isReplying && (
                    <motion.form 
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        onSubmit={handleReply}
                        className="ml-12 mt-2"
                    >
                        <textarea
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                            placeholder="Viết phản hồi của bạn..."
                            className="w-full bg-gray-900 border border-gray-800 rounded-lg p-3 text-sm text-gray-200 focus:outline-none focus:border-indigo-500 transition-colors resize-none"
                            rows={2}
                        />
                        <div className="flex justify-end gap-2 mt-2">
                            <button 
                                type="button"
                                onClick={() => setIsReplying(false)}
                                className="px-3 py-1.5 text-xs text-gray-400 hover:text-gray-200 transition-colors"
                            >
                                Hủy
                            </button>
                            <button 
                                type="submit"
                                disabled={isSubmitting || !replyContent.trim()}
                                className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-600/50 text-white rounded-md text-xs font-medium transition-all"
                            >
                                {isSubmitting ? 'Đang gửi...' : 'Phản hồi'}
                            </button>
                        </div>
                    </motion.form>
                )}
            </AnimatePresence>

            {/* Nested Replies */}
            {comment.replies && comment.replies.length > 0 && (
                <div className="flex flex-col">
                    <button 
                        onClick={() => setShowReplies(!showReplies)}
                        className="ml-12 flex items-center gap-1 text-xs text-indigo-400 hover:text-indigo-300 transition-colors mb-2 w-fit"
                    >
                        {showReplies ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                        <span>{showReplies ? 'Ẩn' : 'Xem'} {comment.replies.length} phản hồi</span>
                    </button>
                    
                    <AnimatePresence>
                        {showReplies && (
                            <motion.div
                                initial={{ opacity: 0, scaleY: 0.9 }}
                                animate={{ opacity: 1, scaleY: 1 }}
                                exit={{ opacity: 0, scaleY: 0.9 }}
                                className="origin-top"
                            >
                                {comment.replies.map(reply => (
                                    <CommentItem 
                                        key={reply.id} 
                                        comment={reply} 
                                        onReplySuccess={onReplySuccess}
                                        onDeleteSuccess={onDeleteSuccess}
                                        movieSlug={movieSlug}
                                        episodeSlug={episodeSlug}
                                        isReply
                                    />
                                ))}
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            )}
        </div>
    );
};

export default CommentItem;
