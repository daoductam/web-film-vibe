# Implementation Walkthrough: Push Notification for Android (New Episode Updates)

## Execution Summary

- **Spec:** [2026-07-10-spec.md](file:///d:/Java%20Study/Projects/web-film/docs/plans/push-notification/2026-07-10-spec.md)
- **Plan:** [2026-07-10-plan.md](file:///d:/Java%20Study/Projects/web-film/docs/plans/push-notification/2026-07-10-plan.md)
- **Implementation approach:** Standard
- **Tasks completed:** 7/7

---

## Task Execution Log

### TASK-001: Backend Firebase SDK Configuration
- **Status:** Completed
- **Files modified:**
  - [pom.xml](file:///d:/Java%20Study/Projects/web-film/web-film-backend/pom.xml) — Added `firebase-admin` dependency.
  - [application.yml](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/resources/application.yml) — Declared Firebase service account credentials path property.
- **Files created:**
  - [FirebaseConfig.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/config/FirebaseConfig.java) — Safely and dynamically initializes the default `FirebaseApp` using Spring `@PostConstruct`.

### TASK-002: Backend Spring Event Publisher in DataMerger
- **Status:** Completed
- **Files modified:**
  - [DataMergerService.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/crawler/service/DataMergerService.java) — Added Spring `ApplicationEventPublisher` invocation in the transactional episode merge sequence.
- **Files created:**
  - [EpisodeUpdateEvent.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/dto/event/EpisodeUpdateEvent.java) — Standard Spring ApplicationEvent encapsulating new episode details.

### TASK-003: Backend FCM Push Notification Service & Listener (with Redis dedup)
- **Status:** Completed
- **Files modified:**
  - [WebFilmBackendApplication.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/WebFilmBackendApplication.java) — Enabled `@EnableAsync` globally.
- **Files created:**
  - [FcmNotificationService.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/service/FcmNotificationService.java) — Handles the layout of custom FCM payloads and sends to Google servers asynchronously.
  - [NotificationEventListener.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/service/NotificationEventListener.java) — Listens post-commit (`AFTER_COMMIT`), applies Redis key-based (`notif:movie:{slug}:ep:{name}`) dedup logic for 24h.

### TASK-004: Android Firebase Dependency Integration
- **Status:** Completed
- **Files modified:**
  - [libs.versions.toml](file:///d:/Java%20Study/Projects/web-film/web-film-android/gradle/libs.versions.toml) — Declared Firebase BOM & Messaging artifacts + Google Services plugin.
  - [build.gradle.kts (Project level)](file:///d:/Java%20Study/Projects/web-film/web-film-android/build.gradle.kts) — Registered Google services plugin.
  - [build.gradle.kts (App level)](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/build.gradle.kts) — Applied google-services plugin and imported FCM client library.

### TASK-005: Android Topic Subscription Logic
- **Status:** Completed
- **Files created:**
  - [TopicSubscriptionManager.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/notification/TopicSubscriptionManager.kt) — Dedicated helper manager for managing subscriptions asynchronously using FCM SDK.

### TASK-006: Android FCM Service & Rich Notification Renderer
- **Status:** Completed
- **Files modified:**
  - [AndroidManifest.xml](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/AndroidManifest.xml) — Registered custom notification service and configured deep-link filters.
- **Files created:**
  - [NotificationHelper.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/notification/NotificationHelper.kt) — Configures priority-high channel, processes async Coil Image bitmap downloads, and builds interactive `BigPictureStyle` layouts.
  - [CineFirebaseMessagingService.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/notification/CineFirebaseMessagingService.kt) — Entry point service extending SDK messaging callbacks.

### TASK-007: Android UI/VM Wiring & Re-sync on Login
- **Status:** Completed
- **Files modified:**
  - [MovieRepository.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/data/repository/MovieRepository.kt) — Integrated automatic topic subscription triggers on `toggleFavorite()` and `refreshFavorites()`.
  - [MainActivity.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/MainActivity.kt) — Mapped `cinestream://movie/{slug}` deep-link direct navigation through standard Compose Navigation graph.

---

## Deviations from Plan
* **MovieRepository Integration**: Direct integration in the repository layers instead of ViewModels. This keeps logic centralized, prevents UI code pollution, and guarantees automatic FCM re-subscription synchronization whenever remote servers push bulk changes down to local Room database.
