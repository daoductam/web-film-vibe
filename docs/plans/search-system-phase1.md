# CineStream Search Architecture & Feature Documentation (Phase 1)

## 1. Tổng quan (Overview)
Hệ thống tìm kiếm của **CineStream** được nâng cấp toàn diện nhằm giải quyết các bài toán tìm kiếm thực tế của nền tảng streaming phim (lấy cảm hứng từ Netflix), mang lại trải nghiệm mượt mà, chính xác và thông minh:
- **Tốc độ phản hồi cao**: Autocomplete và gợi ý phim tức thì (Instant Search) với Debounce giảm tải server.
- **Khử dấu tiếng Việt thông minh**: Người dùng gõ có dấu hay không dấu đều trả về kết quả chính xác tuyệt đối.
- **Tìm kiếm đa trường có trọng số (Multi-Field Weighted Search)**: Xếp hạng ưu tiên theo mức độ khớp (tiêu đề, tên gốc, diễn viên, đạo diễn, thể loại) và độ phổ biến (lượt xem).
- **Tìm kiếm ngữ nghĩa bằng AI (AI Semantic Search)**: Tận dụng mô hình Groq LLM (Llama 3.3) để bóc tách ý định từ câu nói tự nhiên thành các tiêu chí lọc chính xác.
- **Cá nhân hóa kết quả (Personalized Re-ranking)**: Tận dụng cơ sở dữ liệu đồ thị Neo4j để đẩy các phim thuộc thể loại yêu thích của người dùng lên đầu trang.

---

## 2. Kiến trúc & Chi tiết triển khai

```
+-------------------------------------------------------------------------------+
|                                Frontend (React)                               |
|  - Navbar Autocomplete Dropdown (Debounce 280ms, Top 5 suggestions)          |
|  - Recent Searches & Quick Trends (LocalStorage)                              |
|  - Search Page: Full Pagination + AI Semantic Search Toggle Mode              |
+---------------------------------------+---------------------------------------+
                                        |
                 REST API (HTTP)        |
                                        v
+-------------------------------------------------------------------------------+
|                            Backend (Spring Boot)                              |
|                                                                               |
|  [ VietnameseStringUtils ]  --> Chuẩn hóa NFD, tách dấu thanh, đ/Đ, sinh slug |
|                                                                               |
|  [ MovieController / Service ]                                                |
|    |                                                                          |
|    +---> GET /v1/movies/search?q=...                                          |
|    |       * Tìm kiếm JPQL đa trường (title, originTitle, slug, cast, cat...) |
|    |       * Trọng số CASE WHEN (1->9) + ORDER BY viewCount DESC              |
|    |       * Neo4j Re-ranking nếu User đã đăng nhập                          |
|    |                                                                          |
|    +---> GET /v1/ai/search?q=...                                              |
|            * Gửi prompt tự nhiên tới Groq Llama 3.3                           |
|            * Trích xuất ParsedAIIntent (category, country, type, year, key)  |
|            * Tìm kiếm đa tiêu chí + Fallback                                  |
+-------------------+-----------------------------------+-----------------------+
                    |                                   |
                    v                                   v
+-----------------------------+       +-----------------------------------------+
|     MySQL (Primary DB)      |       |      Neo4j (Graph Recommendation)       |
|  - Movies, Categories       |       |  - (u:User)-[:FAVORITED|WATCHED]->(m)   |
|  - Full entity data         |       |  - Cypher: getTopCategorySlugsForUser   |
+-----------------------------+       +-----------------------------------------+
```

---

## 3. Các thành phần chi tiết

### 3.1. Chuẩn hóa tiếng Việt (Vietnamese Normalization)
- **File**: `web-film-backend/src/main/java/com/tamdao/web_film_backend/util/VietnameseStringUtils.java`
- **Tính năng**:
  - Khử trước các biến thể ký tự `đ`, `Đ`, `\u0111`, `\u0110` (do chuẩn NFD của Unicode không phân tách gạch ngang của chữ đ).
  - Sử dụng `Normalizer.normalize(..., Form.NFD)` và Regex `\p{InCombiningDiacriticalMarks}+` bóc tách triệt để mọi nguyên âm và dấu thanh tiếng Việt.
  - Chuẩn hóa khoảng trắng đặc thù trên web (`NBSP \u00A0`, multiple spaces).
  - Tự động sinh `slug` chuẩn SEO để khớp nối từ khóa không dấu với slug trong database.
  - *Ví dụ*: Người dùng gõ `tro choi doi tra` -> Hệ thống so sánh khớp nối với slug `tro-choi-doi-tra-...` của phim `Trò Chơi Dối Trá`.

### 3.2. Tìm kiếm đa trường có trọng số (Multi-Field Weighted Search)
- **File**: 
  - `MovieRepository.java` (`searchByKeyword`)
  - `MovieService.java`
