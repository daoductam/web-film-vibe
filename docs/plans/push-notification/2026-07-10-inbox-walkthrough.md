# Walkthrough: Notification Inbox (Lịch sử thông báo & Giao diện quả chuông)

## Execution Summary

- **Spec:** [2026-07-10-inbox-spec.md](file:///d:/Java%20Study/Projects/web-film/docs/plans/push-notification/2026-07-10-inbox-spec.md)
- **Plan:** [2026-07-10-inbox-plan.md](file:///d:/Java%20Study/Projects/web-film/docs/plans/push-notification/2026-07-10-inbox-plan.md)
- **Approach:** TDD (Test-Driven Development)
- **Tasks completed:** 13/13 tasks across Groups A, B, C, D.

---

## Technical Changes

### 1. Backend (`web-film-backend`)
* **Entities**:
  * [Notification.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/entity/Notification.java) — Lưu trữ nội dung và siêu dữ liệu thông báo chung.
  * [UserNotification.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/entity/UserNotification.java) — Liên kết thông báo đến từng User cùng trạng thái đã đọc (`isRead`).
* **Repositories**:
  * [NotificationRepository.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/NotificationRepository.java)
  * [UserNotificationRepository.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/UserNotificationRepository.java) — Hỗ trợ đánh dấu toàn bộ đã đọc và đếm chưa đọc.
  * [UserFavoriteRepository.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/repository/UserFavoriteRepository.java) — Bổ sung `findByMovieSlug` kèm EntityGraph để lấy danh sách User tối ưu nhất.
* **Service**:
  * [NotificationService.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/service/NotificationService.java) — Xử lý business logic, ngăn chặn lỗ hổng bảo mật IDOR khi đọc thông báo.
* **Controller**:
  * [NotificationController.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/controller/NotificationController.java) — Cung cấp endpoints REST: lấy hòm thư (phân trang), đánh dấu đã đọc một/tất cả, đếm số chưa đọc.
* **EventListener**:
  * [NotificationEventListener.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/main/java/com/tamdao/web_film_backend/service/NotificationEventListener.java) — Khi có tập phim mới thành công, tạo bản ghi thông báo tương ứng cho những user yêu thích phim đó trước khi bắn FCM push.
* **Tests**:
  * [NotificationServiceTest.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/test/java/com/tamdao/web_film_backend/service/NotificationServiceTest.java) — Unit tests (TDD).
  * [NotificationControllerTest.java](file:///d:/Java%20Study/Projects/web-film/web-film-backend/src/test/java/com/tamdao/web_film_backend/controller/NotificationControllerTest.java) — WebMvc mock tests.

### 2. Android (`web-film-android`)
* **Data Layer**:
  * [NotificationDto.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/data/model/NotificationDto.kt) — Chứa data class mapping JSON.
  * [NotificationApiService.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/network/NotificationApiService.kt) — Retrofit calls.
  * [NetworkModule.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/network/NetworkModule.kt) — Khai báo DI trong Hilt.
  * [NotificationRepository.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/data/repository/NotificationRepository.kt) — Cầu nối gọi api lên viewmodel.
* **UI/UX & ViewModel**:
  * [NotificationViewModel.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationViewModel.kt) — Quản lý state của hòm thư, làm mới dữ liệu và xử lý các sự kiện click đọc thông báo.
  * [NotificationScreen.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/feature/notification/NotificationScreen.kt) — Thiết kế UI đẹp mắt, hiển thị chấm tròn màu Cyan cho thông báo chưa đọc, hỗ trợ Pull-to-refresh và Empty state.
  * [HomeScreen.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/feature/home/HomeScreen.kt) — Thêm nút quả chuông đè badge đỏ thông báo chưa đọc trên thanh tiêu đề đầu trang.
  * [MainActivity.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/MainActivity.kt) & [Screen.kt](file:///d:/Java%20Study/Projects/web-film/web-film-android/app/src/main/java/com/tamdao/cinestream/core/navigation/Screen.kt) — Cấu hình định tuyến (Route) điều hướng sang màn hình thông báo và ngược lại.

---

## Verification Results

* **Backend Tests**:
  * 9/9 tests passed successfully (xác thực lưu thông báo, ngăn chặn lỗ hổng IDOR, đếm chính xác trạng thái).
