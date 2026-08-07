package com.tamdao.web_film_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamdao.web_film_backend.dto.request.CreateWatchRoomRequest;
import com.tamdao.web_film_backend.dto.response.*;
import com.tamdao.web_film_backend.service.WatchRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class WatchRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WatchRoomService roomService;

    private WatchRoomResponse mockRoomResponse;

    @BeforeEach
    void setUp() {
        mockRoomResponse = WatchRoomResponse.builder()
                .id(1L)
                .code("ROOM1234")
                .name("Phòng Test")
                .roomType("PUBLIC")
                .maxMembers(10)
                .currentMemberCount(1)
                .status("WAITING")
                .createdAt(LocalDateTime.now())
                .host(WatchRoomResponse.UserSummary.builder()
                        .id(1L)
                        .username("testuser")
                        .fullName("Test User")
                        .build())
                .movie(WatchRoomResponse.MovieSummary.builder()
                        .id(100L)
                        .title("Movie Test")
                        .slug("movie-test")
                        .build())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateRoomSuccessfully() throws Exception {
        CreateWatchRoomRequest request = new CreateWatchRoomRequest();
        request.setName("Phòng Test");
        request.setMovieId(100L);
        request.setRoomType("PUBLIC");
        request.setMaxMembers(10);

        Mockito.when(roomService.createRoom(any(CreateWatchRoomRequest.class), eq("testuser")))
                .thenReturn(mockRoomResponse);

        mockMvc.perform(post("/v1/watch-rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.code").value("ROOM1234"))
                .andExpect(jsonPath("$.data.name").value("Phòng Test"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetPublicRoomsSuccessfully() throws Exception {
        Mockito.when(roomService.getPublicRooms(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(mockRoomResponse)));

        mockMvc.perform(get("/v1/watch-rooms/public")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetRoomByIdSuccessfully() throws Exception {
        Mockito.when(roomService.getRoomById(eq(1L), eq("testuser")))
                .thenReturn(mockRoomResponse);

        mockMvc.perform(get("/v1/watch-rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetRoomByCodeSuccessfully() throws Exception {
        Mockito.when(roomService.getRoomByCode(eq("ROOM1234")))
                .thenReturn(mockRoomResponse);

        mockMvc.perform(get("/v1/watch-rooms/code/ROOM1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("ROOM1234"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldJoinRoomSuccessfully() throws Exception {
        WatchRoomMemberResponse mockMemberResponse = WatchRoomMemberResponse.builder()
                .id(1L)
                .roomId(1L)
                .userId(1L)
                .username("testuser")
                .role("MEMBER")
                .build();

        Mockito.when(roomService.joinRoom(eq(1L), eq("testuser")))
                .thenReturn(mockMemberResponse);

        mockMvc.perform(post("/v1/watch-rooms/1/join"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldLeaveRoomSuccessfully() throws Exception {
        mockMvc.perform(post("/v1/watch-rooms/1/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rời phòng thành công"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetSyncStateSuccessfully() throws Exception {
        VideoStateResponse mockState = VideoStateResponse.builder()
                .currentTime(125.5)
                .isPlaying(true)
                .playbackRate(1.0)
                .build();

        Mockito.when(roomService.getVideoState(eq(1L)))
                .thenReturn(mockState);

        mockMvc.perform(get("/v1/watch-rooms/1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentTime").value(125.5))
                .andExpect(jsonPath("$.data.isPlaying").value(true));
    }
}
