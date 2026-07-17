package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.response.NotificationResponse;
import com.tamdao.web_film_backend.dto.response.UnreadCountResponse;
import com.tamdao.web_film_backend.entity.Notification;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.entity.UserFavorite;
import com.tamdao.web_film_backend.entity.UserNotification;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.repository.NotificationRepository;
import com.tamdao.web_film_backend.repository.UserFavoriteRepository;
import com.tamdao.web_film_backend.repository.UserNotificationRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final UserFavoriteRepository favoriteRepository;

    /**
     * Get paginated notifications for the authenticated user.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String username, Pageable pageable) {
        User user = findUser(username);
        Page<UserNotification> userNotifications = userNotificationRepository.findByUserId(user.getId(), pageable);
        return userNotifications.map(this::toResponse);
    }

    /**
     * Mark a specific notification as read (with IDOR check).
     */
    @Transactional
    public void markAsRead(String username, Long userNotificationId) {
        User user = findUser(username);
        UserNotification userNotification = userNotificationRepository.findByIdAndUserId(userNotificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + userNotificationId));

        if (!userNotification.isRead()) {
            userNotification.setRead(true);
            userNotification.setReadAt(LocalDateTime.now());
            userNotificationRepository.save(userNotification);
            log.debug("Marked notification id={} as read for user={}", userNotificationId, username);
        }
    }

    /**
     * Mark all unread notifications of the user as read.
     */
    @Transactional
    public void markAllAsRead(String username) {
        User user = findUser(username);
        userNotificationRepository.markAllAsReadForUser(user.getId(), LocalDateTime.now());
        log.info("Marked all notifications as read for user={}", username);
    }

    /**
     * Get unread notifications count for the user.
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String username) {
        User user = findUser(username);
        long count = userNotificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return new UnreadCountResponse(count);
    }

    /**
     * Create notification for all users who favorited the movie.
     */
    @Transactional
    public void createNotificationForEpisode(String movieSlug, String movieTitle, String episodeName, String thumbUrl) {
        log.info("Creating database notification for movieSlug={}, episode={}", movieSlug, episodeName);
        List<UserFavorite> favorites = favoriteRepository.findByMovieSlug(movieSlug);
        if (favorites.isEmpty()) {
            log.debug("No favorites found for movieSlug={}. Skipping notification creation.", movieSlug);
            return;
        }

        Notification notification = Notification.builder()
                .title(movieTitle)
                .content("Đã cập nhật " + episodeName)
                .type("NEW_EPISODE")
                .movieSlug(movieSlug)
                .thumbUrl(thumbUrl)
                .build();
        Notification savedNotification = notificationRepository.save(notification);

        List<UserNotification> userNotifications = new ArrayList<>();
        for (UserFavorite fav : favorites) {
            userNotifications.add(UserNotification.builder()
                    .user(fav.getUser())
                    .notification(savedNotification)
                    .isRead(false)
                    .build());
        }

        userNotificationRepository.saveAll(userNotifications);
        log.info("Created {} database notifications for movieSlug={}", userNotifications.size(), movieSlug);
    }

    // ── Private helpers ──────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private NotificationResponse toResponse(UserNotification userNotif) {
        Notification notif = userNotif.getNotification();
        return NotificationResponse.builder()
                .id(userNotif.getId())
                .title(notif.getTitle())
                .content(notif.getContent())
                .type(notif.getType())
                .movieSlug(notif.getMovieSlug())
                .thumbUrl(notif.getThumbUrl())
                .isRead(userNotif.isRead())
                .createdAt(userNotif.getCreatedAt())
                .build();
    }
}
