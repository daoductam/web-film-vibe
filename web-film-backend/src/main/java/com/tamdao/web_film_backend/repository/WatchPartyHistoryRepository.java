package com.tamdao.web_film_backend.repository;

import com.tamdao.web_film_backend.entity.WatchPartyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchPartyHistoryRepository extends JpaRepository<WatchPartyHistory, Long> {
    Page<WatchPartyHistory> findByUserIdOrderByLeftAtDesc(Long userId, Pageable pageable);
}
