import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { watchPartyService } from '../../services/watchParty.service';
import type { WatchRoom, ChatMessage } from '../../services/watchParty.service';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../components/common/Toast';
import { Navbar } from '../../components/layout/Navbar';
import { Footer } from '../../components/layout/Footer';
import { Send, Eye, ShieldAlert, ArrowLeft, Users, Copy } from 'lucide-react';

export const WatchPartyRoomPage = () => {
  const { id } = useParams<{ id: string }>();
  const roomId = parseInt(id || '0');
  
  const [room, setRoom] = useState<WatchRoom | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [isSpoiler, setIsSpoiler] = useState(false);
  const [loading, setLoading] = useState(true);
  const [revealedSpoilers, setRevealedSpoilers] = useState<Record<number, boolean>>({});

  const stompClientRef = useRef<Client | null>(null);
  const { token, user } = useAuthStore();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (roomId) {
      loadRoomDetails();
    }
    return () => {
      disconnectWebSocket();
    };
  }, [roomId]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadRoomDetails = async () => {
    try {
      setLoading(true);
      const roomData = await watchPartyService.getRoomById(roomId);
      setRoom(roomData);

      // Load initial chat messages
      const msgsData = await watchPartyService.getMessages(roomId);
      setMessages(msgsData.content?.slice().reverse() || []);

      // Connect STOMP
      connectWebSocket(roomData.id);
    } catch (err) {
      console.error(err);
      showToast('Không thể tải thông tin phòng', 'error');
      navigate('/watch-party');
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = (rId: number) => {
    const socket = new SockJS('http://localhost:8081/ws');
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log(str),
      onConnect: () => {
        console.log('STOMP connected successfully on Web');
        
        // Subscribe to Chat Messages
        client.subscribe(`/topic/room.${rId}.chat`, (message) => {
          const payload = JSON.parse(message.body) as ChatMessage;
          setMessages((prev) => [...prev, payload]);
        });

        // Subscribe to Sync controls (placeholder in Phase 3)
        client.subscribe(`/topic/room.${rId}.sync`, (message) => {
          const payload = JSON.parse(message.body);
          console.log('Received Sync command:', payload);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
      }
    });

    stompClientRef.current = client;
    client.activate();
  };

  const disconnectWebSocket = () => {
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
    }
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!chatInput.trim() || !stompClientRef.current) return;

    stompClientRef.current.publish({
      destination: `/app/room.${roomId}.chat`,
      body: JSON.stringify({
        content: chatInput,
        messageType: isSpoiler ? 'SPOILER' : 'CHAT'
      })
    });

    setChatInput('');
    setIsSpoiler(false);
  };

  const handleLeaveRoom = async () => {
    try {
      await watchPartyService.leaveRoom(roomId);
      showToast('Đã rời khỏi phòng xem chung', 'info');
      navigate('/watch-party');
    } catch (err) {
      console.error(err);
      navigate('/watch-party');
    }
  };

  const handleCopyCode = () => {
    if (!room) return;
    navigator.clipboard.writeText(room.code);
    showToast('Đã sao chép mã phòng!', 'success');
  };

  if (loading) {
    return (
      <div className="bg-obsidian text-white min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-neon"></div>
      </div>
    );
  }

  if (!room) return null;

  return (
    <div className="bg-obsidian text-text-primary min-h-screen flex flex-col font-sans">
      <Navbar />

      <main className="flex-1 flex flex-col lg:flex-row h-[calc(100vh-4rem)] pt-16">
        {/* Left Column: Synced Video Player area */}
        <div className="flex-1 flex flex-col bg-black justify-center items-center relative aspect-video lg:aspect-auto">
          <img 
            src={room.movie.thumbUrl || room.movie.posterUrl} 
            alt={room.movie.title}
            className="absolute inset-0 w-full h-full object-cover opacity-30"
          />
          <div className="relative z-10 text-center px-6">
            <h2 className="text-2xl md:text-4xl font-bold text-white mb-2">{room.movie.title}</h2>
            <p className="text-neon text-sm font-bold tracking-widest uppercase">Trình phát Video Đồng bộ (Web)</p>
          </div>
          
          <button 
            onClick={handleLeaveRoom}
            className="absolute top-4 left-4 flex items-center gap-2 px-4 py-2 bg-white/10 hover:bg-white/20 text-white rounded-lg transition-all"
          >
            <ArrowLeft className="w-4 h-4" />
            Rời phòng
          </button>
        </div>

        {/* Right Column: Chat panel */}
        <div className="w-full lg:w-96 flex flex-col bg-surface border-t lg:border-t-0 lg:border-l border-white/10 h-96 lg:h-full">
          {/* Room info header */}
          <div className="p-4 border-b border-white/10 flex items-center justify-between shrink-0">
            <div>
              <h3 className="font-bold text-white text-sm md:text-base line-clamp-1">{room.name}</h3>
              <div className="flex items-center gap-2 mt-1 text-xs text-text-secondary">
                <span className="flex items-center gap-1">
                  <Users className="w-3.5 h-3.5 text-neon" />
                  {room.currentMemberCount}
                </span>
                <span className="flex items-center gap-1 font-bold text-neon cursor-pointer" onClick={handleCopyCode}>
                  Mã: {room.code} <Copy className="w-3 h-3" />
                </span>
              </div>
            </div>
          </div>

          {/* Chat Messages scroll area */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.map((msg) => {
              const isMe = msg.userId === user?.id;
              const isBot = msg.messageType === 'AI_RESPONSE';
              const isSystem = msg.messageType === 'SYSTEM';

              if (isSystem) {
                return (
                  <div key={msg.id} className="text-center text-xs text-text-secondary font-medium">
                    {msg.content}
                  </div>
                );
              }

              return (
                <div key={msg.id} className={`flex gap-2 ${isMe ? 'justify-end' : 'justify-start'}`}>
                  {!isMe && (
                    <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center text-sm shrink-0 border border-white/10">
                      {isBot ? '🤖' : msg.username.charAt(0)}
                    </div>
                  )}
                  <div className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                    <span className={`text-[10px] ${isBot ? 'text-neon font-bold' : 'text-text-secondary'}`}>
                      {msg.username}
                    </span>
                    <div className={`mt-1 p-3 rounded-xl text-sm max-w-[260px] ${
                      isMe ? 'bg-neon text-obsidian font-bold' : 
                      isBot ? 'bg-cyan-950/70 border border-cyan-800 text-white' : 'bg-white/5 text-white'
                    }`}>
                      {msg.messageType === 'SPOILER' && !revealedSpoilers[msg.id] ? (
                        <div 
                          className="flex items-center gap-1.5 text-red-500 font-bold cursor-pointer"
                          onClick={() => setRevealedSpoilers(p => ({ ...p, [msg.id]: true }))}
                        >
                          <Eye className="w-4 h-4" /> Spoiler. Bấm để xem!
                        </div>
                      ) : (
                        msg.content
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
            <div ref={messagesEndRef} />
          </div>

          {/* Chat Input form */}
          <form onSubmit={handleSendMessage} className="p-4 border-t border-white/10 space-y-2 shrink-0">
            <div className="flex items-center gap-2">
              <input 
                type="checkbox" 
                id="web-spoiler"
                checked={isSpoiler} 
                onChange={(e) => setIsSpoiler(e.target.checked)}
                className="accent-neon size-4"
              />
              <label htmlFor="web-spoiler" className="text-xs text-text-secondary flex items-center gap-1 cursor-pointer">
                <ShieldAlert className="w-3.5 h-3.5" /> Spoiler mờ nội dung
              </label>
            </div>
            <div className="flex gap-2">
              <input 
                type="text" 
                className="flex-1 bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-sm focus:outline-none focus:border-neon text-white"
                placeholder="Trò chuyện... (@AI để gọi bot)"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
              />
              <button 
                type="submit"
                className="p-2.5 rounded-xl bg-neon hover:bg-white text-obsidian transition-colors"
              >
                <Send className="w-4 h-4" />
              </button>
            </div>
          </form>
        </div>
      </main>
      <Footer />
    </div>
  );
};
