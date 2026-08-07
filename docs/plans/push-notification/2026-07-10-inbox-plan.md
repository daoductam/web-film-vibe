# Implementation Plan: Notification Inbox (Lịch sử thông báo & Giao diện quả chuông)

Spec Source: `file:///d:/Java%20Study/Projects/web-film/docs/plans/push-notification/2026-07-10-inbox-spec.md`
Owner: Antigravity AI
Last Updated: 2026-07-10
Status: Draft

---

# 1. Context

**Problem:**
Người dùng hiện tại chỉ nhận được thông báo đẩy (push notification) qua FCM khi có tập phim mới. Nếu họ lỡ bỏ qua thông báo đẩy này, họ không có cách nào xem lại danh sách các thông báo trước đó trực tiếp trên giao diện ứng dụng.

**Affected Modules:**
- `web-film-backend` — Thêm thực thể Notification, lưu lịch sử, cập nhật Listener và cung cấp REST API cho Client.
- `web-film-android` — Thêm biểu tượng quả chuông có badge chưa đọc, thêm màn hình danh sách thông báo và tích hợp API.

**Non-Goals:**
- Tạo giao diện gửi thông báo từ Admin (Backend chỉ gửi tự động qua Event Listener).
- Lưu lịch sử thiết bị offline (Dữ liệu sẽ được đồng bộ và lưu trữ hoàn toàn trên server backend).

---

# 2. Constraints

**Language:** Java 21 (Backend), Kotlin (Android)
**Architecture:** RESTful API (Backend), MVVM (Android Jetpack Compose)

**Rules:**
- Chỉ thay đổi các module/package liên quan đến notification và home header.
- Không thêm thư viện phụ thuộc (dependency) mới.
- Tương thích ngược với hệ thống JWT token và FCM Service hiện tại.

---

# 3. Conventions

**Naming:**
- Spring Boot: Entity (`PascalCase`), Controller (`PascalCase`), Service (`PascalCase`), Repository (`PascalCase`), DTO (`PascalCase`).
- Android Compose: Screen (`PascalCase` + `Screen`), ViewModel (`PascalCase` + `ViewModel`), DTO (`PascalCase` + `Dto`).

**Error Handling:**
- Backend: Trả về HttpStatus thích hợp (e.g. 404 Not Found khi không thấy thông báo, 400 Bad Request cho dữ liệu sai).
- Android: Hiển thị giao diện Error State khi không thể tải hoặc gọi API lỗi.

**Logging:**
- Backend: Sử dụng `slf4j` với định dạng log rõ ràng.
- Android: Sử dụng `Log` của Android SDK.

**Testing:**
- JUnit 5 cho Backend.
- MockK + JUnit cho Android ViewModel.

---

# 4. Contracts

## Data Structure: `Notification`
```java
public class Notification {
  private Long id;
  private String title;
  private String content;
  private String type;
  private String movieSlug;
  private String thumbUrl;
  private LocalDateTime createdAt;
}
```

## Data Structure: `UserNotification`
```java
public class UserNotification {
  private Long id;
  private User user;
  private Notification notification;
  private boolean isRead;
  private LocalDateTime readAt;
}
```

## Shared DTOs:
```java
public class NotificationResponse {
  private Long id; // UserNotification ID
  private String title;
  private String content;
  private String type;
  private String movieSlug;
  private String thumbUrl;
  private boolean isRead;
  private LocalDateTime createdAt;
}

public class UnreadCountResponse {
  private long unreadCount;
}
```

---

# 5. Target Architecture

**Components:**
- Backend:
  - `Notification` & `UserNotification` (JPA Entity)
  - `NotificationRepository` & `UserNotificationRepository`
  - `NotificationService` (Lưu thông báo, đánh dấu đã đọc, đếm số lượng chưa đọc)
  - `NotificationController` (REST Endpoints)
  - `NotificationEventListener` (Xử lý sự kiện lưu vào DB)
- Android:
  - `NotificationDto` (Data model)
  - `NotificationRepository` (Gọi API qua Retrofit Service)
  - `NotificationViewModel` (Quản lý trạng thái UI)
  - `NotificationScreen` (Hiển thị UI danh sách và cập nhật trạng thái đã đọc)

