package com.tamdao.web_film_backend.websocket;

import com.tamdao.web_film_backend.dto.request.ChatMessageRequest;
import com.tamdao.web_film_backend.dto.request.ReactionRequest;
import com.tamdao.web_film_backend.dto.request.SignalingMessage;
import com.tamdao.web_film_backend.dto.request.SyncCommand;
import com.tamdao.web_film_backend.service.WatchRoomChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WatchRoomWebSocketHandler {

    private final WatchRoomChatService chatService;

    @MessageMapping("/room.{roomId}.chat")
    public void handleChat(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest request,
            Principal principal) {
        log.info("WebSocket chat received on room {}: {}", roomId, request.getContent());
        chatService.processChat(roomId, principal.getName(), request);
    }

    @MessageMapping("/room.{roomId}.sync")
    public void handleSync(
            @DestinationVariable Long roomId,
            @Payload SyncCommand command,
            Principal principal) {
        log.info("WebSocket sync command received on room {}: {}", roomId, command.getAction());
        chatService.processSyncCommand(roomId, principal.getName(), command);
    }

    @MessageMapping("/room.{roomId}.reaction")
    public void handleReaction(
            @DestinationVariable Long roomId,
            @Payload ReactionRequest request,
            Principal principal) {
        String emoji = request != null ? request.getEmoji() : "";
        log.info("WebSocket emoji reaction received on room {}: {}", roomId, emoji);
        chatService.processReaction(roomId, principal.getName(), emoji);
    }

    @MessageMapping("/room.{roomId}.signal")
    public void handleSignaling(
            @DestinationVariable Long roomId,
            @Payload SignalingMessage message,
            Principal principal) {
        log.info("WebSocket Signaling message of type {} received on room {}", message.getType(), roomId);
        chatService.processSignaling(roomId, principal.getName(), message);
    }
}
