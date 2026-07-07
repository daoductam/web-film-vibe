# Watch Party UX Overhaul — Spec

**Date**: 2026-07-07
**Status**: Draft
**Goal**: Cải thiện toàn diện UX của Watch Party trên Android — từ onboarding, tạo phòng, đến entry points — để người dùng hiểu và sử dụng được ngay lần đầu.

---

## Bối cảnh & Vấn đề

Hiện tại Watch Party lobby trên Android có nhiều vấn đề UX nghiêm trọng:

1. **Lỗi quyền truy cập**: Endpoint `/v1/watch-rooms/public` yêu cầu JWT authentication nhưng đây là dữ liệu công khai → nếu token hết hạn hoặc user chưa đăng nhập → hiện "Bạn không có quyền truy cập tính năng này"
2. **Không có onboarding**: Người dùng mở Watch Party không biết nó dùng để làm gì
3. **Tạo phòng yêu cầu nhập Movie ID thủ công**: Người dùng phải tự biết ID phim → trải nghiệm cực kỳ tệ
4. **Empty state trống rỗng**: Khi chưa có phòng nào, chỉ hiện text nhạt
5. **Không có entry point tự nhiên**: Phải vào tab Watch Party mới dùng được, không thể tạo phòng từ trang chi tiết phim

---

## Thay đổi 1: Fix lỗi quyền truy cập (Backend)

### Root Cause
Trong `SecurityConfig.java`, danh sách `PUBLIC_ENDPOINTS` không bao gồm `/v1/watch-rooms/**`. Theo rule `.anyRequest().authenticated()`, mọi request đến Watch Room endpoints đều cần JWT token.

Endpoint `GET /v1/watch-rooms/public` lấy danh sách phòng công khai — đây nên là public endpoint, không cần authentication.

### Giải pháp
Thêm rule permit cho `GET /v1/watch-rooms/public` trong `SecurityConfig`:

```java
.requestMatchers(HttpMethod.GET, "/v1/watch-rooms/public").permitAll()
```

> **Lưu ý**: CHỈ permit GET method cho `/public`. Các endpoint khác (create, join, leave, end) vẫn yêu cầu authentication.

### Files thay đổi
- `web-film-backend/src/main/java/com/tamdao/web_film_backend/config/SecurityConfig.java`

---

## Thay đổi 2: Redesign Lobby Screen (Android)

### Hiện tại
- Chỉ có: Card "Tham gia bằng mã phòng" + LazyColumn rooms + FAB tạo phòng
- Không có giới thiệu, hướng dẫn, hay visual thu hút

### Thiết kế mới

#### 2a. Hero Section (phía trên cùng, trước card nhập mã)
- **Icon**: 🎬 hoặc custom icon xem chung
- **Title**: "Xem phim cùng bạn bè"
- **Subtitle**: "Tạo phòng, mời bạn bè và xem phim đồng bộ theo thời gian thực"
- **Visual**: Gradient background NeonCyan -> Obsidian, bo góc, có animation nhẹ

#### 2b. Quick Start Steps (sau Hero, 3 bước ngang)
Row hiển thị 3 bước minh hoạ:
1. 🎬 "Chọn phim" — Chọn phim yêu thích
2. 📨 "Mời bạn bè" — Gửi mã phòng
3. 🍿 "Xem cùng nhau" — Xem đồng bộ real-time

Mỗi bước: icon + text ngắn, style nhẹ nhàng (card translucent)

#### 2c. Phần "Tham gia bằng mã phòng" (giữ nguyên logic, cải thiện visual)
- Thêm icon gợi ý trước input
- Border & placeholder rõ ràng hơn

#### 2d. Phần "Các phòng công khai"
- Giữ nguyên `PublicRoomCard` design hiện tại (đã đẹp)
- **Empty State mới**: Thay text nhạt bằng Column có icon + text + CTA button "Tạo phòng đầu tiên"

