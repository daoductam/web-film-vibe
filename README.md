# 🎬 CineStream - Hệ Sinh Thái Xem Phim Hiện Đại

CineStream là một giải pháp xem phim toàn diện (full-stack) được thiết kế để mang lại trải nghiệm xem phim cao cấp trên cả nền tảng Web và Android. Hệ sinh thái bao gồm backend Spring Boot mạnh mẽ, giao diện web React hiệu năng cao và ứng dụng Android gốc (native) được xây dựng bằng Jetpack Compose.

---

## 🏗️ Kiến Trúc Kỹ Thuật

CineStream được xây dựng trên kiến trúc mô-đun, phân chia rõ rệt trách nhiệm giữa ba thành phần chính:

### 1. [Backend API](file:///d:/Java%20Study/Projects/web-film/web-film-backend) (Spring Boot)
Động cơ cốt lõi của CineStream, quản lý lưu trữ dữ liệu, bảo mật và tự động khám phá nội dung.
- **Thiết kế Microservice**: Kiến trúc RESTful API với Spring Boot 3.4+.
- **Bảo mật**: Xác thực không trạng thái (stateless) sử dụng **Spring Security** và **JWT**.
- **Hiệu năng**: Tích hợp bộ nhớ đệm **Redis** để truy xuất siêu dữ liệu phim với tốc độ cao.
- **Tự động hóa**: Hệ thống **Crawler** tự động thu thập và hợp nhất dữ liệu phim từ các nguồn bên ngoài.

### 2. [Web Frontend](file:///d:/Java%20Study/Projects/web-film/web-film-frontend) (React)
Ứng dụng web mượt mà, phản hồi nhanh, tập trung vào tốc độ và thẩm mỹ.
- **Framework**: React 19 với **Vite** để tối ưu hóa quá trình phát triển và build.
- **Styling**: Sử dụng **Tailwind CSS 4.0** cho thiết kế giao diện hiện đại theo hướng utility-first.
- **Quản lý trạng thái**: **Zustand** cho trạng thái toàn cục nhẹ nhàng và **TanStack Query** để xử lý trạng thái server hiệu quả.
- **Trình phát video**: Trình phát **Vidstack** nâng cao hỗ trợ phát trực tuyến HLS.

### 3. [Ứng dụng Mobile](file:///d:/Java%20Study/Projects/web-film/web-film-android) (Android Native)
Trải nghiệm di động cao cấp dành cho người dùng.
- **UI Framework**: 100% **Jetpack Compose** với hệ thống thiết kế Material 3.
- **DI & Persistence**: **Hilt** để tiêm phụ thuộc (dependency injection) và **Room** để lưu trữ dữ liệu cục bộ (phim yêu thích/lịch sử ngoại tuyến).
- **Video Engine**: **Media3 ExoPlayer** được tối ưu hóa cho phát trực tuyến HLS và kiểm soát phát lại mượt mà.

---

## 🚀 Tính Năng Cốt Lõi

### 🔍 Khám Phá Nội Dung
- **Tìm Kiếm Toàn Cầu Nâng Cao**: Tìm kiếm và lọc phim trong thời gian thực.
- **Lọc Đa Danh Mục**: Lọc động theo thể loại, năm và quốc gia (Hỗ trợ chọn nhiều danh mục cùng lúc).
- **Crawler Tự Động**: Dịch vụ hợp nhất dữ liệu thông minh giúp thư viện phim luôn cập nhật với thông tin chất lượng cao.

### 👤 Trải Nghiệm Người Dùng
- **Quản Lý Tài Khoản**: Đăng nhập/Đăng ký bảo mật với phiên làm việc JWT.
- **Cá Nhân Hóa**: Hồ sơ người dùng và ảnh đại diện có thể tùy chỉnh.
- **Lịch Sử Xem & Yêu Thích**: Đồng bộ hóa lịch sử xem và các phim đã lưu trên tất cả các nền tảng.
- **UI/UX Cao Cấp**: Chuyển động vi mô mượt mà (Framer Motion) và bố cục tương thích với mọi thiết bị.

### 📺 Phát Trực Tuyến Chất Lượng Cao
- **Hỗ Trợ HLS**: Phát trực tuyến HTTP Live Streaming hiệu suất cao, đảm bảo xem mượt mà ngay cả với kết nối mạng yếu.
- **Tính Năng Trình Phát**: Chọn tập phim, điều khiển tốc độ và điều hướng liền mạch.

---

## 🛠️ Chi Tiết Công Nghệ

| Thành phần | Công nghệ sử dụng |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot, Spring Security, JWT, MySQL, Redis, MapStruct, Swagger |
| **Frontend** | React 19, TypeScript, Tailwind CSS 4, TanStack Query, Zustand, Framer Motion |
| **Android** | Kotlin, Jetpack Compose, Hilt, Room, Retrofit, OkHttp, Media3 ExoPlayer, Coil |
| **Hạ tầng** | Docker, Docker Compose, GitHub Actions (CI/CD) |

---

## 📂 Cấu Trúc Dự Án

```text
web-film/
├── web-film-backend/    # Spring Boot REST API
├── web-film-frontend/   # React (Vite) Web Application
└── web-film-android/    # Dự án Android Native (Compose)
```

---

## 🏁 Bắt Đầu

Để chạy toàn bộ hệ sinh thái, hãy làm theo hướng dẫn thiết lập trong từng thư mục con:

1. **Backend**: [Hướng dẫn thiết lập Backend](file:///d:/Java%20Study/Projects/web-film/web-film-backend/README.md)
2. **Frontend**: [Hướng dẫn thiết lập Frontend](file:///d:/Java%20Study/Projects/web-film/web-film-frontend/README.md)
3. **Android**: Mở thư mục `web-film-android` bằng Android Studio.

---

## 📄 Bản Quyền
Dự án được phát triển bởi **Tam Dao**. Mọi quyền được bảo lưu.