**Interaction Flow:**
```
EpisodeUpdateEvent -> NotificationEventListener 
                         -> NotificationService (Save to Notification & UserNotification)
                         -> FcmNotificationService (Trigger FCM)

Android Client -> GET /api/v1/notifications -> UI displays Notification List
Android Client -> PATCH /api/v1/notifications/{id}/read -> Mark as read on Server -> Refresh List
Android Client -> GET /api/v1/notifications/unread-count -> Update Red Dot Badge on Home Header
```

---

# 6. Artifact Registry

| Artifact | Type | Owner Task | Implements |
|----------|------|------------|------------|
| `src/main/java/com/tamdao/web_film_backend/entity/Notification.java` | class | TASK-001 | Database Entity |
| `src/main/java/com/tamdao/web_film_backend/entity/UserNotification.java` | class | TASK-001 | Database Entity |
| `src/main/java/com/tamdao/web_film_backend/repository/NotificationRepository.java` | interface | TASK-002 | JPA Repository |
| `src/main/java/com/tamdao/web_film_backend/repository/UserNotificationRepository.java` | interface | TASK-002 | JPA Repository |
| `src/main/java/com/tamdao/web_film_backend/dto/response/NotificationResponse.java` | class | TASK-003 | Response DTO |
| `src/main/java/com/tamdao/web_film_backend/dto/response/UnreadCountResponse.java` | class | TASK-003 | Response DTO |
| `src/main/java/com/tamdao/web_film_backend/service/NotificationService.java` | class | TASK-004 | Service Layer |
| `src/main/java/com/tamdao/web_film_backend/controller/NotificationController.java` | class | TASK-005 | REST Controller |
| `src/main/java/com/tamdao/web_film_backend/service/NotificationEventListener.java` | class | TASK-006 | Event Handler (Modify) |
| `app/src/main/java/com/tamdao/cinestream/data/model/NotificationDto.kt` | class | TASK-007 | Android DTO |
| `app/src/main/java/com/tamdao/cinestream/data/api/CineStreamApiService.kt` | interface | TASK-008 | API Service (Modify) |
| `app/src/main/java/com/tamdao/cinestream/data/repository/NotificationRepository.kt` | class | TASK-009 | Repository Layer |
| `app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationViewModel.kt` | class | TASK-010 | ViewModel |
| `app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationScreen.kt` | class | TASK-011 | Composable UI |
| `app/src/main/java/com/tamdao/cinestream/feature/home/HomeScreen.kt` | class | TASK-012 | Composable UI (Modify) |
| `app/src/main/java/com/tamdao/cinestream/MainActivity.kt` | class | TASK-013 | Main Activity / Navigation (Modify) |

---

# 7. Task Graph

| ID | Name | Depends On | Effort | parallel_group | sub_agent_scope | domain_skills |
|----|------|------------|--------|----------------|-----------------|---------------|
| TASK-001 | Backend Entities Setup | — | S | Group A | web-film-backend/src/main/java/com/tamdao/web_film_backend/entity/ | database-design |
| TASK-002 | Backend Repositories Setup | TASK-001 | S | Group A | web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/ | database-design |
| TASK-003 | Backend Response DTOs | — | S | Group A | web-film-backend/src/main/java/com/tamdao/web_film_backend/dto/ | api-design |
| TASK-004 | Backend Service Layer | TASK-002 | M | Group B | web-film-backend/src/main/java/com/tamdao/web_film_backend/service/ | log-processing |
| TASK-005 | Backend REST Controller | TASK-003, TASK-004 | M | Group B | web-film-backend/src/main/java/com/tamdao/web_film_backend/controller/ | api-design |
| TASK-006 | Backend Event Listener Modification | TASK-004 | S | Group B | web-film-backend/src/main/java/com/tamdao/web_film_backend/service/ | log-processing |
| TASK-007 | Android Notification DTO | — | S | Group C | web-film-android/app/src/main/java/com/tamdao/cinestream/data/model/ | — |
| TASK-008 | Android API Service Integration | TASK-007 | S | Group C | web-film-android/app/src/main/java/com/tamdao/cinestream/data/api/ | — |
| TASK-009 | Android Repository Integration | TASK-008 | S | Group C | web-film-android/app/src/main/java/com/tamdao/cinestream/data/repository/ | — |
| TASK-010 | Android Notification ViewModel | TASK-009 | M | Group D | web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/ | test-strategy |
| TASK-011 | Android Notification Screen | TASK-010 | M | Group D | web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/ | — |
| TASK-012 | Android Home Header Modification | TASK-009 | S | Group D | web-film-android/app/src/main/java/com/tamdao/cinestream/feature/home/ | — |
| TASK-013 | Android Navigation Setup | TASK-011 | S | Group D | web-film-android/app/src/main/java/com/tamdao/cinestream/ | — |

