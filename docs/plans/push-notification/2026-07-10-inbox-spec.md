Started at: 2026/07/10 17:25:00
Finished at: 2026/07/10 17:26:00
Total time: 1 minute
---

# Design Spec: Notification Inbox (Lịch sử thông báo & Giao diện quả chuông)

## 1. Overview
Tính năng này mở rộng hệ thống Push Notification hiện tại bằng cách lưu trữ lịch sử thông báo ở Backend (dành riêng cho các bộ phim người dùng đã thêm vào Yêu thích) và cung cấp giao diện hiển thị danh sách thông báo trên ứng dụng Android.

## 2. Backend Design

### Database Schema
Chúng ta sẽ tạo thêm 2 bảng trong MySQL:

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL, -- e.g. "NEW_EPISODE"
    movie_slug VARCHAR(255) NOT NULL,
    thumb_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE CASCADE
);
```

### Event Handling
Trong `NotificationEventListener.java`, sau khi vượt qua bước khử trùng lặp (deduplication) sự kiện `EpisodeUpdateEvent`:
1. Truy vấn danh sách `User` thích bộ phim này bằng cách gọi `favoriteRepository.findByUserIdAndMovieSlug(...)` hoặc `UserFavoriteRepository.findByMovieSlug(...)`.
2. Tạo và lưu một thực thể `Notification` mới.
3. Tạo và lưu thực thể `UserNotification` cho mỗi User tương ứng với trạng thái `isRead = false`.
4. Gọi `FcmNotificationService.sendNewEpisodeNotification` để gửi push notification như trước.

### API Endpoints (`NotificationController.java`)
Tất cả các API này yêu cầu xác thực người dùng (JWT token):
- `GET /api/v1/notifications`
  - Mô tả: Lấy danh sách thông báo của người dùng hiện tại (phân trang, sắp xếp theo `created_at` giảm dần).
  - Trả về: `List<NotificationResponse>` (bao gồm: `id` của user_notifications, `title`, `content`, `type`, `movieSlug`, `thumbUrl`, `isRead`, `createdAt`).
- `PATCH /api/v1/notifications/{id}/read`
  - Mô tả: Đánh dấu một thông báo cụ thể là đã đọc.
  - Phản hồi: `200 OK` hoặc `204 No Content`.
- `POST /api/v1/notifications/read-all`
  - Mô tả: Đánh dấu tất cả thông báo của người dùng hiện tại là đã đọc.
  - Phản hồi: `200 OK` hoặc `204 No Content`.
- `GET /api/v1/notifications/unread-count`
  - Mô tả: Lấy số lượng thông báo chưa đọc.
  - Trả về: `{ "unreadCount": Integer }`.

## 3. Android Design

### UI/UX
- **HomeScreen & Top Bar**:
  - Thêm biểu tượng quả chuông (`IconButton` chứa `Icons.Default.Notifications` hoặc tương đương) bên cạnh nút Tìm kiếm.
  - Đè một badge màu đỏ (chấm nhỏ) nếu `unreadCount > 0`.
  - Nhấp vào quả chuông sẽ kích hoạt điều hướng đến màn hình `"notifications"`.
- **NotificationScreen**:
  - Tiêu đề màn hình: "Thông báo".
  - Có nút quay lại (Back) và nút "Đánh dấu đã đọc tất cả" ở góc phải.
  - Hiển thị danh sách thông báo dạng danh sách dọc (`LazyColumn`).
  - Hỗ trợ Pull-to-refresh.
  - Mỗi mục thông báo gồm:
    - Ảnh Thumbnail của phim (Coil image, bo góc).
    - Tên phim (Title) và nội dung cập nhật (ví dụ: "Đã cập nhật tập mới").
    - Thời gian dạng tương đối (ví dụ: "5 phút trước").
    - Đánh dấu chưa đọc bằng chấm tròn màu Cyan hoặc nền tối nhẹ.
  - Khi click vào một mục:
    - Kích hoạt API đọc thông báo.
    - Điều hướng sang màn hình xem phim bằng `movieSlug`.

### Architecture
- **Data DTO**: `NotificationResponseDto` khớp với API response từ backend.
- **API Client**: `CineStreamApiService` định nghĩa các phương thức API.
- **Repository**: `NotificationRepository` thực hiện gọi API và xử lý dữ liệu.
- **ViewModel**: `NotificationViewModel` quản lý UI State (Loading, Success, Error), danh sách thông báo, và đếm số lượng chưa đọc.
- **Navigation**: Thêm màn hình `"notifications"` vào navigation graph trong `MainActivity` hoặc `NavGraph`.

## 4. Verification Plan
- **Backend Unit / Integration Test**: Xây dựng test case kiểm tra việc tạo thông báo khi `EpisodeUpdateEvent` được kích hoạt và kiểm tra các API endpoint.
- **Manual Test**:
  - Thêm một bộ phim vào danh sách Yêu thích trên Android.
  - Trigger sự kiện cập nhật tập phim mới ở Backend.
  - Xác nhận Android nhận được thông báo, hiển thị chấm đỏ trên Top Bar.
  - Click vào biểu tượng quả chuông để vào màn hình danh sách, kiểm tra hiển thị thông tin và thời gian.
  - Click vào thông báo để chuyển hướng đến trang xem phim và xác nhận trạng thái chuyển thành "Đã đọc".
