# 💻 CineStream Web Frontend (React + TS + Tailwind 4)

Đây là thành phần Web Frontend của hệ sinh thái **CineStream**, mang lại trải nghiệm xem phim chất lượng cao, phản hồi nhanh và giao diện Obsidian & Neon Cyan tối giản, hiện đại.

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

- **Core Framework**: React 19, TypeScript, Vite
- **Styling**: Tailwind CSS v4.0 (cho hiệu năng render vượt trội và hỗ trợ biến CSS native)
- **State Management**: 
  - Zustand (Quản lý state toàn cục nhẹ và hiệu quả)
  - TanStack Query v5 (Quản lý server state, caching, fetching và synchronization)
- **Routing**: React Router Dom v7
- **Video Engine**: Vidstack React v0.6+ (Trình phát video HLS hiện đại, tùy biến cao)
- **Animations**: Framer Motion & Swiper (cho các hiệu ứng trượt, carousel và chuyển cảnh mượt mà)
- **Icons**: Lucide React
- **Form Handling**: React Hook Form kết hợp với Zod Validation

---

## ⚙️ Thiết Lập Môi Trường

Bạn có thể thay đổi endpoint kết nối API bằng cách cấu hình trong code hoặc thông qua các biến môi trường của Vite:

1. Tạo file `.env` tại thư mục gốc của frontend:
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   ```

---

## 🏁 Hướng Dẫn Khởi Chạy

Yêu cầu: Đã cài đặt Node.js phiên bản mới nhất (khuyến nghị v18 hoặc v20+).

1. Cài đặt các thư viện/phụ thuộc:
   ```bash
   npm install
   ```

2. Chạy môi trường phát triển (Local Development):
   ```bash
   npm run dev
   ```
   Ứng dụng sẽ chạy trên địa chỉ mặc định `http://localhost:5173`.

3. Biên dịch cho môi trường Production:
   ```bash
   npm run build
   ```
   Bản build tối ưu sẽ được tạo ra trong thư mục `/dist`.

---

## 📁 Cấu Trúc Thư Mục Source Code

```text
src/
├── assets/          # Hình ảnh, font và tài nguyên tĩnh
├── components/      # Các component dùng chung (Button, Input, VideoPlayer...)
├── context/         # React Contexts
├── hooks/           # Custom React Hooks
├── layouts/         # Bố cục giao diện (MainLayout, AuthLayout)
├── pages/           # Các trang chính (Home, MovieDetail, Search, Profile...)
├── services/        # Các service gọi API (Axios instance, queries...)
├── store/           # Zustand stores (Auth store, UI store...)
├── types/           # Định nghĩa TypeScript interfaces/types
├── utils/           # Hàm tiện ích dùng chung
├── App.tsx          # Điểm bắt đầu của React App
└── main.tsx         # File entry khởi tạo React
```
