package com.tamdao.web_film_backend.service;

import com.tamdao.web_film_backend.dto.event.EpisodeUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final FcmNotificationService fcmNotificationService;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.redis-dedup-ttl-hours:24}")
    private int dedupTtlHours;

    /**
     * Listens to EpisodeUpdateEvent after the transaction has successfully committed.
     * Runs asynchronously in a separate task executor.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEpisodeUpdateEvent(EpisodeUpdateEvent event) {
        String movieSlug = event.getMovieSlug();
        String episodeName = event.getEpisodeName();
        
        // Dedup key pattern: notif:movie:{slug}:ep:{name}
        String dedupKey = String.format("notif:movie:%s:ep:%s", movieSlug, episodeName.toLowerCase().replace(" ", ""));
        
        try {
            // SetIfAbsent acts as a lock (NX in Redis). If it returns true, this is the first crawl of this episode.
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(dedupKey, "sent", dedupTtlHours, TimeUnit.HOURS);
            
            if (Boolean.TRUE.equals(isNew)) {
                log.info("Episode event processing passed dedup: slug={}, episode={}", movieSlug, episodeName);
                
                // Save to Database
                notificationService.createNotificationForEpisode(
                        movieSlug,
                        event.getMovieTitle(),
                        episodeName,
                        event.getThumbUrl()
                );

                // Send FCM
                fcmNotificationService.sendNewEpisodeNotification(
                        movieSlug,
                        event.getMovieTitle(),
                        episodeName,
                        event.getThumbUrl()
                );
            } else {
                log.info("Duplicate episode update detected by Redis. Skipping notification for slug={}, episode={}", movieSlug, episodeName);
            }
        } catch (Exception e) {
            log.error("Error checking notification deduplication in Redis: {}. Fallback to sending notification.", e.getMessage());
            // Fallback: if Redis is down, we still send the notification so users don't miss updates
            notificationService.createNotificationForEpisode(
                    movieSlug,
                    event.getMovieTitle(),
                    episodeName,
                    event.getThumbUrl()
            );
            fcmNotificationService.sendNewEpisodeNotification(
                    movieSlug,
                    event.getMovieTitle(),
                    episodeName,
                    event.getThumbUrl()
            );
        }
    }
}