**Dependency Graph:**
```
[Backend Task Flow]
TASK-001 (Entities) ──> TASK-002 (Repos) ──> TASK-004 (Service) ──> TASK-005 (Controller)
                                            └──> TASK-006 (EventListener)
TASK-003 (DTOs) ────────────────────────────────┘

[Android Task Flow]
TASK-007 (DTO) ──> TASK-008 (API Service) ──> TASK-009 (Repo) ──> TASK-010 (VM) ──> TASK-011 (Screen) ──> TASK-013 (Nav)
                                               └──> TASK-012 (HomeHeader) ────────┘
```

---

# 8. Task Specifications

## TASK-001: Backend Entities Setup
- **Description:** Tạo class Entity `Notification` và `UserNotification` để cấu hình bảng CSDL tương ứng.
- **Input:** None
- **Output:** JPA entities.
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/entity/Notification.java` — **create**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/entity/UserNotification.java` — **create**
- **Acceptance Criteria:**
  - Biên dịch thành công, JPA cấu hình đúng quan hệ `ManyToOne` từ `UserNotification` tới `User` và `Notification`.

## TASK-002: Backend Repositories Setup
- **Description:** Định nghĩa các Repository JPA cho `Notification` và `UserNotification`.
- **Input:** TASK-001
- **Output:** Spring Data JPA Repositories.
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/NotificationRepository.java` — **create**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/UserNotificationRepository.java` — **create**
- **Acceptance Criteria:**
  - Cung cấp method: `findByUserIdOrderByNotificationCreatedAtDesc(Long userId, Pageable pageable)` trong `UserNotificationRepository`.
  - Cung cấp method: `countByUserIdAndIsReadFalse(Long userId)`.

## TASK-003: Backend Response DTOs
- **Description:** Định nghĩa `NotificationResponse` và `UnreadCountResponse` để chuẩn hóa API payload.
- **Input:** None
- **Output:** DTO classes.
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/dto/response/NotificationResponse.java` — **create**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/dto/response/UnreadCountResponse.java` — **create**

## TASK-004: Backend Service Layer
- **Description:** Hiện thực business logic lấy danh sách, đếm chưa đọc, đánh dấu đã đọc thông báo.
- **Input:** TASK-002
- **Output:** `NotificationService`
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/service/NotificationService.java` — **create**
- **Acceptance Criteria:**
  - Đánh dấu đã đọc đúng thông báo theo ID thuộc về user đang yêu cầu (chống IDOR).
  - Đếm đúng số lượng thông báo `isRead = false`.

## TASK-005: Backend REST Controller
- **Description:** Cung cấp REST endpoints cho Client.
- **Input:** TASK-003, TASK-004
- **Output:** `NotificationController`
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/controller/NotificationController.java` — **create**
- **Acceptance Criteria:**
  - Endpoint `GET /api/v1/notifications` trả về list phân trang.
  - Endpoint `PATCH /api/v1/notifications/{id}/read` trả về 200 OK sau khi chuyển đổi trạng thái đọc.

