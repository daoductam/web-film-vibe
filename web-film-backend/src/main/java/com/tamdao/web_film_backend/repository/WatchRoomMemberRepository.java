package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.WatchRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchRoomMemberRepository extends JpaRepository<WatchRoomMember, Long> {
    Optional<WatchRoomMember> findByRoomIdAndUserIdAndLeftAtIsNull(Long roomId, Long userId);
    List<WatchRoomMember> findByRoomIdAndLeftAtIsNull(Long roomId);
    int countByRoomIdAndLeftAtIsNull(Long roomId);
}
