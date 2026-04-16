# AGENTS.md

## Project Overview
CineStream là một hệ sinh thái xem phim toàn diện (full-stack) được thiết kế để mang lại trải nghiệm xem phim cao cấp trên cả nền tảng Web và Android. Dự án bao gồm Backend Spring Boot mạnh mẽ, Web Frontend React hiện đại và ứng dụng Android Native sử dụng Jetpack Compose.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.4+, MySQL (Primary DB), Redis (Caching), Spring Security (JWT), MapStruct.
- **Frontend**: React 19 (Vite), TypeScript, Tailwind CSS 4, TanStack Query, Zustand, Framer Motion, Vidstack (HLS Player).
- **Android**: Kotlin, Jetpack Compose, Material 3, Hilt (DI), Room (Local Data), Retrofit (Networking), Media3 ExoPlayer (HLS Streaming).
- **Infrastructure**: Docker, Docker Compose, GitHub Actions (CI/CD).

## Project Structure
- `web-film-backend/`: Thư mục mã nguồn Spring Boot REST API.
- `web-film-frontend/`: Thư mục mã nguồn React Web Application.
- `web-film-android/`: Thư mục mã nguồn ứng dụng Android Native.
- `.agent/`: Chứa bộ não của AI (workflows, rules, skills).

## Operational Resources (AI Context)
Mọi hành động của AI phải soi chiếu qua các tài nguyên này:
- **Workflows:** Tham khảo `.agent/workflows/` (`feature.md` cho tính năng mới, `debug.md` cho sửa lỗi, `improve.md` cho tối ưu hóa).
- **Coding Rules:** Tham khảo `.agent/rules/` (Tuân thủ nghiêm ngặt `code-quality.md` và `security.md`).
- **Special Skills:** Tham khảo `.agent/skills/` (Sử dụng các skill tương ứng như `api-design`, `brainstorming`, `database-design` khi cần).

## Conventions
- **API Naming**: Tuân thủ RESTful API, versioning qua URL (v1).
- **Validation**: Sử dụng Bean Validation (JSR 380) cho dữ liệu đầu vào backend.
- **UI/UX**: 
    - Web: Tailwind CSS 4, Responsive design, Micro-animations mượt mà.
    - Android: Jetpack Compose, Material 3 design system.
- **Streaming**: Sử dụng chuẩn HLS (HTTP Live Streaming) cho cả Web và Android.
- **Commit Message**: Tuân thủ Conventional Commits (feat, fix, docs, style, refactor, test, chore).

## Commands
### Backend (`web-film-backend/`)
- `./mvnw spring-boot:run` — Khởi chạy ứng dụng
- `./mvnw clean install` — Build dự án và cài đặt dependencies
- `./mvnw test` — Chạy Unit Test

### Frontend (`web-film-frontend/`)
- `npm run dev` — Khởi chạy môi trường phát triển (Vite)
- `npm run build` — Build sản phẩm cho production
- `npm install` — Cài đặt dependencies

### Android (`web-film-android/`)
- `./gradlew assembleDebug` — Build file APK Debug
- Mở bằng Android Studio để phát triển và debugging mạnh mẽ hơn.