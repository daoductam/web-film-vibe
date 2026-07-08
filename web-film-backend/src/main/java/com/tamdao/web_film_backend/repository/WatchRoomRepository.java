package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.RoomStatus;
import com.tamdao.web_film_backend.entity.RoomType;
import com.tamdao.web_film_backend.entity.WatchRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchRoomRepository extends JpaRepository<WatchRoom, Long> {
    Optional<WatchRoom> findByCode(String code);
    Page<WatchRoom> findByRoomTypeAndStatusNotOrderByCreatedAtDesc(RoomType roomType, RoomStatus status, Pageable pageable);
}
