import { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import { userService } from '../../services/user.service';

export const SettingsTab = () => {
    const { user, updateUser } = useAuthStore();
    const [fullName, setFullName] = useState(user?.fullName || '');
    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState('');

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setMessage('');
        try {
            const updatedUser = await userService.updateProfile({ fullName });
            updateUser(updatedUser);
            setMessage('Cập nhật thông tin thành công!');
        } catch (error) {
            setMessage('Có lỗi xảy ra, vui lòng thử lại.');
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <div>
            <h3 className="text-2xl font-bold text-white mb-6 font-serif">Cài đặt tài khoản</h3>
            <form onSubmit={handleSave} className="max-w-md space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-400 mb-1">Tên hiển thị</label>
                    <input 
                        type="text" 
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon transition-all"
                        placeholder="Nhập tên hiển thị"
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-400 mb-1">Email</label>
                    <input 
                        type="email" 
                        value={user?.email || ''}
                        disabled
                        className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-500 cursor-not-allowed"
                    />
                </div>
                <button 
                    type="submit" 
                    disabled={isSaving}
                    className="w-full bg-neon text-obsidian font-bold py-3 rounded-xl hover:bg-white transition-colors disabled:opacity-50"
                >
                    {isSaving ? 'Đang lưu...' : 'Lưu thay đổi'}
                </button>
                {message && (
                    <p className={`text-center text-sm ${message.includes('thành công') ? 'text-green-400' : 'text-red-400'}`}>
                        {message}
                    </p>
                )}
            </form>
        </div>
    );
};
