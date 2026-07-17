package com.tamdao.cinestream.core.notification

import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicSubscriptionManager @Inject constructor() {

    companion object {
        private const val TAG = "TopicSubManager"
        private const val TOPIC_PREFIX = "movie_"
    }

    /**
     * Subscribe to a movie update topic.
     * Topic naming convention: movie_{slug}
     */
    fun subscribeToMovie(slug: String) {
        val topic = "$TOPIC_PREFIX$slug"
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully subscribed to topic: $topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic: $topic", task.exception)
                }
            }
    }

    /**
     * Unsubscribe from a movie update topic.
     */
    fun unsubscribeFromMovie(slug: String) {
        val topic = "$TOPIC_PREFIX$slug"
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Successfully unsubscribed from topic: $topic")
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: $topic", task.exception)
                }
            }
    }

    /**
     * Re-sync all favorite movie subscriptions.
     * Often used after clean installs or user re-login.
     */
    fun syncAllFavorites(favoriteSlugs: List<String>) {
        Log.d(TAG, "Syncing subscriptions for ${favoriteSlugs.size} favorite movies")
        favoriteSlugs.forEach { slug ->
            subscribeToMovie(slug)
        }
    }
}
