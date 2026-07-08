package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.request.CreateWatchRoomRequest;
import com.tamdao.web_film_backend.dto.response.*;
import com.tamdao.web_film_backend.service.WatchRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/watch-rooms")
@RequiredArgsConstructor
@Tag(name = "Watch Party", description = "Endpoints for real-time Watch Party rooms")
public class WatchRoomController {

    private final WatchRoomService roomService;

    @PostMapping
    @Operation(summary = "Tạo phòng xem phim chung mới")
    public ResponseEntity<ApiResponse<WatchRoomResponse>> createRoom(
            @Valid @RequestBody CreateWatchRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        WatchRoomResponse response = roomService.createRoom(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<WatchRoomResponse>builder()
                .success(true)
                .message("Tạo phòng xem phim chung thành công")
                .data(response)
                .build());
    }

    @GetMapping("/public")
    @Operation(summary = "Lấy danh sách các phòng công khai")
    public ResponseEntity<ApiResponse<Page<WatchRoomResponse>>> getPublicRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<WatchRoomResponse> rooms = roomService.getPublicRooms(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<WatchRoomResponse>>builder()
                .success(true)
                .message("Lấy danh sách phòng công khai thành công")
                .data(rooms)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết phòng bằng ID")
    public ResponseEntity<ApiResponse<WatchRoomResponse>> getRoomById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        WatchRoomResponse response = roomService.getRoomById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<WatchRoomResponse>builder()
                .success(true)
                .message("Lấy thông tin phòng thành công")
                .data(response)
                .build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Tìm thông tin phòng bằng mã phòng")
    public ResponseEntity<ApiResponse<WatchRoomResponse>> getRoomByCode(@PathVariable String code) {
        WatchRoomResponse response = roomService.getRoomByCode(code);
        return ResponseEntity.ok(ApiResponse.<WatchRoomResponse>builder()
                .success(true)
                .message("Tìm thấy phòng xem phim")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/join")
    @Operation(summary = "Tham gia phòng xem phim")
    public ResponseEntity<ApiResponse<WatchRoomMemberResponse>> joinRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        WatchRoomMemberResponse response = roomService.joinRoom(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<WatchRoomMemberResponse>builder()
                .success(true)
                .message("Tham gia phòng thành công")
                .data(response)
                .build());
    }

    @PostMapping("/{id}/leave")
    @Operation(summary = "Rời phòng xem phim")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        roomService.leaveRoom(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Rời phòng thành công")
                .build());
    }

    @PutMapping("/{id}/end")
    @Operation(summary = "Host kết thúc phòng xem phim")
    public ResponseEntity<ApiResponse<Void>> endRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        roomService.endRoom(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Kết thúc phòng thành công")
                .build());
    }

    @GetMapping("/{id}/state")
    @Operation(summary = "Lấy trạng thái video hiện tại (sync state)")
    public ResponseEntity<ApiResponse<VideoStateResponse>> getVideoState(@PathVariable Long id) {
        VideoStateResponse response = roomService.getVideoState(id);
        return ResponseEntity.ok(ApiResponse.<VideoStateResponse>builder()
                .success(true)
                .message("Lấy trạng thái đồng bộ thành công")
                .data(response)
                .build());
    }

    @PutMapping("/{id}/members/{userId}/role")
    @Operation(summary = "Cập nhật vai trò thành viên (HOST only)")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam String role,
            @AuthenticationPrincipal UserDetails userDetails) {
        roomService.updateMemberRole(id, userId, role, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Cập nhật quyền hạn thành viên thành công")
                .build());
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Lấy lịch sử tin nhắn chat trong phòng")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<ChatMessageResponse> messages = roomService.getMessages(id, userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.<Page<ChatMessageResponse>>builder()
                .success(true)
                .message("Lấy lịch sử chat thành công")
                .data(messages)
                .build());
    }

    @GetMapping("/history")
    @Operation(summary = "Lấy lịch sử tham gia Watch Party của user")
    public ResponseEntity<ApiResponse<Page<WatchPartyHistoryResponse>>> getWatchPartyHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Page<WatchPartyHistoryResponse> history = roomService.getWatchPartyHistory(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.<Page<WatchPartyHistoryResponse>>builder()
                .success(true)
                .message("Lấy lịch sử xem nhóm thành công")
                .data(history)
                .build());
    }
}
