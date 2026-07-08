package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.ChatMessageRequest;
import com.tamdao.web_film_backend.dto.request.SignalingMessage;
import com.tamdao.web_film_backend.dto.request.SyncCommand;
import com.tamdao.web_film_backend.dto.response.ChatMessageResponse;
import com.tamdao.web_film_backend.entity.*;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.repository.UserRepository;
import com.tamdao.web_film_backend.repository.WatchRoomMemberRepository;
import com.tamdao.web_film_backend.repository.WatchRoomMessageRepository;
import com.tamdao.web_film_backend.repository.WatchRoomRepository;
import com.tamdao.web_film_backend.service.ai.GroqApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchRoomChatService {

    private final WatchRoomRepository roomRepository;
    private final WatchRoomMemberRepository memberRepository;
    private final WatchRoomMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final GroqApiClient groqApiClient;

    private static final String STATE_PREFIX = "watchroom:%d:state";
    private static final String AI_COOLDOWN_PREFIX = "watchroom:%d:ai-cooldown:%d";

    @Transactional
    public void processChat(Long roomId, String username, ChatMessageRequest request) {
        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        MessageType msgType;
        try {
            msgType = MessageType.valueOf(request.getMessageType().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Loại tin nhắn không hợp lệ (CHAT/SPOILER)");
        }

        WatchRoomMessage message = WatchRoomMessage.builder()
                .room(room)
                .user(user)
                .content(request.getContent())
                .messageType(msgType)
                .build();

        message = messageRepository.save(message);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(message.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .content(message.getContent())
                .messageType(message.getMessageType().name())
                .timestamp(message.getCreatedAt())
                .build();

        // Broadcast to room chat topic
        messagingTemplate.convertAndSend(String.format("/topic/room.%d.chat", roomId), (Object) response);

        // Check if triggers AI Bot
        if (request.getContent().trim().startsWith("@AI")) {
            triggerAiBot(room, user, request.getContent().trim());
        }
    }

    @Transactional
    public void processSyncCommand(Long roomId, String username, SyncCommand command) {
        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        WatchRoomMember member = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        if (!MemberRole.HOST.equals(member.getRole()) && !MemberRole.CO_HOST.equals(member.getRole())) {
            throw new AccessDeniedException("Chỉ HOST hoặc CO_HOST mới có quyền điều khiển video");
        }

        // Save sync state to Redis
        String stateKey = String.format(STATE_PREFIX, roomId);
        Map<String, String> state = new HashMap<>();
        state.put("currentTime", String.valueOf(command.getTimestamp()));
        state.put("isPlaying", String.valueOf("PLAY".equalsIgnoreCase(command.getAction())));
        state.put("playbackRate", "1.0");
        state.put("lastSyncAt", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(stateKey, state);

        // Map Issued By details
        Map<String, Object> syncPayload = new HashMap<>();
        syncPayload.put("action", command.getAction().toUpperCase());
        syncPayload.put("timestamp", command.getTimestamp());
        syncPayload.put("serverTime", LocalDateTime.now().toString());
        
        Map<String, Object> issuedBy = new HashMap<>();
        issuedBy.put("id", user.getId());
        issuedBy.put("username", user.getUsername());
        syncPayload.put("issuedBy", issuedBy);

        // Broadcast sync state
        messagingTemplate.convertAndSend(String.format("/topic/room.%d.sync", roomId), (Object) syncPayload);
    }

    @Transactional(readOnly = true)
    public void processReaction(Long roomId, String username, String emoji) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        Map<String, Object> reactionPayload = new HashMap<>();
        reactionPayload.put("userId", user.getId());
        reactionPayload.put("username", user.getUsername());
        reactionPayload.put("emoji", emoji);
        reactionPayload.put("timestamp", LocalDateTime.now().toString());

        // Broadcast reaction
        messagingTemplate.convertAndSend(String.format("/topic/room.%d.reaction", roomId), (Object) reactionPayload);
    }

    @Transactional(readOnly = true)
    public void processSignaling(Long roomId, String username, SignalingMessage signal) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        Map<String, Object> signalPayload = new HashMap<>();
        signalPayload.put("senderUserId", user.getId());
        signalPayload.put("type", signal.getType().toUpperCase());
        signalPayload.put("payload", signal.getPayload());

        // Direct forward to target user if specified, otherwise broadcast to all
        if (signal.getTargetUserId() != null) {
            signalPayload.put("targetUserId", signal.getTargetUserId());
            messagingTemplate.convertAndSend(
                    String.format("/topic/room.%d.signal", roomId),
                    (Object) signalPayload
            );
        } else {
            messagingTemplate.convertAndSend(String.format("/topic/room.%d.signal", roomId), (Object) signalPayload);
        }
    }

    // ── AI Integration ──────────────────────────────────────────────────────

    private void triggerAiBot(WatchRoom room, User user, String userPrompt) {
        String cooldownKey = String.format(AI_COOLDOWN_PREFIX, room.getId(), user.getId());
        Boolean onCooldown = redisTemplate.hasKey(cooldownKey);
        
        if (Boolean.TRUE.equals(onCooldown)) {
            sendSystemMessage(room.getId(), "Vui lòng đợi 3 giây trước khi gọi tiếp AI bot.");
            return;
        }

        // Apply 3 seconds cooldown per user in room
        redisTemplate.opsForValue().set(cooldownKey, "active", 3, TimeUnit.SECONDS);

        // Remove "@AI" prefix to get clean question
        String question = userPrompt.substring(3).trim();
        if (question.isEmpty()) {
            sendSystemMessage(room.getId(), "CineBot sẵn sàng! Hãy hỏi: @AI <câu hỏi của bạn> để bắt đầu.");
            return;
        }

        // Call async to avoid blocking Stomp handler
        processAiResponseAsync(room, question);
    }

    @Async
    protected void processAiResponseAsync(WatchRoom room, String question) {
        log.info("WatchRoom {} processing async AI request for question: {}", room.getId(), question);

        String systemPrompt = String.format(
                "Bạn là CineBot, trợ lý AI trong phòng xem phim chung của CineStream. " +
                "Trả lời thật ngắn gọn, vui vẻ, thân thiện, dùng nhiều emoji. Tối đa 200 ký tự. " +
                "Phim cả phòng đang xem: %s, mô tả phim: %s, năm phát hành: %d.",
                room.getMovie().getTitle(),
                room.getMovie().getDescription() != null ? room.getMovie().getDescription().replaceAll("<[^>]*>", "") : "Không có mô tả",
                room.getMovie().getYear()
        );

        try {
            // Groq expects JSON output formats natively because of configuration in GroqApiClient.
            // We wrapper the prompt so it returns valid JSON.
            String wrappedSystemPrompt = systemPrompt + "\nĐầu ra phải là một đối tượng JSON có dạng: {\"reply\": \"câu trả lời của bạn\"}";
            
            GroqApiClient.GroqMessage systemMsg = new GroqApiClient.GroqMessage("system", wrappedSystemPrompt);
            GroqApiClient.GroqMessage userMsg = new GroqApiClient.GroqMessage("user", question);

            GroqApiClient.GroqResponse groqResponse = groqApiClient.callChatCompletion(List.of(systemMsg, userMsg));
            String rawJson = groqResponse.getFirstMessageContent();
            
            String reply = "Xin lỗi, tôi gặp chút sự cố khi phân tích câu trả lời.";
            if (rawJson != null && rawJson.contains("reply")) {
                // Fuzzy extract json value to prevent parser dependency conflicts
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"reply\"\\s*:\\s*\"([^\"]+)\"").matcher(rawJson);
                if (m.find()) {
                    reply = m.group(1);
                } else {
                    reply = rawJson;
                }
            }

            // Save Bot message to DB
            WatchRoomMessage botMessage = WatchRoomMessage.builder()
                    .room(room)
                    .user(null) // null = bot
                    .content(reply)
                    .messageType(MessageType.AI_RESPONSE)
                    .build();

            botMessage = messageRepository.save(botMessage);

            ChatMessageResponse response = ChatMessageResponse.builder()
                    .id(botMessage.getId())
                    .userId(null)
                    .username("CineBot 🤖")
                    .avatarUrl("/bot-avatar.png")
                    .content(botMessage.getContent())
                    .messageType(botMessage.getMessageType().name())
                    .timestamp(botMessage.getCreatedAt())
                    .build();

            messagingTemplate.convertAndSend(String.format("/topic/room.%d.chat", room.getId()), response);

        } catch (Exception e) {
            log.error("AI Assistant watchparty call failed", e);
            sendSystemMessage(room.getId(), "CineBot hiện đang bận. Bạn vui lòng thử lại sau.");
        }
    }

    private void sendSystemMessage(Long roomId, String content) {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(0L)
                .userId(null)
                .username("Hệ thống ⚙️")
                .content(content)
                .messageType(MessageType.SYSTEM.name())
                .timestamp(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend(String.format("/topic/room.%d.chat", roomId), response);
    }
}
