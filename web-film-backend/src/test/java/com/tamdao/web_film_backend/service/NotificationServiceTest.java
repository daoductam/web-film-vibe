package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.response.NotificationResponse;
import com.tamdao.web_film_backend.dto.response.UnreadCountResponse;
import com.tamdao.web_film_backend.entity.Notification;
import com.tamdao.web_film_backend.entity.User;
import com.tamdao.web_film_backend.entity.UserNotification;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.repository.NotificationRepository;
import com.tamdao.web_film_backend.repository.UserFavoriteRepository;
import com.tamdao.web_film_backend.repository.UserNotificationRepository;
import com.tamdao.web_film_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserFavoriteRepository favoriteRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User mockUser;
    private Notification mockNotification;
    private UserNotification mockUserNotification;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        mockNotification = Notification.builder()
                .id(10L)
                .title("New Movie")
                .content("Episode 1 has updated")
                .type("NEW_EPISODE")
                .movieSlug("one-piece")
                .thumbUrl("http://image.png")
                .createdAt(LocalDateTime.now())
                .build();

        mockUserNotification = UserNotification.builder()
                .id(100L)
                .user(mockUser)
                .notification(mockNotification)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGetNotificationsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserNotification> page = new PageImpl<>(List.of(mockUserNotification));

        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(userNotificationRepository.findByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<NotificationResponse> result = notificationService.getUserNotifications("testuser", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        NotificationResponse responseDto = result.getContent().get(0);
        assertEquals(100L, responseDto.getId());
        assertEquals("New Movie", responseDto.getTitle());
        assertFalse(responseDto.isRead());
    }

    @Test
    void shouldMarkAsReadSuccessfully() {
        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(userNotificationRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(mockUserNotification));
        Mockito.when(userNotificationRepository.save(any(UserNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.markAsRead("testuser", 100L);

        assertTrue(mockUserNotification.isRead());
        assertNotNull(mockUserNotification.getReadAt());
        Mockito.verify(userNotificationRepository).save(mockUserNotification);
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFoundOrNotBelongingToUser() {
        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(userNotificationRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.markAsRead("testuser", 100L)
        );
    }

    @Test
    void shouldMarkAllAsReadSuccessfully() {
        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));

        notificationService.markAllAsRead("testuser");

        Mockito.verify(userNotificationRepository).markAllAsReadForUser(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void shouldGetUnreadCountSuccessfully() {
        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(userNotificationRepository.countByUserIdAndIsReadFalse(1L))
                .thenReturn(5L);

        UnreadCountResponse response = notificationService.getUnreadCount("testuser");

        assertNotNull(response);
        assertEquals(5L, response.getUnreadCount());
    }
}
