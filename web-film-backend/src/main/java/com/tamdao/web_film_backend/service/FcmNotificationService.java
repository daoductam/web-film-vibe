package com.tamdao.web_film_backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmNotificationService {

    /**
     * Send new episode notification to the movie's topic.
     * Topic format: movie_{slug}
     */
    public void sendNewEpisodeNotification(String movieSlug, String movieTitle, String episodeName, String thumbUrl) {
        String topic = "movie_" + movieSlug;
        log.info("Sending FCM push notification to topic={}: movieSlug={}, episode={}", topic, movieSlug, episodeName);

        try {
            // Build simple FCM notification payload (visible on notification drawer)
            Notification notification = Notification.builder()
                    .setTitle(movieTitle)
                    .setBody("Đã cập nhật " + episodeName)
                    .setImage(thumbUrl)
                    .build();

            // Build payload with custom data for deep linking in Android
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(notification)
                    .putData("type", "NEW_EPISODE")
                    .putData("movieSlug", movieSlug)
                    .putData("movieTitle", movieTitle)
                    .putData("episodeName", episodeName)
                    .putData("thumbUrl", thumbUrl != null ? thumbUrl : "")
                    .build();

            // Send message asynchronously to avoid blocking the caller thread
            FirebaseMessaging.getInstance().sendAsync(message);
            log.debug("Successfully queued FCM message for topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send FCM push notification for topic {}: {}", topic, e.getMessage(), e);
        }
    }
}
