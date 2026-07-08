package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.request.CreateWatchRoomRequest;
import com.tamdao.web_film_backend.dto.response.WatchRoomResponse;
import com.tamdao.web_film_backend.entity.*;
import com.tamdao.web_film_backend.exception.BadRequestException;
import com.tamdao.web_film_backend.exception.ResourceNotFoundException;
import com.tamdao.web_film_backend.mapper.WatchRoomMapper;
import com.tamdao.web_film_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class WatchRoomServiceTest {

    @Mock
    private WatchRoomRepository roomRepository;
    @Mock
    private WatchRoomMemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private WatchRoomMapper roomMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private WatchRoomService watchRoomService;

    private User mockUser;
    private Movie mockMovie;
    private WatchRoom mockRoom;
    private WatchRoomResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .build();

        mockMovie = Movie.builder()
                .id(100L)
                .title("Movie Test")
                .slug("movie-test")
                .build();

        mockRoom = WatchRoom.builder()
                .id(1L)
                .code("ROOM1234")
                .name("Phòng Test")
                .host(mockUser)
                .movie(mockMovie)
                .roomType(RoomType.PUBLIC)
                .maxMembers(10)
                .status(RoomStatus.WAITING)
                .build();

        mockResponse = WatchRoomResponse.builder()
                .id(1L)
                .code("ROOM1234")
                .name("Phòng Test")
                .roomType("PUBLIC")
                .maxMembers(10)
                .status("WAITING")
                .build();
    }

    @Test
    void shouldCreateRoomSuccessfully() {
        CreateWatchRoomRequest request = new CreateWatchRoomRequest();
        request.setName("Phòng Test");
        request.setMovieId(100L);
        request.setRoomType("PUBLIC");
        request.setMaxMembers(10);

        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(movieRepository.findById(100L))
                .thenReturn(Optional.of(mockMovie));
        Mockito.when(roomRepository.findByCode(anyString()))
                .thenReturn(Optional.empty());
        Mockito.when(roomRepository.save(any(WatchRoom.class)))
                .thenReturn(mockRoom);
        Mockito.when(roomMapper.toResponse(any(WatchRoom.class)))
                .thenReturn(mockResponse);
        Mockito.when(redisTemplate.opsForHash())
                .thenReturn(hashOperations);

        WatchRoomResponse response = watchRoomService.createRoom(request, "testuser");

        assertNotNull(response);
        assertEquals("ROOM1234", response.getCode());
        assertEquals("Phòng Test", response.getName());

        Mockito.verify(roomRepository).save(any(WatchRoom.class));
        Mockito.verify(memberRepository).save(any(WatchRoomMember.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        CreateWatchRoomRequest request = new CreateWatchRoomRequest();
        request.setName("Phòng Test");
        request.setMovieId(100L);
        request.setRoomType("PUBLIC");

        Mockito.when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            watchRoomService.createRoom(request, "unknown")
        );
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        CreateWatchRoomRequest request = new CreateWatchRoomRequest();
        request.setName("Phòng Test");
        request.setMovieId(999L);
        request.setRoomType("PUBLIC");

        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(movieRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            watchRoomService.createRoom(request, "testuser")
        );
    }

    @Test
    void shouldThrowExceptionWhenRoomTypeInvalid() {
        CreateWatchRoomRequest request = new CreateWatchRoomRequest();
        request.setName("Phòng Test");
        request.setMovieId(100L);
        request.setRoomType("INVALID_TYPE");

        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(movieRepository.findById(100L))
                .thenReturn(Optional.of(mockMovie));

        assertThrows(BadRequestException.class, () -> 
            watchRoomService.createRoom(request, "testuser")
        );
    }
}