## TASK-006: Backend Event Listener Modification
- **Description:** Sửa đổi listener để khi nhận `EpisodeUpdateEvent` sẽ lưu thông báo cho tất cả user yêu thích bộ phim đó.
- **Input:** TASK-004
- **Output:** Listener cập nhật logic DB.
- **Files:**
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/service/NotificationEventListener.java` — **modify**
- **Acceptance Criteria:**
  - Khi luồng craw cập nhật phim có tập mới, tìm thấy user đã yêu thích phim, tạo bản ghi Notification trong CSDL.

## TASK-007: Android Notification DTO
- **Description:** Tạo data class `NotificationDto` và `UnreadCountDto` tương ứng để khớp với dữ liệu JSON của API backend.
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/data/model/NotificationDto.kt` — **create**

## TASK-008: Android API Service Integration
- **Description:** Khai báo các API call trong `CineStreamApiService`.
- **Input:** TASK-007
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/data/api/CineStreamApiService.kt` — **modify**

## TASK-009: Android Repository Integration
- **Description:** Viết code Repository gọi API và đưa dữ liệu sang ViewModel.
- **Input:** TASK-008
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/data/repository/NotificationRepository.kt` — **create**

## TASK-010: Android Notification ViewModel
- **Description:** Quản lý UI state, danh sách thông báo và các lệnh click xử lý sự kiện.
- **Input:** TASK-009
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationViewModel.kt` — **create**

## TASK-011: Android Notification Screen
- **Description:** Thiết kế giao diện Composable hiển thị danh sách thông báo, xử lý sự kiện Pull-to-refresh và click chuyển hướng.
- **Input:** TASK-010
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationScreen.kt` — **create**

## TASK-012: Android Home Header Modification
- **Description:** Thêm nút Quả chuông vào header và cập nhật badge đỏ dựa trên Flow `unreadCount` từ repository/viewmodel.
- **Input:** TASK-009
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/feature/home/HomeScreen.kt` — **modify**

## TASK-013: Android Navigation Setup
- **Description:** Đăng ký màn hình `"notifications"` vào navigation graph.
- **Input:** TASK-011
- **Files:**
  - `web-film-android/app/src/main/java/com/tamdao/cinestream/MainActivity.kt` — **modify**

---

# 9. Edge Cases

| # | Scenario | Expected Behavior | Handled In |
|---|----------|-------------------|------------|
| 1 | Không có người dùng nào thích phim khi có tập mới | Chỉ tạo bản ghi Notification chung, không tạo UserNotification nào. Không báo lỗi. | TASK-006 |
| 2 | Đánh dấu đã đọc một thông báo không thuộc về mình | Trả về lỗi 403 Forbidden hoặc 404 Not Found để tránh IDOR. | TASK-004 |
| 3 | Mạng yếu hoặc mất kết nối khi tải trang thông báo trên Android | Hiển thị thông báo lỗi kết nối và nút "Thử lại" (Retry). | TASK-011 |
| 4 | Notification chứa slug phim không tồn tại trên hệ thống | Khi click vào, điều hướng an toàn và báo lỗi trên màn hình phim/tập phim ("Không tìm thấy phim"). | TASK-011 |

---

# 10. Risks

| # | Risk | Impact | Likelihood | Mitigation |
|---|------|--------|------------|------------|
| 1 | Số lượng User yêu thích 1 bộ phim quá lớn (e.g. 10.000+) dẫn đến ghi DB chậm khi crawl | High | Low | Sử dụng Bulk Insert khi lưu `UserNotification` hoặc xử lý bất đồng bộ (Async) để tránh nghẽn thread của Event Listener. |

---

# 11. Verification Plan

**Unit Tests (Backend):**
- `NotificationServiceTest` — Kiểm tra tính chính xác của đếm số chưa đọc, đánh dấu đã đọc, phân quyền IDOR.
- `NotificationEventListenerTest` — Xác thực lưu đúng số bản ghi khi sự kiện xảy ra.

**Manual / Smoke Tests:**
1. Đăng nhập tài liệu User A trên Android, Yêu thích phim "Đảo Hải Tặc".
2. Backend gửi mock event tập mới cho phim "Đảo Hải Tặc".
3. Kiểm tra trên Android: Quả chuông xuất hiện chấm đỏ chưa đọc.
4. Mở màn hình thông báo, xem tin nhắn. Nhấp vào tin nhắn -> Chuyển hướng sang xem phim, mất chấm đỏ.
