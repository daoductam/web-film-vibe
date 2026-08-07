package com.tamdao.web_film_backend.controller;

import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.NotificationResponse;
import com.tamdao.web_film_backend.dto.response.PageInfo;
import com.tamdao.web_film_backend.dto.response.UnreadCountResponse;
import com.tamdao.web_film_backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Manage user notifications history")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get paginated notification inbox for current user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications, createPageInfo(notifications)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu thông báo là đã đọc"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all unread notifications of the user as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications for current user")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        UnreadCountResponse response = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private PageInfo createPageInfo(Page<?> page) {
        return PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .itemsPerPage(page.getSize())
                .build();
    }
}