### Files thay đổi
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt`

---

## Thay đổi 3: Movie Picker (Full-screen Bottom Sheet)

### Hiện tại
`CreateRoomDialog` yêu cầu nhập `Mã phim (ID)` bằng tay — UX rất tệ.

### Thiết kế mới — Flow 2 bước

#### Bước 1: Movie Picker BottomSheet
Khi user nhấn FAB "+" hoặc CTA "Tạo phòng":
- **ModalBottomSheet** full-screen (sử dụng `ModalBottomSheet` Material3)
- **Search bar** phía trên — gõ tên phim để tìm
- **Default content**: Hiển thị phim phổ biến (từ `getPopularMovies()` API đã có)
- **Search results**: Grid 3 cột, mỗi item = poster + tên phim
- **Chọn phim** → chuyển sang bước 2

#### Bước 2: Create Room Dialog (simplified)
Sau khi chọn phim:
- **AlertDialog** giống hiện tại NHƯNG:
  - Bỏ field "Mã phim (ID)"
  - Thêm **Preview phim đã chọn** (poster nhỏ + tên phim) ở đầu dialog
  - Giữ nguyên: Tên phòng, Loại phòng, Slider số người
  - Movie ID tự động truyền từ phim đã chọn

### Tận dụng logic có sẵn
- `MovieApiService.searchMovies(query)` — đã có API search
- `MovieApiService.getPopularMovies()` — đã có API phổ biến
- `MovieRepository.getMoviesByFilter()` — đã có hàm gọi search

### Data flow
1. User nhấn tạo phòng
2. BottomSheet hiện lên → load `getPopularMovies()` 
3. User gõ tìm kiếm → gọi `searchMovies(query)` (debounce 300ms)
4. User tap vào poster → lưu `MovieDto` selected
5. BottomSheet đóng → `CreateRoomDialog` hiện với phim đã chọn
6. User nhập tên phòng + settings → gọi `createRoom(request)` với `movieId = selectedMovie.id`

### Files thay đổi
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt` — thêm MoviePickerBottomSheet, sửa CreateRoomDialog
- `web-film-android/.../feature/watchparty/WatchPartyViewModel.kt` — thêm hàm searchMovies, loadPopularMovies

### Dependencies
- `MovieApiService` (đã có, inject qua `MovieRepository`)
- `WatchPartyViewModel` cần inject thêm `MovieRepository` hoặc `MovieApiService`

---

## Thay đổi 4: Entry Point từ MovieDetailScreen

### Thiết kế
Thêm nút **"Xem cùng bạn bè"** vào trang chi tiết phim:
- **Vị trí**: Trong action bar (cạnh nút Favorite, Download) hoặc dưới thông tin phim
- **Icon**: `Icons.Default.Group` hoặc `Icons.Default.LiveTv`
- **Hành vi**: Navigate đến `WatchPartyLobby` và tự động mở `CreateRoomDialog` với phim đã chọn (bỏ qua Movie Picker)

### Navigation flow
1. User ở `MovieDetailScreen` → nhấn "Xem cùng bạn bè"
2. Navigate đến `WatchPartyLobby` với param `?movieId={id}&movieTitle={title}`
3. Lobby tự động mở `CreateRoomDialog` với phim pre-filled

### Files thay đổi
- `web-film-android/.../feature/detail/MovieDetailScreen.kt` — thêm button
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt` — handle param movie pre-fill
- `web-film-android/.../core/navigation/Screen.kt` — cập nhật route WatchPartyLobby thêm optional params
- `web-film-android/.../MainActivity.kt` — cập nhật NavGraph

---

## Thay đổi 5: Empty State Illustration

### Hiện tại
```
"Chưa có phòng công khai nào. Hãy tạo phòng mới!"
```
Text nhạt, không thu hút.

### Thiết kế mới
- **Icon lớn**: `LiveTv` hoặc `Groups` icon, size 80dp, tint NeonCyan
- **Text chính**: "Chưa có phòng nào đang hoạt động"
- **Text phụ**: "Hãy tạo phòng mới để xem phim cùng bạn bè!"
- **CTA Button**: "✨ Tạo phòng ngay" → mở Movie Picker

### Files thay đổi
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt`

---

## Tóm tắt files cần thay đổi

| File | Thay đổi |
|------|----------|
| `SecurityConfig.java` | Permit GET `/v1/watch-rooms/public` |
| `WatchPartyLobbyScreen.kt` | Redesign lobby + Movie Picker + Empty state |
| `WatchPartyViewModel.kt` | Thêm searchMovies, loadPopularMovies |
| `MovieDetailScreen.kt` | Thêm nút "Xem cùng bạn bè" |
| `Screen.kt` | Cập nhật route WatchPartyLobby |
| `MainActivity.kt` | Cập nhật NavGraph params |

---

## Ngoài scope (không làm đợt này)

- Tìm kiếm phòng theo tên
- Invite bạn bè qua deep link
- Push notification khi bạn bè tạo phòng
- Custom avatar cho phòng
