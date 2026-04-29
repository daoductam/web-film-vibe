import { useState } from 'react';
import api from '../../services/api';
import { Database, RefreshCw } from 'lucide-react';
import { useToast } from '../../components/common/Toast';

export const CrawlPage = () => {
    const [isCrawling, setIsCrawling] = useState(false);
    const [pageNumber, setPageNumber] = useState(1);
    const [message, setMessage] = useState('');

    const { showToast } = useToast();

    const handleCrawl = async () => {
        setIsCrawling(true);
        setMessage('Đang tiến hành crawl dữ liệu từ NguonC. Vui lòng đợi...');
        showToast('Bắt đầu quá trình crawl dữ liệu...', 'info');
        try {
            // TestCrawlController endpoint
            const res = await api.get(`/crawl/nguonc/latest?page=${pageNumber}`);
            const count = res.data.data?.length || 0;
            setMessage(`Crawl thành công! Đã cập nhật ${count} phim.`);
            showToast(`Crawl thành công! Đã cập nhật ${count} phim mới.`, 'success');
        } catch (error: any) {
            const errorMsg = error.response?.data?.message || error.message;
            setMessage(`Lỗi crawl: ${errorMsg}`);
            showToast(`Lỗi crawl: ${errorMsg}`, 'error');
        } finally {
            setIsCrawling(false);
        }
    };

    return (
        <div className="max-w-4xl space-y-6">
            <div className="glass-card rounded-2xl p-8 border border-white/5">
                <div className="flex items-center gap-3 mb-6">
                    <div className="size-10 rounded-full bg-neon/10 flex items-center justify-center text-neon">
                        <Database className="w-5 h-5" />
                    </div>
                    <div>
                        <h3 className="text-xl font-bold text-white">Crawl Dữ Liệu Phim Mới</h3>
                        <p className="text-sm text-gray-400">Tự động lấy dữ liệu phim mới nhất từ nguồn API bên ngoài.</p>
                    </div>
                </div>

                <div className="bg-white/5 border border-white/10 rounded-xl p-6 space-y-6">
                    <div>
                        <label className="block text-sm font-medium text-gray-400 mb-2">Số trang cần lấy (Page)</label>
                        <input 
                            type="number" 
                            min="1"
                            value={pageNumber}
                            onChange={(e) => setPageNumber(parseInt(e.target.value) || 1)}
                            className="w-full sm:w-32 bg-obsidian border border-white/10 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all"
                        />
                    </div>

                    <button 
                        onClick={handleCrawl}
                        disabled={isCrawling}
                        className="flex items-center gap-2 px-6 py-3 bg-neon text-obsidian font-bold rounded-xl hover:bg-white transition-colors disabled:opacity-50"
                    >
                        {isCrawling ? <RefreshCw className="w-5 h-5 animate-spin" /> : <Database className="w-5 h-5" />}
                        {isCrawling ? 'Đang Crawl...' : 'Bắt đầu Crawl'}
                    </button>

                    {message && (
                        <div className={`p-4 rounded-lg border ${message.includes('Lỗi') ? 'bg-red-500/10 border-red-500/30 text-red-400' : 'bg-green-500/10 border-green-500/30 text-green-400'}`}>
                            {message}
                        </div>
                    )}
                </div>
            </div>
            
            {/* Instruction Box */}
            <div className="bg-blue-500/10 border border-blue-500/30 rounded-xl p-6">
                <h4 className="font-bold text-blue-400 mb-2">Hướng dẫn:</h4>
                <ul className="list-disc list-inside text-sm text-gray-300 space-y-1">
                    <li>Hệ thống hiện tại hỗ trợ API từ OPhim và NguonC.</li>
                    <li>Nên chạy crawl định kỳ hoặc khi có nhu cầu cập nhật gấp.</li>
                    <li>Dữ liệu bao gồm: Thông tin phim, hình ảnh, server và các tập phim.</li>
                </ul>
            </div>
        </div>
    );
};
