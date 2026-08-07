package com.tamdao.web_film_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamdao.web_film_backend.dto.response.ApiResponse;
import com.tamdao.web_film_backend.dto.response.NotificationResponse;
import com.tamdao.web_film_backend.dto.response.UnreadCountResponse;
import com.tamdao.web_film_backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = NotificationResponse.builder()
                .id(100L)
                .title("New Episode")
                .content("Episode 5 is out!")
                .type("NEW_EPISODE")
                .movieSlug("one-piece")
                .thumbUrl("http://img.url")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetNotificationsSuccessfully() throws Exception {
        Mockito.when(notificationService.getUserNotifications(eq("testuser"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(mockResponse)));

        mockMvc.perform(get("/v1/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(100L))
                .andExpect(jsonPath("$.data.content[0].title").value("New Episode"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldMarkAsReadSuccessfully() throws Exception {
        Mockito.doNothing().when(notificationService).markAsRead("testuser", 100L);

        mockMvc.perform(patch("/v1/notifications/100/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã đánh dấu thông báo là đã đọc"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldMarkAllAsReadSuccessfully() throws Exception {
        Mockito.doNothing().when(notificationService).markAllAsRead("testuser");

        mockMvc.perform(post("/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetUnreadCountSuccessfully() throws Exception {
        Mockito.when(notificationService.getUnreadCount("testuser"))
                .thenReturn(new UnreadCountResponse(3L));

        mockMvc.perform(get("/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(3L));
    }
}
