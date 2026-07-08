import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { watchPartyService } from '../../services/watchParty.service';
import type { WatchRoom } from '../../services/watchParty.service';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../components/common/Toast';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Plus, Users, Lock, Globe } from 'lucide-react';

export const WatchPartyLobbyPage = () => {
  const [rooms, setRooms] = useState<WatchRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [joinCode, setJoinCode] = useState('');
  const [roomName, setRoomName] = useState('');
  const [movieId, setMovieId] = useState('');
  const [roomType, setRoomType] = useState<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const [maxMembers, setMaxMembers] = useState(10);
  
  const { token } = useAuthStore();
  const { showToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    fetchPublicRooms();
  }, []);

  const fetchPublicRooms = async () => {
    try {
      setLoading(true);
      const data = await watchPartyService.getPublicRooms();
      setRooms(data.content || []);
    } catch (err) {
      console.error(err);
      showToast('Không thể lấy danh sách phòng công khai', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      showToast('Vui lòng đăng nhập để tạo phòng!', 'warning');
      navigate('/login');
      return;
    }
    const moviePk = parseInt(movieId);
    if (!roomName || isNaN(moviePk)) {
      showToast('Vui lòng điền đầy đủ thông tin phòng', 'warning');
      return;
    }

    try {
      const room = await watchPartyService.createRoom({
        name: roomName,
        movieId: moviePk,
        roomType,
        maxMembers
      });
      showToast('Tạo phòng xem chung thành công!', 'success');
      navigate(`/watch-party/room/${room.id}`);
    } catch (err) {
      console.error(err);
      showToast('Tạo phòng xem chung thất bại', 'error');
    }
  };

  const handleJoinByCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      showToast('Vui lòng đăng nhập để tham gia phòng!', 'warning');
      navigate('/login');
      return;
    }
    if (!joinCode) return;

    try {
      const room = await watchPartyService.getRoomByCode(joinCode.trim().toUpperCase());
      await watchPartyService.joinRoom(room.id);
      showToast('Vào phòng xem chung thành công!', 'success');
      navigate(`/watch-party/room/${room.id}`);
    } catch (err) {
      console.error(err);
      showToast('Không tìm thấy phòng phù hợp hoặc phòng đã đầy', 'error');
    }
  };

  return (
    <div className="bg-obsidian text-text-primary min-h-screen flex flex-col font-sans">
      <Navbar />
      
      <main className="flex-1 max-w-[1400px] w-full mx-auto px-4 md:px-6 pt-24 md:pt-28 pb-16">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
          <div>
            <h1 className="font-serif text-3xl md:text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-white to-gray-400">
              Watch Party
            </h1>
            <p className="text-text-secondary text-sm md:text-base mt-2">
              Xem phim trực tuyến cùng bạn bè trong thời gian thực, trò chuyện và voice chat.
            </p>
          </div>
          <button 
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 px-6 py-3 rounded-full bg-neon text-obsidian font-bold transition-all shadow-neon hover:scale-105"
          >
            <Plus className="w-5 h-5" />
            Tạo phòng mới
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Join by code section */}
          <div className="lg:col-span-4 space-y-6">
            <div className="bg-white/5 border border-white/10 rounded-2xl p-6 backdrop-blur-md">
              <h3 className="text-lg font-bold text-white mb-4">Vào phòng bằng mã</h3>
              <form onSubmit={handleJoinByCode} className="space-y-4">
                <input 
                  type="text" 
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-neon focus:ring-1 focus:ring-neon text-white uppercase"
                  placeholder="Mã phòng (ví dụ: ABC12XYZ)"
                  value={joinCode}
                  onChange={(e) => setJoinCode(e.target.value)}
                />
                <button 
                  type="submit"
                  className="w-full py-3 rounded-xl bg-white/10 hover:bg-neon hover:text-obsidian font-bold text-white transition-all"
                >
                  Tham gia
                </button>
              </form>
            </div>
          </div>

          {/* Public rooms listing */}
          <div className="lg:col-span-8 space-y-6">
            <h2 className="text-xl font-bold text-white pl-2 border-l-4 border-neon">
              Các phòng công khai đang chiếu
            </h2>

            {loading ? (
              <div className="flex justify-center py-20">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-neon"></div>
              </div>
            ) : rooms.length === 0 ? (
              <div className="bg-white/5 border border-white/10 rounded-2xl p-12 text-center text-text-secondary">
                Chưa có phòng công khai nào đang hoạt động. Hãy tạo phòng để bắt đầu cuộc vui nhé!
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {rooms.map((room) => (
                  <div 
                    key={room.id}
                    onClick={() => navigate(`/watch-party/room/${room.id}`)}
                    className="flex gap-4 p-4 bg-white/5 border border-white/10 hover:border-neon/30 rounded-xl cursor-pointer transition-all hover:scale-[1.02]"
                  >
                    <div className="w-20 h-28 bg-white/10 rounded-lg overflow-hidden shrink-0">
                      <img 
                        src={room.movie.posterUrl || room.movie.thumbUrl} 
                        alt={room.movie.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="flex flex-col justify-between py-1">
                      <div>
                        <h4 className="font-bold text-white text-base line-clamp-1">{room.name}</h4>
                        <p className="text-text-secondary text-xs mt-1">Đang chiếu: {room.movie.title}</p>
                      </div>
                      <div className="flex items-center gap-4 text-xs text-text-secondary mt-2">
                        <span className="flex items-center gap-1">
                          <Users className="w-4 h-4 text-neon" />
                          {room.currentMemberCount}/{room.maxMembers}
                        </span>
                        <span className="flex items-center gap-1">
                          {room.roomType === 'PUBLIC' ? (
                            <Globe className="w-4 h-4 text-green-400" />
                          ) : (
                            <Lock className="w-4 h-4 text-red-400" />
                          )}
                          {room.roomType === 'PUBLIC' ? 'Công khai' : 'Riêng tư'}
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      <Footer />

      {/* Creation Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="bg-surface border border-white/10 rounded-2xl max-w-md w-full p-6 space-y-4 animate-in zoom-in-95">
            <h3 className="text-xl font-bold text-white">Tạo phòng Watch Party mới</h3>
            <form onSubmit={handleCreateRoom} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-400 mb-1">Tên phòng</label>
                <input 
                  type="text" 
                  required
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-neon text-white"
                  value={roomName}
                  onChange={(e) => setRoomName(e.target.value)}
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 mb-1">Mã phim (ID)</label>
                <input 
                  type="number" 
                  required
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-neon text-white"
                  value={movieId}
                  onChange={(e) => setMovieId(e.target.value)}
                />
              </div>
              <div className="flex gap-4">
                <div className="flex-1">
                  <label className="block text-xs font-bold text-gray-400 mb-1">Loại phòng</label>
                  <select 
                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-neon text-white"
                    value={roomType}
                    onChange={(e) => setRoomType(e.target.value as any)}
                  >
                    <option value="PUBLIC" className="bg-obsidian">Công khai</option>
                    <option value="PRIVATE" className="bg-obsidian">Riêng tư</option>
                  </select>
                </div>
                <div className="flex-1">
                  <label className="block text-xs font-bold text-gray-400 mb-1">Giới hạn người</label>
                  <input 
                    type="number" 
                    min="2" 
                    max="50"
                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-neon text-white"
                    value={maxMembers}
                    onChange={(e) => setMaxMembers(parseInt(e.target.value))}
                  />
                </div>
              </div>
              <div className="flex gap-3 pt-4">
                <button 
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 py-3 rounded-xl bg-white/5 border border-white/10 text-white hover:bg-white/10 transition-all font-bold text-sm"
                >
                  Hủy
                </button>
                <button 
                  type="submit"
                  className="flex-1 py-3 rounded-xl bg-neon text-obsidian font-bold text-sm hover:bg-white transition-all shadow-neon"
                >
                  Tạo phòng
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
