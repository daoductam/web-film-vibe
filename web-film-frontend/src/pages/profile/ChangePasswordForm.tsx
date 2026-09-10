import { useState } from 'react';
import { userService } from '../../services/user.service';

export const ChangePasswordForm = () => {
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [pending, setPending] = useState(false);
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);

    const submit = async (event: React.FormEvent) => {
        event.preventDefault();
        setSuccess(false);
        if (!currentPassword.trim() || !newPassword.trim() || newPassword.length < 6) {
            setMessage('Nhập mật khẩu hiện tại và mật khẩu mới có ít nhất 6 ký tự.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setMessage('Mật khẩu xác nhận không khớp.');
            return;
        }
        setPending(true);
        setMessage('');
        try {
            await userService.changePassword({ currentPassword, newPassword, confirmPassword });
            setCurrentPassword('');
            setNewPassword('');
            setConfirmPassword('');
            setSuccess(true);
            setMessage('Đổi mật khẩu thành công.');
        } catch {
            setMessage('Không thể đổi mật khẩu. Kiểm tra mật khẩu hiện tại và thử lại.');
        } finally {
            setPending(false);
        }
    };

    return (
        <form onSubmit={submit} className="max-w-md space-y-4 border-t border-white/10 pt-8 mt-8">
            <h4 className="text-xl font-bold">Đổi mật khẩu</h4>
            {[
                { id: 'current-password', label: 'Mật khẩu hiện tại', value: currentPassword, change: setCurrentPassword, autocomplete: 'current-password' },
                { id: 'new-password', label: 'Mật khẩu mới', value: newPassword, change: setNewPassword, autocomplete: 'new-password' },
                { id: 'confirm-password', label: 'Xác nhận mật khẩu mới', value: confirmPassword, change: setConfirmPassword, autocomplete: 'new-password' },
            ].map(field => (
                <div key={field.id}>
                    <label htmlFor={field.id} className="block text-sm text-gray-400 mb-1">{field.label}</label>
                    <input id={field.id} type="password" required minLength={field.id === 'current-password' ? undefined : 6}
                        autoComplete={field.autocomplete} value={field.value} onChange={event => field.change(event.target.value)}
                        disabled={pending} className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-neon" />
                </div>
            ))}
            <p className="text-xs text-gray-400">Mật khẩu mới cần ít nhất 6 ký tự.</p>
            <button disabled={pending} className="w-full bg-neon text-obsidian font-bold py-3 rounded-xl disabled:opacity-50">{pending ? 'Đang cập nhật...' : 'Đổi mật khẩu'}</button>
            {message && <p role="status" className={`text-sm ${success ? 'text-green-400' : 'text-red-400'}`}>{message}</p>}
        </form>
    );
};
