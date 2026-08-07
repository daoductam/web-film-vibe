import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle, AlertCircle, X, Info } from 'lucide-react';
import { useToastStore } from '../../store/toastStore';

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { toasts, removeToast } = useToastStore();

    return (
        <>
            {children}
            <div className="fixed top-6 right-6 z-[9999] flex flex-col gap-3 pointer-events-none">
                <AnimatePresence>
                    {toasts.map(toast => (
                        <motion.div
                            key={toast.id}
                            initial={{ opacity: 0, x: 50, scale: 0.9 }}
                            animate={{ opacity: 1, x: 0, scale: 1 }}
                            exit={{ opacity: 0, x: 20, scale: 0.9 }}
                            className="pointer-events-auto"
                        >
                            <div className={`
                                flex items-center gap-3 px-4 py-3 rounded-2xl shadow-2xl backdrop-blur-xl border min-w-[300px] max-w-[450px]
                                ${toast.type === 'success' ? 'bg-green-500/10 border-green-500/20 text-green-400' : ''}
                                ${toast.type === 'error' ? 'bg-red-500/10 border-red-500/20 text-red-400' : ''}
                                ${toast.type === 'info' ? 'bg-blue-500/10 border-blue-500/20 text-blue-400' : ''}
                                ${toast.type === 'warning' ? 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400' : ''}
                            `}>
                                <div className="shrink-0">
                                    {toast.type === 'success' && <CheckCircle size={20} />}
                                    {toast.type === 'error' && <AlertCircle size={20} />}
                                    {toast.type === 'info' && <Info size={20} />}
                                    {toast.type === 'warning' && <AlertCircle size={20} />}
                                </div>
                                <p className="text-sm font-medium flex-1">{toast.message}</p>
                                <button 
                                    onClick={() => removeToast(toast.id)}
                                    className="p-1 hover:bg-white/10 rounded-lg transition-colors"
                                >
                                    <X size={16} />
                                </button>
                            </div>
                        </motion.div>
                    ))}
                </AnimatePresence>
            </div>
        </>
    );
};

// Export useToast as a proxy to useToastStore.showToast for backward compatibility
export const useToast = () => {
    const showToast = useToastStore(state => state.showToast);
    return { showToast };
};
