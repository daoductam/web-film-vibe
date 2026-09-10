import { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import { userService } from '../../services/user.service';
import { ChangePasswordForm } from './ChangePasswordForm';

const MAX_AVATAR_BYTES = 5 * 1024 * 1024;

export const SettingsTab = () => {
    const { user, updateUser } = useAuthStore();
    const [fullName, setFullName] = useState(user?.fullName || '');
    const [isSaving, setIsSaving] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);

    const handleSave = async (event: React.FormEvent) => {
        event.preventDefault();
        setSuccess(false);
        if (fullName.trim().length < 2 || fullName.trim().length > 100) {
            setMessage('Tên hiển thị cần từ 2 đến 100 ký tự.');
            return;
        }
        setIsSaving(true);
        setMessage('');
        try {
            updateUser(await userService.updateProfile({ fullName: fullName.trim() }));
            setSuccess(true);
            setMessage('Cập nhật thông tin thành công!');
        } catch {
            setMessage('Không thể cập nhật thông tin. Vui lòng thử lại.');
        } finally {
            setIsSaving(false);
        }
    };

    const uploadAvatar = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        event.target.value = '';
        if (!file) return;
        setSuccess(false);
        if (!file.type.startsWith('image/') || file.size > MAX_AVATAR_BYTES || file.size === 0) {
            setMessage('Chọn ảnh có dung lượng tối đa 5 MB.');
            return;
        }
        setUploading(true);
        setMessage('');
        try {
            updateUser(await userService.uploadAvatar(file));
            setSuccess(true);
            setMessage('Cập nhật ảnh đại diện thành công!');
        } catch {
            setMessage('Không thể tải ảnh lên. Vui lòng thử lại.');
        } finally {
            setUploading(false);
        }
    };

    return (
        <div>
            <h3 className="text-2xl font-bold text-white mb-6 font-serif">Cài đặt tài khoản</h3>
            <div className="max-w-md mb-6">
                <label htmlFor="avatar-upload" className="block text-sm text-gray-400 mb-2">Ảnh đại diện</label>
                <input id="avatar-upload" type="file" accept="image/*" onChange={uploadAvatar} disabled={uploading || isSaving}
                    className="block w-full text-sm text-gray-400 file:mr-3 file:rounded-lg file:border-0 file:bg-white/10 file:px-4 file:py-2 file:text-white" />
                <p className="text-xs text-gray-400 mt-2">{uploading ? 'Đang tải ảnh lên...' : 'Ảnh có dung lượng tối đa 5 MB.'}</p>
            </div>
            <form onSubmit={handleSave} className="max-w-md space-y-4">
                <div>
                    <label htmlFor="profile-name" className="block text-sm font-medium text-gray-400 mb-1">Tên hiển thị</label>
                    <input id="profile-name" type="text" required minLength={2} maxLength={100} value={fullName}
                        onChange={event => setFullName(event.target.value)} autoComplete="name"
                        className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-neon" />
                </div>
                <div>
                    <label htmlFor="profile-email" className="block text-sm font-medium text-gray-400 mb-1">Email</label>
                    <input id="profile-email" type="email" value={user?.email || ''} disabled
                        className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-500" />
                </div>
                <button disabled={isSaving || uploading} className="w-full bg-neon text-obsidian font-bold py-3 rounded-xl hover:bg-white disabled:opacity-50">
                    {isSaving ? 'Đang lưu...' : 'Lưu thay đổi'}
                </button>
            </form>
            {message && <p role="status" className={`mt-4 text-sm ${success ? 'text-green-400' : 'text-red-400'}`}>{message}</p>}
            <ChangePasswordForm />
        </div>
    );
};