- **Bảng điểm trọng số ưu tiên (ORDER BY CASE)**:
  1. Khớp chính xác tên phim (`LOWER(m.title) = :keyword`) ➔ **Độ ưu tiên 1 (Cao nhất)**
  2. Khớp tiền tố tên phim (`LIKE :keyword%`) ➔ **Độ ưu tiên 2**
  3. Khớp một phần tên phim (`LIKE %:keyword%`) ➔ **Độ ưu tiên 3**
  4. Khớp tiền tố slug không dấu ➔ **Độ ưu tiên 4**
  5. Khớp một phần slug không dấu ➔ **Độ ưu tiên 5**
  6. Khớp tên gốc tiếng Anh/Quốc tế (`originTitle`) ➔ **Độ ưu tiên 6**
  7. Khớp tên diễn viên / đạo diễn ➔ **Độ ưu tiên 7**
  8. Khớp tên thể loại (`c.name`, `c.slug` qua `LEFT JOIN categories`) ➔ **Độ ưu tiên 8**
  9. Khớp nội dung mô tả (`description`) ➔ **Độ ưu tiên 9**
  - Khi cùng bậc ưu tiên: Sắp xếp phụ theo `m.viewCount DESC` (phim phổ biến, nhiều lượt xem hơn sẽ lên trước).

### 3.3. Tìm kiếm AI Ngữ nghĩa (Groq AI Llama 3.3 Intent Parsing)
- **File**:
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/service/ai/AIService.java`
  - `web-film-backend/src/main/java/com/tamdao/web_film_backend/controller/AIController.java`
- **API Endpoint**: `GET /api/v1/ai/search?q={query}&page={page}&size={size}`
- **Cách hoạt động**:
  - Prompt chuẩn ép kiểu Groq Llama 3.3 trả về JSON cấu trúc:
    ```json
    {
      "isMovieQuery": true,
      "categories": ["hoat-hinh", "hai-huoc", "hoc-duong"],
      "country": "nhat-ban",
      "type": "SERIES",
      "year": null,
      "keyword": "phép thuật",
      "summary": "Phim hoạt hình Nhật Bản hài hước học đường có yếu tố phép thuật"
    }
    ```
  - Backend gọi `searchMoviesByDescriptionKeyword` theo các tham số trích xuất được.
  - Có cơ chế Fallback tự động sang tìm kiếm thông thường nếu dịch vụ AI quá tải.
  - Frontend hiển thị banner giải thích trực quan các tiêu chí AI đã tự động phát hiện.

### 3.4. Cá nhân hóa kết quả tìm kiếm với Neo4j (Personalized Re-ranking)
- **File**:
  - `MovieNeo4jRepository.java` (`getTopCategorySlugsForUser`)
  - `MovieService.java` (`searchMovies(keyword, username, page, size)`)
- **Cách hoạt động**:
  - Truy vấn Cypher tính điểm sở thích:
    ```cypher
    MATCH (u:User {username: $username})-[r:FAVORITED|WATCHED]->(m:Movie)-[:BELONGS_TO]->(c:Category)
    RETURN c.slug
    ORDER BY SUM(CASE type(r) WHEN 'FAVORITED' THEN 3.0 ELSE 1.0 END) DESC
    LIMIT 5
    ```
  - Khi User đăng nhập tìm kiếm: Kết quả phim trả về sẽ được re-ranking tại memory service; các bộ phim thuộc thể loại User có điểm tương tác cao sẽ được ưu tiên xuất hiện ở trang đầu.
  - Người dùng ẩn danh (khách vãng lai): Giữ nguyên thứ tự trọng số mặc định.

### 3.5. Trải nghiệm Frontend (UI/UX Best Practices)
- **File**:
  - `web-film-frontend/src/components/layout/Navbar.tsx`
  - `web-film-frontend/src/pages/search/SearchPage.tsx`
  - `web-film-frontend/src/hooks/useDebounce.ts`
- **Tính năng**:
  - **Autocomplete Instant Search**: Popover thả xuống dưới ô tìm kiếm khi gõ >= 2 ký tự (có ảnh thumb, tag chất lượng, số sao rating, năm chiếu).
  - **Recent Searches**: Quản lý lịch sử tìm kiếm cục bộ (LocalStorage), có thể click vào để tìm lại ngay hoặc xóa từng mục / xóa tất cả.
  - **Popular Search Chips**: Xu hướng tìm kiếm theo thể loại (Hành động, Anime, Kinh dị,...).
  - **Toggle AI Semantic Search**: Chuyển đổi giữa chế độ tìm kiếm tiêu chuẩn và chế độ AI thông minh bằng ngôn ngữ tự nhiên.
  - **Pagination & Counter**: Sửa triệt để lỗi đếm tổng số phần tử (`page.totalElements`), phân trang số thông minh mượt mà.

---

## 4. Hướng dẫn kiểm thử (Verification)
1. **Kiểm tra tìm kiếm tiếng Việt không dấu**:
   - Truy cập `/search?q=tro choi doi tra` ➔ Hệ thống trả về chính xác phim *Trò Chơi Dối Trá*.
2. **Kiểm tra Autocomplete**:
   - Tại Navbar, gõ `tro` ➔ Hiển thị dropdown gợi ý phim tức thì.
3. **Kiểm tra AI Semantic Search**:
   - Bật toggle **AI Semantic Search (Llama 3.3)** trên trang `/search`.
   - Nhập: `tìm phim hoạt hình anime hài hước học đường`.
   - Kết quả: Hiển thị banner giải thích của AI và các bộ phim lọc theo `#hoat-hinh`, `#hai-huoc`.
4. **Kiểm tra Personalized Re-ranking**:
   - Đăng nhập tài khoản có lịch sử xem nhiều phim Hoạt hình / Anime.
   - Tìm kiếm từ khóa chung, các phim thể loại Hoạt hình sẽ được đẩy lên trước.
