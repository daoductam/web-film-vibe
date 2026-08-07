# 🖥️ CineStream Backend API (Spring Boot)

Đây là thành phần Backend API của hệ sinh thái **CineStream**, được xây dựng bằng Java 21 và Spring Boot. Hệ thống cung cấp RESTful APIs quản lý phim, người dùng, lịch sử xem, tích hợp AI Chatbot và Crawler tự động cập nhật dữ liệu phim.

---

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

- **Core**: Java 21, Spring Boot 4.x
- **Database**: MySQL (Primary Database), Spring Data JPA
- **Caching**: Redis (Cache siêu dữ liệu phim, cấu hình qua Spring Cache)
- **Security**: Spring Security + JWT (JSON Web Token) cho xác thực phi trạng thái (stateless)
- **Object Mapping**: MapStruct & Lombok
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Integration**:
  - WebClient (Spring WebFlux) cho Crawler tự động cập nhật dữ liệu phim
  - Groq AI SDK/API để tích hợp chatbot thông minh hỗ trợ người dùng tìm và gợi ý phim

---

## ⚙️ Cấu Hình Hệ Thống

Trước khi khởi chạy, bạn cần tạo file `.env` nằm ở thư mục gốc của backend (`web-film-backend/.env`) dựa trên file mẫu `.env.example`:

```properties
# Mật khẩu database MySQL
DATABASE_PASSWORD=your_mysql_password

# Chuỗi bí mật ký JWT Token (Base64)
JWT_SECRET=dGFtZGFvLXdlYi1maWxtLXNlY3JldC1rZXktMjAyNi1wcm9kdWN0aW9uLXZlcnk=

# Tích hợp AI (Groq API Key)
GROQ_API_KEY=your_groq_api_key_here
```

---

## 🏁 Hướng Dẫn Khởi Chạy

### Cách 1: Chạy trực tiếp qua Maven (Local Development)

Yêu cầu: Đã cài đặt Java 21 và các Service (MySQL, Redis) đang chạy trên máy local của bạn.

1. Cài đặt các dependencies:
   ```bash
   ./mvnw clean install
   ```
2. Khởi chạy ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```

Ứng dụng sẽ mặc định khởi chạy trên cổng **8080** (`http://localhost:8080`).

### Cách 2: Sử dụng Docker Compose (Khuyên dùng)

Hệ thống đã được cấu hình Docker Compose để khởi chạy nhanh chóng tất cả dịch vụ đi kèm bao gồm Backend, MySQL, Redis:

```bash
docker-compose up -d --build
```

Lệnh trên sẽ tự động dựng hình ảnh docker của ứng dụng backend và kéo các container database MySQL, Redis về thiết lập tự động kết nối.

---

## 📖 Tài Liệu API (OpenAPI/Swagger)

Sau khi ứng dụng đã khởi chạy thành công, bạn có thể truy cập Swagger UI để xem chi tiết các endpoint và test trực tiếp:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

---

## 📁 Cấu Trúc Thư Mục Source Code

```text
src/main/java/com/tamdao/web_film_backend/
├── config/             # Cấu hình Spring (Security, Cache, Redis, Cors...)
├── controller/         # Lớp REST Controllers cung cấp API endpoints
├── dto/                # Data Transfer Objects (Request/Response)
├── exception/          # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)
├── filter/             # Bộ lọc servlet (JWT filter, Rate Limiting...)
├── mapper/             # Cấu hình MapStruct chuyển đổi Entity <-> DTO
├── model/              # Lớp Entity cơ sở dữ liệu
├── repository/         # Tương tác cơ sở dữ liệu (Spring Data JPA)
├── service/            # Xử lý logic nghiệp vụ chính
└── utils/              # Các class tiện ích (JWT provider, Crawler...)
```
