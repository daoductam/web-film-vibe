# CineStream - Ứng dụng Xem Phim Premium cho Android

![CineStream Logo](app/src/main/res/drawable/ic_cinestream_logo.xml) <!-- Placeholder for logo reference -->

**CineStream** là một ứng dụng xem phim trực tuyến hiện đại được xây dựng với các công nghệ mới nhất của Android, mang lại trải nghiệm người dùng mượt mà, giao diện "Premium" chuẩn Obsidian và hiệu năng tối ưu.

## 🚀 Tính năng nổi bật

- **🎨 Giao diện Obsidian & Neon Cyan**: Thiết kế tối giản, sang trọng với các hiệu ứng Glassmorphism và Shimmer loading mượt mà.
- **🏠 Trang chủ Đa dạng**: Cung cấp Phim mới nhất, Phim phổ biến và Hero Section nổi bật.
- **🔍 Hệ thống Tìm kiếm Thông minh**: Phân loại theo 17+ thể loại, tìm kiếm nhanh chóng với hiệu ứng tối ưu.
- **🎬 Trình phát Video Nhúng**: Tích hợp ExoPlayer mạnh mẽ, hỗ trợ chuyển tập và xem trực tiếp.
- **❤️ Thư viện Cá nhân**: Lưu trữ các bộ phim yêu thích của bạn chỉ với một nút bấm "Thả tim".
- **📺 Lịch sử Xem**: Tự động lưu lại tiến trình xem phim để bạn có thể tiếp tục bất cứ lúc nào.
- **⚡ Hiệu năng Cao**: Sử dụng kiến trúc hiện đại, tải dữ liệu nhanh chóng và mượt mà.

## 🛠 Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Kiến trúc**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room (Lưu trữ cục bộ cho Thư viện & Lịch sử)
- **Networking**: Retrofit & OkHttp
- **Image Loading**: Coil
- **Video Player**: Media3 ExoPlayer
- **Navigation**: Compose Navigation

## 📂 Cấu trúc thư mục

```text
app/src/main/java/com/tamdao/cinestream/
├── core/           # Các thành phần dùng chung (Database, Network, UI Components)
├── data/           # Repository và Data Models
├── feature/        # Các module tính năng (Home, Search, Library, Player, Detail)
└── ui/             # Theme và Styling
```

## 🛠 Hướng dẫn Cài đặt

1. Clone repository này về máy.
2. Mở project bằng Android Studio (Ladybug trở lên).
3. Đảm bảo Backend đã được khởi chạy tại địa chỉ cấu hình trong `NetworkModule.kt`.
4. Nhấn **Run** để khởi động ứng dụng trên Emulator hoặc thiết bị thực.

## 🤝 Đóng góp

Mọi ý kiến đóng góp và báo lỗi xin vui lòng tạo Issue hoặc Pull Request. Chúng tôi luôn hoan nghênh sự nhiệt tình từ cộng đồng!

---
*Phát triển bởi TamDao Team - 2026*
