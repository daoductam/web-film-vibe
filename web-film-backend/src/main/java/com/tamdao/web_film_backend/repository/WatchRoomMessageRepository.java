package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.WatchRoomMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchRoomMessageRepository extends JpaRepository<WatchRoomMessage, Long> {
    Page<WatchRoomMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);
}
