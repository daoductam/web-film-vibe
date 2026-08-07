package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.CreateWatchRoomRequest;
import com.tamdao.web_film_backend.dto.response.WatchRoomMemberResponse;
import com.tamdao.web_film_backend.dto.response.WatchRoomResponse;
import com.tamdao.web_film_backend.dto.response.VideoStateResponse;
import com.tamdao.web_film_backend.dto.response.ChatMessageResponse;
import com.tamdao.web_film_backend.dto.response.WatchPartyHistoryResponse;
import com.tamdao.web_film_backend.entity.*;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.mapper.WatchRoomMapper;
import com.tamdao.web_film_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchRoomService {

    private final WatchRoomRepository roomRepository;
    private final WatchRoomMemberRepository memberRepository;
    private final WatchRoomMessageRepository messageRepository;
    private final WatchPartyHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final EpisodeRepository episodeRepository;
    private final WatchRoomMapper roomMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String ROOM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String STATE_PREFIX = "watchroom:%d:state";
    private static final String MEMBERS_PREFIX = "watchroom:%d:members";

    @Transactional
    public WatchRoomResponse createRoom(CreateWatchRoomRequest request, String username) {
        log.info("User {} creating watch room for movie ID {}", username, request.getMovieId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Episode episode = null;
        if (request.getEpisodeId() != null) {
            episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Episode", "id", request.getEpisodeId()));
        }

        RoomType roomType;
        try {
            roomType = RoomType.valueOf(request.getRoomType().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Loại phòng không hợp lệ (PUBLIC/PRIVATE)");
        }

        String code = generateUniqueCode();

        WatchRoom room = WatchRoom.builder()
                .code(code)
                .name(request.getName())
                .host(user)
                .movie(movie)
                .episode(episode)
                .roomType(roomType)
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 50)
                .status(RoomStatus.WAITING)
                .build();

        room = roomRepository.save(room);

        // Add creator as Host member
        WatchRoomMember member = WatchRoomMember.builder()
                .room(room)
                .user(user)
                .role(MemberRole.HOST)
                .build();

        memberRepository.save(member);

        // Initialize Redis state
        initializeRedisState(room.getId());

        WatchRoomResponse response = roomMapper.toResponse(room);
        response.setCurrentMemberCount(1);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<WatchRoomResponse> getPublicRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findByRoomTypeAndStatusNotOrderByCreatedAtDesc(
                RoomType.PUBLIC, RoomStatus.ENDED, pageable
        ).map(room -> {
            WatchRoomResponse response = roomMapper.toResponse(room);
            response.setCurrentMemberCount(memberRepository.countByRoomIdAndLeftAtIsNull(room.getId()));
            return response;
        });
    }

    @Transactional(readOnly = true)
    public WatchRoomResponse getRoomById(Long roomId, String username) {
        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        // Membership check for private rooms
        if (RoomType.PRIVATE.equals(room.getRoomType())) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                    .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền truy cập phòng riêng tư này"));
        }

        WatchRoomResponse response = roomMapper.toResponse(room);
        response.setCurrentMemberCount(memberRepository.countByRoomIdAndLeftAtIsNull(roomId));
        return response;
    }

    @Transactional(readOnly = true)
    public WatchRoomResponse getRoomByCode(String code) {
        WatchRoom room = roomRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "code", code));

        WatchRoomResponse response = roomMapper.toResponse(room);
        response.setCurrentMemberCount(memberRepository.countByRoomIdAndLeftAtIsNull(room.getId()));
        return response;
    }

    @Transactional
    public WatchRoomMemberResponse joinRoom(Long roomId, String username) {
        log.info("User {} joining watch room {}", username, roomId);

        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        if (RoomStatus.ENDED.equals(room.getStatus())) {
            throw new BadRequestException("Phòng xem phim này đã kết thúc");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Optional<WatchRoomMember> existingMemberOpt = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId());
        if (existingMemberOpt.isPresent()) {
            return roomMapper.toMemberResponse(existingMemberOpt.get());
        }

        int activeMembersCount = memberRepository.countByRoomIdAndLeftAtIsNull(roomId);
        if (activeMembersCount >= room.getMaxMembers()) {
            throw new BadRequestException("Phòng xem phim đã đạt số lượng thành viên tối đa");
        }

        WatchRoomMember member = WatchRoomMember.builder()
                .room(room)
                .user(user)
                .role(MemberRole.MEMBER)
                .build();

        member = memberRepository.save(member);

        // Presence update in Redis
        String membersKey = String.format(MEMBERS_PREFIX, roomId);
        redisTemplate.opsForSet().add(membersKey, String.valueOf(user.getId()));
        redisTemplate.expire(membersKey, 24, TimeUnit.HOURS);

        return roomMapper.toMemberResponse(member);
    }

    @Transactional
    public void leaveRoom(Long roomId, String username) {
        log.info("User {} leaving watch room {}", username, roomId);

        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        WatchRoomMember member = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoomMember", "userId", user.getId()));

        member.setLeftAt(LocalDateTime.now());
        memberRepository.save(member);

        // Redis presence clean
        String membersKey = String.format(MEMBERS_PREFIX, roomId);
        redisTemplate.opsForSet().remove(membersKey, String.valueOf(user.getId()));

        // Save history record
        int watchDuration = getWatchDuration(roomId, member.getJoinedAt());
        WatchPartyHistory history = WatchPartyHistory.builder()
                .room(room)
                .user(user)
                .movie(room.getMovie())
                .watchDurationSeconds(watchDuration)
                .joinedAt(member.getJoinedAt())
                .leftAt(member.getLeftAt())
                .build();
        historyRepository.save(history);

        // End the room if the host leaves, OR if this was the last member still in the room
        boolean isHost = MemberRole.HOST.equals(member.getRole());
        int remainingMembers = memberRepository.countByRoomIdAndLeftAtIsNull(roomId);
        if (isHost || remainingMembers == 0) {
            log.info("Ending watch room {} — reason: {} left and {} member(s) remain",
                    roomId, isHost ? "HOST" : "last member", remainingMembers);
            endRoomInternal(room);
        }
    }

    @Transactional
    public void endRoom(Long roomId, String username) {
        log.info("Host {} ending watch room {}", username, roomId);

        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        WatchRoomMember member = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        if (!MemberRole.HOST.equals(member.getRole())) {
            throw new AccessDeniedException("Chỉ chủ phòng (HOST) mới được phép kết thúc phòng");
        }

        endRoomInternal(room);
    }

    @Transactional(readOnly = true)
    public VideoStateResponse getVideoState(Long roomId) {
        String stateKey = String.format(STATE_PREFIX, roomId);
        Map<Object, Object> state = redisTemplate.opsForHash().entries(stateKey);

        if (state.isEmpty()) {
            return VideoStateResponse.builder()
                    .currentTime(0.0)
                    .isPlaying(false)
                    .playbackRate(1.0)
                    .lastSyncAt(LocalDateTime.now())
                    .build();
        }

        return VideoStateResponse.builder()
                .currentTime(Double.valueOf((String) state.get("currentTime")))
                .isPlaying(Boolean.valueOf((String) state.get("isPlaying")))
                .playbackRate(Double.valueOf((String) state.get("playbackRate")))
                .lastSyncAt(LocalDateTime.parse((String) state.get("lastSyncAt")))
                .build();
    }

    @Transactional
    public void updateMemberRole(Long roomId, Long targetUserId, String roleStr, String requesterName) {
        WatchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoom", "id", roomId));

        User requester = userRepository.findByUsername(requesterName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", requesterName));

        WatchRoomMember requesterMember = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, requester.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không phải thành viên phòng này"));

        if (!MemberRole.HOST.equals(requesterMember.getRole())) {
            throw new AccessDeniedException("Chỉ chủ phòng (HOST) mới được phép thay đổi quyền hạn");
        }

        WatchRoomMember targetMember = memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("WatchRoomMember", "userId", targetUserId));

        MemberRole newRole;
        try {
            newRole = MemberRole.valueOf(roleStr.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Vai trò không hợp lệ (CO_HOST/MEMBER)");
        }

        if (MemberRole.HOST.equals(newRole)) {
            throw new BadRequestException("Không thể gán quyền HOST trực tiếp");
        }

        targetMember.setRole(newRole);
        memberRepository.save(targetMember);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long roomId, String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        memberRepository.findByRoomIdAndUserIdAndLeftAtIsNull(roomId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn phải tham gia phòng để xem tin nhắn"));

        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(msg -> ChatMessageResponse.builder()
                        .id(msg.getId())
                        .userId(msg.getUser() != null ? msg.getUser().getId() : null)
                        .username(msg.getUser() != null ? msg.getUser().getUsername() : "CineBot 🤖")
                        .avatarUrl(msg.getUser() != null ? msg.getUser().getAvatarUrl() : null)
                        .content(msg.getContent())
                        .messageType(msg.getMessageType().name())
                        .timestamp(msg.getCreatedAt())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<WatchPartyHistoryResponse> getWatchPartyHistory(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findByUserIdOrderByLeftAtDesc(user.getId(), pageable)
                .map(roomMapper::toHistoryResponse);
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private String generateUniqueCode() {
        for (int i = 0; i < 5; i++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int j = 0; j < CODE_LENGTH; j++) {
                sb.append(ROOM_CODE_CHARS.charAt(RANDOM.nextInt(ROOM_CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!roomRepository.findByCode(code).isPresent()) {
                return code;
            }
        }
        throw new RuntimeException("Lỗi tạo mã phòng duy nhất, vui lòng thử lại");
    }

    private void initializeRedisState(Long roomId) {
        String stateKey = String.format(STATE_PREFIX, roomId);
        Map<String, String> state = new HashMap<>();
        state.put("currentTime", "0.0");
        state.put("isPlaying", "false");
        state.put("playbackRate", "1.0");
        state.put("lastSyncAt", LocalDateTime.now().toString());

        redisTemplate.opsForHash().putAll(stateKey, state);
        redisTemplate.expire(stateKey, 24, TimeUnit.HOURS);
    }

    private void endRoomInternal(WatchRoom room) {
        room.setStatus(RoomStatus.ENDED);
        room.setEndedAt(LocalDateTime.now());
        roomRepository.save(room);

        // Mark remaining online members as left
        List<WatchRoomMember> activeMembers = memberRepository.findByRoomIdAndLeftAtIsNull(room.getId());
        LocalDateTime now = LocalDateTime.now();
        for (WatchRoomMember m : activeMembers) {
            m.setLeftAt(now);
            memberRepository.save(m);

            // Record history
            int watchDuration = getWatchDuration(room.getId(), m.getJoinedAt());
            WatchPartyHistory history = WatchPartyHistory.builder()
                    .room(room)
                    .user(m.getUser())
                    .movie(room.getMovie())
                    .watchDurationSeconds(watchDuration)
                    .joinedAt(m.getJoinedAt())
                    .leftAt(now)
                    .build();
            historyRepository.save(history);
        }

        // Redis cleanup
        redisTemplate.delete(String.format(STATE_PREFIX, room.getId()));
        redisTemplate.delete(String.format(MEMBERS_PREFIX, room.getId()));
    }

    private int getWatchDuration(Long roomId, LocalDateTime joinedAt) {
        try {
            // Ephemeral duration estimation
            return (int) java.time.Duration.between(joinedAt, LocalDateTime.now()).toSeconds();
        } catch (Exception e) {
            return 0;
        }
    }
}
