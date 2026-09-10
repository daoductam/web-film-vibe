import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { watchPartyService } from '../../services/watchParty.service';
import type { WatchRoom, ChatMessage, VideoState } from '../../services/watchParty.service';
import { movieService } from '../../services/movie.service';
import type { MovieDetail } from '../../types';
import api from '../../services/api';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../hooks/useToast';
import { Navbar } from '../../components/layout/Navbar';
import { StreamingPlayer } from '../../components/player/StreamingPlayer';
import { acquireRoomMembership } from './roomMembership';

export const WatchPartyRoomPage = () => {
  const roomId = Number(useParams().id);
  const navigate = useNavigate();
  const { user, token } = useAuthStore();
  const userId = user?.id;
  const { showToast } = useToast();
  const [room, setRoom] = useState<WatchRoom | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [src, setSrc] = useState('');
  const [canControl, setCanControl] = useState(false);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');
  const [input, setInput] = useState('');
  const [spoiler, setSpoiler] = useState(false);
  const [revealed, setRevealed] = useState<Set<number>>(new Set());
  const [reaction, setReaction] = useState('');
  const [leaving, setLeaving] = useState(false);
  const [needsPlay, setNeedsPlay] = useState(false);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const clientRef = useRef<Client | null>(null);
  const stateRef = useRef<VideoState | null>(null);
  const applyingUntil = useRef(0);
  const messagesEnd = useRef<HTMLDivElement | null>(null);
  const leftRef = useRef(false);

  const applyState = (state: VideoState) => {
    stateRef.current = state;
    const video = videoRef.current;
    if (!video || video.readyState < 1) return;
    applyingUntil.current = Date.now() + 750;
    if (Math.abs(video.currentTime - state.currentTime) > 1.5) video.currentTime = state.currentTime;
    video.playbackRate = state.playbackRate || 1;
    if (state.isPlaying) void video.play().then(() => setNeedsPlay(false)).catch(() => setNeedsPlay(true));
    else video.pause();
  };

  useEffect(() => {
    if (!Number.isSafeInteger(roomId) || roomId <= 0) return;
    if (!userId || !token) { navigate('/login', { replace: true }); return; }
    let disposed = false;
    const membership = acquireRoomMembership(roomId, userId, token);
    let client: Client | null = null;
    let poll: ReturnType<typeof setInterval> | undefined;
    const refresh = async () => {
      const details = await watchPartyService.getRoomById(roomId);
      if (disposed) return;
      setRoom(details);
      if (details.status === 'ENDED') {
        videoRef.current?.pause();
        setConnected(false);
        void client?.deactivate();
        if (poll) clearInterval(poll);
        setError('Phòng đã kết thúc. Quay lại sảnh để tham gia phòng khác.');
      }
    };
    const initialize = async () => {
      try {
        const member = await membership.joined;
        if (disposed) return;
        leftRef.current = false;
        setCanControl(member.role === 'HOST' || member.role === 'CO_HOST');
        const details = await watchPartyService.getRoomById(roomId);
        const [movie, history, state] = await Promise.all([
          movieService.getMovieDetail(details.movie.slug) as Promise<MovieDetail>,
          watchPartyService.getMessages(roomId), watchPartyService.getVideoState(roomId),
        ]);
        if (disposed) return;
        const episodes = movie.servers.flatMap(server => server.episodes);
        const episode = details.episode
          ? episodes.find(item => item.id === details.episode?.id || item.slug === details.episode?.slug)
          : episodes[0];
        setSrc(episode?.linkM3u8 || '');
        setRoom(details);
        setMessages(history.content.slice().reverse());
        stateRef.current = state;
        const base = (api.defaults.baseURL || '/api/v1').replace(/\/v1\/?$/, '');
        client = new Client({
          webSocketFactory: () => new SockJS(`${base}/ws`),
          reconnectDelay: 5000,
          beforeConnect: () => { if (client) client.connectHeaders = { Authorization: `Bearer ${useAuthStore.getState().token}` }; },
          onConnect: () => {
            if (disposed || !client) return;
            setConnected(true);
            client.subscribe(`/topic/room.${roomId}.chat`, message => {
              const payload = JSON.parse(message.body) as ChatMessage;
              setMessages(previous => payload.id !== 0 && previous.some(item => item.id === payload.id) ? previous : [...previous, payload]);
            });
            client.subscribe(`/topic/room.${roomId}.sync`, message => {
              const payload = JSON.parse(message.body) as { action: string; timestamp: number; issuedBy?: { id: number } };
              if (payload.issuedBy?.id === useAuthStore.getState().user?.id) return;
              if (!Number.isFinite(payload.timestamp) || payload.timestamp < 0) return;
              applyState({ currentTime: payload.timestamp, isPlaying: payload.action === 'PLAY', playbackRate: 1, lastSyncAt: '' });
            });
            client.subscribe(`/topic/room.${roomId}.reaction`, message => {
              const payload = JSON.parse(message.body) as { username: string; emoji: string };
              setReaction(`${payload.username}: ${payload.emoji}`);
            });
            void watchPartyService.getVideoState(roomId).then(state => { if (!disposed) applyState(state); }).catch(() => undefined);
          },
          onWebSocketClose: () => { if (!disposed) setConnected(false); },
          onStompError: () => { if (!disposed) { setConnected(false); showToast('Không thể kết nối phòng. Vui lòng thử lại.', 'error'); } },
        });
        clientRef.current = client;
        client.activate();
        poll = setInterval(() => void refresh().catch(() => undefined), 10000);
      } catch {
        if (!disposed) setError('Không thể tham gia phòng. Phòng có thể đã đầy hoặc đã kết thúc.');
      }
    };
    void initialize();
    return () => {
      disposed = true;
      if (poll) clearInterval(poll);
      void client?.deactivate();
      clientRef.current = null;
      membership.release(leftRef.current);
    };
  }, [roomId, userId, token, navigate, showToast]);

  useEffect(() => { messagesEnd.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);
  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;
    const ready = () => { if (stateRef.current) applyState(stateRef.current); };
    video.addEventListener('loadedmetadata', ready);
    return () => video.removeEventListener('loadedmetadata', ready);
  }, [src]);
  useEffect(() => {
    if (!reaction) return;
    const timer = setTimeout(() => setReaction(''), 4000);
    return () => clearTimeout(timer);
  }, [reaction]);
  useEffect(() => {
    if (!canControl || !connected) return;
    const timer = setInterval(() => {
      const video = videoRef.current;
      if (video && !video.paused && clientRef.current?.connected && Date.now() >= applyingUntil.current) {
        clientRef.current.publish({ destination: `/app/room.${roomId}.sync`, body: JSON.stringify({ action: 'PLAY', timestamp: video.currentTime }) });
      }
    }, 5000);
    return () => clearInterval(timer);
  }, [canControl, connected, roomId]);

  const publishPlayback = () => {
    const video = videoRef.current;
    if (!video || !canControl || !clientRef.current?.connected || Date.now() < applyingUntil.current) return;
    // The server persists only PLAY as playing; sending SEEK would incorrectly pause late joiners.
    clientRef.current.publish({ destination: `/app/room.${roomId}.sync`, body: JSON.stringify({ action: video.paused ? 'PAUSE' : 'PLAY', timestamp: video.currentTime }) });
  };
  const leave = async () => {
    setLeaving(true);
    try {
      if (room?.status !== 'ENDED') await watchPartyService.leaveRoom(roomId);
      leftRef.current = true;
      navigate('/watch-party');
    } catch { showToast('Không thể rời phòng. Vui lòng thử lại.', 'error'); }
    finally { setLeaving(false); }
  };
  const send = (event: React.FormEvent) => {
    event.preventDefault();
    if (!input.trim() || !clientRef.current?.connected) return;
    clientRef.current.publish({ destination: `/app/room.${roomId}.chat`, body: JSON.stringify({ content: input.trim(), messageType: spoiler ? 'SPOILER' : 'CHAT' }) });
    setInput('');
    setSpoiler(false);
  };

  return <div className="min-h-screen bg-obsidian text-white"><Navbar />
    <main className="max-w-[1600px] mx-auto px-4 pt-24 pb-12">
      <button onClick={() => room ? void leave() : navigate('/watch-party')} disabled={leaving} className="mb-5 rounded-xl bg-white/10 px-4 py-2">← {room?.host.id === user?.id ? 'Rời và kết thúc phòng' : 'Về sảnh'}</button>
      {(error || !Number.isSafeInteger(roomId) || roomId <= 0) && <p role="alert" className="p-4 mb-4 bg-red-500/10 rounded-xl">{error || 'Mã phòng không hợp lệ.'}</p>}
      {!room && !error && roomId > 0 && <p role="status">Đang tham gia phòng…</p>}
      {room && <div className="grid lg:grid-cols-[minmax(0,1fr)_360px] gap-6">
        <section>
          <h1 className="text-2xl font-bold mb-2">{room.movie.title}</h1>
          <p className="text-text-secondary mb-4">{room.episode?.name} · {canControl ? 'Bạn có quyền điều khiển phim cho cả phòng' : 'Phim được đồng bộ theo chủ phòng'}</p>
          {src ? <div className="aspect-video"><StreamingPlayer autoPlay={false} src={src} title={room.movie.title} poster={room.movie.thumbUrl} videoRef={videoRef} controls={canControl && room.status !== 'ENDED'} onPlay={publishPlayback} onPause={publishPlayback} onSeeked={publishPlayback} onRateChange={() => { if (videoRef.current && videoRef.current.playbackRate !== 1) videoRef.current.playbackRate = 1; }} /></div> : <div className="aspect-video grid place-items-center bg-white/5 rounded-xl">Tập phim chưa có nguồn HLS để xem chung.</div>}
          {needsPlay && <button onClick={() => { if (stateRef.current) applyState(stateRef.current); }} className="bg-neon text-black rounded-xl px-4 py-2 mt-3">Bấm để phát phim đồng bộ</button>}
          <div className="flex gap-3 mt-4" aria-label="Thả cảm xúc">{['❤️', '😂', '😮', '👏', '🔥'].map(emoji => <button key={emoji} aria-label={`Gửi ${emoji}`} disabled={!connected} className="text-2xl disabled:opacity-40" onClick={() => clientRef.current?.connected && clientRef.current.publish({ destination: `/app/room.${roomId}.reaction`, body: JSON.stringify({ emoji }) })}>{emoji}</button>)}</div>
          <p aria-live="polite" className="mt-3 h-8 text-neon">{reaction}</p>
        </section>
        <section className="bg-white/5 border border-white/10 rounded-2xl flex flex-col h-[650px] max-h-[80vh]">
          <div className="p-4 border-b border-white/10"><h2 className="font-bold">{room.name}</h2><p className="text-sm text-text-secondary">{room.currentMemberCount}/{room.maxMembers} thành viên · {connected ? 'Đã kết nối' : 'Đang kết nối lại…'}</p><button className="text-neon text-sm mt-2" onClick={() => void navigator.clipboard.writeText(room.code).then(() => showToast('Đã sao chép mã phòng', 'success')).catch(() => showToast('Không thể sao chép mã phòng', 'error'))}>Mã: {room.code} · Sao chép</button></div>
          <div className="flex-1 overflow-y-auto p-4 space-y-4">{messages.map((message, index) => <div key={`${message.id}-${index}`} className={message.userId === user?.id ? 'text-right' : ''}><span className="text-xs text-text-secondary">{message.username}</span><div className="text-sm break-words whitespace-pre-wrap mt-1">{message.messageType === 'SPOILER' && !revealed.has(message.id) ? <button className="text-amber-400" onClick={() => setRevealed(previous => new Set([...previous, message.id]))}>Nội dung spoiler · Bấm để xem</button> : message.content}</div></div>)}<div ref={messagesEnd} /></div>
          <form onSubmit={send} className="p-4 border-t border-white/10"><label className="text-xs flex gap-2 mb-2"><input type="checkbox" checked={spoiler} onChange={event => setSpoiler(event.target.checked)} />Ẩn nội dung spoiler</label><div className="flex gap-2"><input aria-label="Tin nhắn" maxLength={2000} value={input} onChange={event => setInput(event.target.value)} placeholder="Trò chuyện… (@AI để hỏi bot)" className="min-w-0 flex-1 bg-black/30 rounded-lg px-3 py-2" /><button disabled={!connected || !input.trim()} className="bg-neon text-black px-3 rounded-lg disabled:opacity-40">Gửi</button></div></form>
        </section>
      </div>}
    </main>
  </div>;
};
