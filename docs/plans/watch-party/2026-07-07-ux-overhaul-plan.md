# Implementation Plan: Watch Party UX Overhaul

Spec Source: `docs/plans/watch-party/2026-07-07-ux-overhaul-spec.md`
Owner: CineStream Android Team
Last Updated: 2026-07-07
Status: `Draft`

---

# 1. Context

**Problem:**
Watch Party trên Android có UX rất kém — người dùng không hiểu tính năng dùng để làm gì, form tạo phòng yêu cầu nhập Movie ID thủ công (không ai biết ID), và lỗi quyền truy cập hiện thông báo khó hiểu. Kết quả là tính năng gần như không sử dụng được.

**Affected Modules:**
- `web-film-backend/config` — fix SecurityConfig để permit endpoint public
- `web-film-android/feature/watchparty` — redesign lobby, thêm movie picker, empty state
- `web-film-android/feature/detail` — thêm entry point "Xem cùng bạn bè"
- `web-film-android/core/navigation` — cập nhật routes

**Non-Goals:**
- Tìm kiếm phòng theo tên
- Invite bạn bè qua deep link
- Push notification khi bạn bè tạo phòng
- Custom avatar cho phòng
- Thay đổi backend Watch Party logic (chỉ fix security config)

---

# 2. Constraints

**Language:** Java (Backend), Kotlin (Android)
**Architecture:** Spring Boot REST (Backend), MVVM + Hilt DI (Android)

**Rules:**
- Do NOT modify unrelated modules
- Do NOT introduce new external dependencies (all APIs already exist)
- Maintain backward compatibility with existing Watch Party API
- Tận dụng `MovieApiService.searchMovies()` và `getPopularMovies()` đã có

**Performance Budgets:**
- Movie search debounce: 300ms
- Movie Picker grid load: < 500ms perceived

---

# 3. Conventions

**Naming:**
- Classes: `PascalCase` — e.g. `MoviePickerBottomSheet`, `WatchPartyLobbyScreen`
- Methods/variables: `camelCase` — e.g. `searchMovies()`, `selectedMovie`
- Constants: `UPPER_SNAKE_CASE`
- Packages: `com.tamdao.cinestream.feature.watchparty`

**Error Handling:**
- Android: `Result<T>` pattern với `safeApiCall` wrapper (existing pattern in `WatchPartyRepository`)
- Backend: `ApiResponse<T>` wrapper (existing pattern)
- UI: Show error via `AlertDialog` hoặc `Text` trong Error state

**Logging:**
- Android: `android.util.Log` with tag `"WatchPartyViewModel"`
- Backend: SLF4J (lombok `@Slf4j`)

**Testing:**
- Backend: JUnit 5 + MockMvc (existing `WatchRoomControllerTest.java`)
- Android: Manual smoke test (no unit test framework setup for compose screens)

**Task Size:**
- Max 200 LOC per task (excluding tests)
- If a task exceeds this, split into subtasks

---

# 4. Contracts

## Existing Interfaces (Reused)

```
MovieApiService.searchMovies(query: String, page: Int, size: Int): ApiResponse<PageResult<MovieDto>>
  - pre: query is non-blank
  - post: returns paginated movie results matching query
  - throws: HttpException on network failure

MovieApiService.getPopularMovies(page: Int, size: Int): ApiResponse<PageResult<MovieDto>>
  - pre: none
  - post: returns paginated popular movies
  - throws: HttpException on network failure
```

## New Methods in WatchPartyViewModel

```
WatchPartyViewModel.searchMoviesForPicker(query: String): Unit
  - pre: query is non-blank, trimmed
  - post: updates _movieSearchResults StateFlow with matching movies
  - side-effect: cancels previous search job (debounce)
  - throws: none (catches internally, updates error state)

WatchPartyViewModel.loadPopularMoviesForPicker(): Unit
  - pre: none
  - post: updates _movieSearchResults StateFlow with popular movies
  - throws: none (catches internally)
```

## New UI State

```
MoviePickerUiState {
  SEALED:
    Loading      — initial load or search in progress
    Success      — movies: List<MovieDto>
    Error        — message: String
}
```

## Existing Data Structures (Unchanged)

```
MovieDto {
  id:          Long     — used as movieId for CreateWatchRoomRequest
  title:       String   — displayed in picker grid
  slug:        String   — for navigation
  posterUrl:   String?  — displayed as poster image
  thumbUrl:    String?  — fallback image
  year:        Int?     — display context
  quality:     String?  — display badge
}

CreateWatchRoomRequest {
  name:        String   — room name, non-blank
  movieId:     Long     — selected movie ID from picker
  episodeId:   Long?    — nullable, not used in initial flow
  roomType:    String   — "PUBLIC" | "PRIVATE"
  maxMembers:  Int      — 2..50
}
```

---

# 5. Target Architecture

**Components:**
- `SecurityConfig` (Backend) — permit public rooms endpoint
- `WatchPartyLobbyScreen` (Android) — redesigned lobby with hero, steps, empty state
- `MoviePickerBottomSheet` (Android) — full-screen bottom sheet for movie selection
- `CreateRoomDialog` (Android) — simplified dialog with pre-selected movie
- `WatchPartyViewModel` (Android) — new search/popular methods, inject MovieRepository
- `MovieDetailScreen` (Android) — new "Xem cùng bạn bè" button
- `Screen.kt` / `MainActivity.kt` (Android) — updated navigation routes

**Interaction Flow:**

```
Flow A: Tạo phòng từ Lobby
  User taps FAB/CTA "Tạo phòng"
    → MoviePickerBottomSheet opens
      → ViewModel.loadPopularMoviesForPicker() → display grid
      → User types search → ViewModel.searchMoviesForPicker(query) → update grid
      → User taps movie → selectedMovie = MovieDto
    → CreateRoomDialog opens (pre-filled with selectedMovie)
      → User fills name, type, maxMembers
      → ViewModel.createRoom(request) → navigate to WatchPartyRoom

Flow B: Tạo phòng từ MovieDetail
  User on MovieDetailScreen → taps "Xem cùng bạn bè"
    → Navigate to WatchPartyLobby(movieId=X, movieTitle=Y)
    → Lobby auto-opens CreateRoomDialog (pre-filled, skip picker)
    → User fills name, type, maxMembers
    → ViewModel.createRoom(request) → navigate to WatchPartyRoom
```

**Key Decisions:**
- Movie Picker dùng `ModalBottomSheet` Material3 (native, không cần thêm dependency)
- Search debounce 300ms trong ViewModel (dùng `Job.cancel()` pattern)
- ViewModel inject `MovieRepository` thay vì `MovieApiService` trực tiếp (consistency với existing pattern)
- Navigation dùng optional query params (không phải path params) cho MovieDetail → Lobby flow

---

# 6. Artifact Registry

| Artifact | Type | Owner Task | Implements |
|----------|------|------------|------------|
| `SecurityConfig.java` | config | TASK-001 | — (security fix) |
| `WatchPartyViewModel.kt` | viewmodel | TASK-002 | `searchMoviesForPicker`, `loadPopularMoviesForPicker` |
| `WatchPartyLobbyScreen.kt` | screen | TASK-003, TASK-005 | Hero, Steps, Empty State, Picker integration |
| `MoviePickerBottomSheet` (in LobbyScreen) | composable | TASK-004 | Movie selection UI |
| `CreateRoomDialog` (in LobbyScreen) | composable | TASK-004 | Simplified with movie preview |
| `MovieDetailScreen.kt` | screen | TASK-005 | "Xem cùng bạn bè" button |
| `Screen.kt` | navigation | TASK-005 | Updated WatchPartyLobby route |
| `MainActivity.kt` | nav graph | TASK-005 | Updated composable args |

---

# 7. Task Graph

| ID | Name | Depends On | Effort |
|----|------|------------|--------|
| TASK-001 | Fix Security Config | — | S |
| TASK-002 | ViewModel Movie Search | — | S |
| TASK-003 | Redesign Lobby UI | — | M |
| TASK-004 | Movie Picker + Create Dialog | TASK-002 | M |
| TASK-005 | MovieDetail Entry Point + Navigation | TASK-004 | M |
| TASK-006 | Smoke Test & Polish | TASK-001..005 | S |

**Dependency Graph:**

```
TASK-001 (Backend) ─────────────────────┐
                                        │
TASK-002 (ViewModel) ── TASK-004 ─┐     │
                                  ├── TASK-005 ── TASK-006
TASK-003 (Lobby UI) ──────────────┘
```

**Execution Rules:**
- TASK-001, TASK-002, TASK-003 can run in parallel (no dependencies)
- TASK-004 requires TASK-002 (needs search/popular methods in ViewModel)
- TASK-005 requires TASK-004 (needs MoviePicker + CreateDialog to be ready)
- TASK-006 is final integration & polish

---

# 8. Task Specifications

## TASK-001: Fix Security Config (Backend)

**Description:**
Thêm permit rule cho `GET /v1/watch-rooms/public` trong `SecurityConfig.java` để endpoint lấy phòng công khai không yêu cầu authentication.

**Input:**
- None (root task)

**Output:**
- Updated `SecurityConfig` — fixes "Không có quyền truy cập" error

**Files:**
- `web-film-backend/src/main/java/com/tamdao/web_film_backend/config/SecurityConfig.java` — **modify**: thêm `.requestMatchers(HttpMethod.GET, "/v1/watch-rooms/public").permitAll()` vào chain `authorizeHttpRequests`

**Responsibilities:**
- CHỈ permit `GET` method cho `/v1/watch-rooms/public`
- KHÔNG permit các endpoint khác (create, join, leave, end vẫn cần auth)
- Đặt rule TRƯỚC `.anyRequest().authenticated()`

**Acceptance Criteria:**
- [ ] `GET /v1/watch-rooms/public` trả 200 mà KHÔNG cần JWT token
- [ ] `POST /v1/watch-rooms` vẫn trả 401 khi không có token
- [ ] `POST /v1/watch-rooms/{id}/join` vẫn trả 401 khi không có token
- [ ] Existing tests pass (`WatchRoomControllerTest`)
- [ ] Code compiles với zero warnings

---

## TASK-002: ViewModel Movie Search Methods

**Description:**
Thêm khả năng tìm kiếm và load phim phổ biến vào `WatchPartyViewModel` bằng cách inject `MovieRepository` và tạo các method + state mới.

**Input:**
- None (root task)

**Output:**
- `WatchPartyViewModel` với `moviePickerState`, `searchMoviesForPicker()`, `loadPopularMoviesForPicker()`
- Consumed by TASK-004 (MoviePickerBottomSheet)

**Files:**
- `web-film-android/.../feature/watchparty/WatchPartyViewModel.kt` — **modify**:
  - Inject `MovieRepository` vào constructor
  - Thêm `MoviePickerUiState` sealed class
  - Thêm `_moviePickerState: MutableStateFlow<MoviePickerUiState>`
  - Thêm `searchMoviesForPicker(query: String)` method với debounce
  - Thêm `loadPopularMoviesForPicker()` method
  - Thêm `private var searchJob: Job?` cho debounce

**Responsibilities:**
- Implement `searchMoviesForPicker()` per contract in Section 4
- Implement `loadPopularMoviesForPicker()` per contract in Section 4
- Debounce search 300ms (cancel previous job before starting new)
- Handle empty results gracefully

**Acceptance Criteria:**
- [ ] `searchMoviesForPicker("avenger")` → updates `moviePickerState` to `Success(movies)` 
- [ ] `searchMoviesForPicker("")` → no-op hoặc load popular
- [ ] `loadPopularMoviesForPicker()` → updates `moviePickerState` to `Success(movies)`
- [ ] Network error → `moviePickerState` = `Error(message)`
- [ ] Rapid typing triggers only 1 API call (debounce works)
- [ ] Code compiles with zero warnings

---

## TASK-003: Redesign Lobby UI (Hero + Steps + Empty State)

**Description:**
Redesign `WatchPartyLobbyScreen` với Hero section, Quick Start steps, và improved empty state. Task này chỉ thay đổi phần layout/visual, KHÔNG thay đổi logic tạo/join phòng.

**Input:**
- None (root task, purely UI)

**Output:**
- Redesigned `WatchPartyLobbyScreen` — consumed by TASK-004 và TASK-005

**Files:**
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt` — **modify**:
  - Thêm `HeroSection` composable (gradient background, icon, title, subtitle)
  - Thêm `QuickStartSteps` composable (3 bước ngang)
  - Cải thiện empty state (icon lớn + text + CTA button)
  - Giữ nguyên `PublicRoomCard`, join-by-code card logic

**Responsibilities:**
- Hero section: gradient NeonCyan → Obsidian, rounded corners
- Quick Start: 3 items horizontal, translucent cards
- Empty state: icon 80dp + text + Button "Tạo phòng ngay"
- Maintain existing room list and join-by-code functionality

**Acceptance Criteria:**
- [ ] Hero section hiển thị đúng với gradient + text
- [ ] Quick Start 3 bước hiển thị đúng responsive (không bị wrap)
- [ ] Empty state có icon + text + CTA button
- [ ] CTA button "Tạo phòng ngay" trigger `showCreateDialog = true`
- [ ] Existing join-by-code vẫn hoạt động
- [ ] Public room list vẫn hiển thị đúng
- [ ] Code compiles with zero warnings

---

## TASK-004: Movie Picker BottomSheet + Simplified Create Dialog

**Description:**
Tạo `MoviePickerBottomSheet` (full-screen, search + grid poster) và refactor `CreateRoomDialog` để nhận `MovieDto` thay vì yêu cầu nhập ID thủ công.

**Input:**
- From TASK-002: `WatchPartyViewModel.moviePickerState`, `searchMoviesForPicker()`, `loadPopularMoviesForPicker()`

**Output:**
- `MoviePickerBottomSheet` composable + simplified `CreateRoomDialog`
- Consumed by TASK-005 (entry point from MovieDetail)

**Files:**
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt` — **modify**:
  - Thêm `MoviePickerBottomSheet` composable:
    - `ModalBottomSheet` với `skipPartiallyExpanded = true`
    - Search bar (OutlinedTextField) phía trên
    - LazyVerticalGrid 3 cột hiển thị poster + title
    - Loading/Error states
  - Refactor `CreateRoomDialog`:
    - Thêm param `preSelectedMovie: MovieDto?`
    - Bỏ field "Mã phim (ID)"
    - Thêm movie preview (poster nhỏ + title) ở đầu dialog
    - `movieId` tự động lấy từ `preSelectedMovie.id`
  - Cập nhật flow: FAB/CTA → Picker → chọn phim → Dialog

**Responsibilities:**
- MoviePicker: grid layout 3 cột, poster aspect ratio, search debounce integration
- CreateDialog: validate tên phòng non-blank, movieId non-null
- Handle no search results: hiện text "Không tìm thấy phim"
- Handle loading state: CircularProgressIndicator

**Acceptance Criteria:**
- [ ] FAB click → opens MoviePickerBottomSheet
- [ ] Bottom sheet loads popular movies on open
- [ ] Typing in search → debounced search → results update
- [ ] Tapping movie poster → closes picker → opens CreateRoomDialog with movie pre-filled
- [ ] CreateRoomDialog shows movie poster + title preview
- [ ] Bỏ hoàn toàn field "Mã phim (ID)"
- [ ] Tạo phòng thành công → navigate to room
- [ ] Edge case: empty search results → show friendly message
- [ ] Code compiles with zero warnings

---

## TASK-005: MovieDetail Entry Point + Navigation Updates

**Description:**
Thêm nút "Xem cùng bạn bè" vào `MovieDetailScreen` và cập nhật navigation route cho `WatchPartyLobby` để hỗ trợ optional movie pre-fill params.

**Input:**
- From TASK-004: `CreateRoomDialog` accepts `preSelectedMovie: MovieDto?`

**Output:**
- Complete end-to-end flow: MovieDetail → Lobby → CreateDialog (pre-filled)

**Files:**
- `web-film-android/.../core/navigation/Screen.kt` — **modify**: 
  - Cập nhật `WatchPartyLobby` route thêm optional query params `?movieId={movieId}&movieTitle={movieTitle}&moviePoster={moviePoster}`
- `web-film-android/.../MainActivity.kt` — **modify**: 
  - Cập nhật composable `WatchPartyLobby` trong NavGraph để parse optional params
  - Truyền params vào `WatchPartyLobbyScreen`
- `web-film-android/.../feature/detail/MovieDetailScreen.kt` — **modify**: 
  - Thêm nút `IconButton` với icon `LiveTv` trong TopAppBar actions
  - onClick: navigate đến WatchPartyLobby với movieId, movieTitle, moviePoster
- `web-film-android/.../feature/watchparty/WatchPartyLobbyScreen.kt` — **modify**: 
  - Thêm params `preSelectedMovieId`, `preSelectedMovieTitle`, `preSelectedMoviePoster`
  - Nếu params có → auto-open CreateRoomDialog với movie pre-filled (skip picker)

**Responsibilities:**
- Navigation: optional params pattern (query params, not path params)
- MovieDetail: button chỉ hiện khi movie loaded thành công
- Lobby: auto-open dialog khi nhận pre-selected movie
- URL encode movie title/poster để tránh special chars

**Acceptance Criteria:**
- [ ] MovieDetailScreen hiển thị nút "Xem cùng bạn bè" khi movie loaded
- [ ] Tap nút → navigate đến Lobby với movie params
- [ ] Lobby tự động mở CreateRoomDialog với phim pre-filled
- [ ] CreateRoomDialog hiện đúng thông tin phim
- [ ] Navigate bình thường đến Lobby (không có params) vẫn hoạt động
- [ ] Back navigation hoạt động đúng
- [ ] Code compiles with zero warnings

---

## TASK-006: Smoke Test & Polish

**Description:**
Chạy smoke test toàn bộ flow, fix visual issues, và polish UX details.

**Input:**
- From ALL previous tasks

**Output:**
- Production-ready Watch Party UX

**Files:**
- Any file from TASK-001..005 — **modify** as needed for polish

**Responsibilities:**
- Test flow A: Lobby → Picker → CreateDialog → Room
- Test flow B: MovieDetail → Lobby → CreateDialog → Room
- Test flow C: Join by code
- Test flow D: Empty state → CTA → Picker
- Fix visual alignment, spacing, color consistency
- Verify backend security fix works on real device

**Acceptance Criteria:**
- [ ] Flow A hoạt động end-to-end
- [ ] Flow B hoạt động end-to-end
- [ ] Flow C (join by code) không bị regression
- [ ] Empty state hiển thị đẹp
- [ ] No "Bạn không có quyền truy cập" error
- [ ] Visual consistency với app theme

---

# 9. Edge Cases

| # | Scenario | Expected Behavior | Handled In |
|---|----------|-------------------|------------|
| 1 | User chưa login, mở Lobby | Phòng công khai vẫn load OK (sau fix TASK-001). Join/Create yêu cầu login | TASK-001 |
| 2 | Search trả 0 kết quả | Hiện "Không tìm thấy phim phù hợp" + suggest thử từ khóa khác | TASK-004 |
| 3 | Network error khi search | Hiện error state trong picker, cho retry | TASK-004 |
| 4 | User gõ search rất nhanh | Chỉ trigger API call cuối cùng (debounce 300ms) | TASK-002 |
| 5 | Movie poster null | Fallback sang `thumbUrl`, nếu cũng null → placeholder gray box | TASK-004 |
| 6 | MovieDetail → Lobby, movie title chứa special chars | URL encode params đúng | TASK-005 |
| 7 | User back từ CreateDialog | Quay lại Lobby bình thường, không navigate lại MovieDetail | TASK-005 |
| 8 | Tên phòng trống khi submit | Disable nút "Tạo" hoặc show validation error | TASK-004 |
| 9 | Token expired giữa chừng (load lobby OK, nhưng create fail) | Show error dialog rõ ràng, gợi ý đăng nhập lại | TASK-004 |

---

# 10. Risks

| # | Risk | Impact | Likelihood | Mitigation |
|---|------|--------|------------|------------|
| 1 | `MovieRepository` inject vào `WatchPartyViewModel` gây circular DI | Med | Low | Hilt xử lý tốt vì cả hai đều `@Singleton` inject qua constructor |
| 2 | BottomSheet full-screen bị overlap keyboard khi gõ search | Med | Med | Dùng `imePadding()` modifier trên search TextField |
| 3 | URL encoding movie params bị decode sai | Low | Med | Dùng `Uri.encode()` / `Uri.decode()` chuẩn Android |
| 4 | Popular movies API trả empty (server vừa deploy) | Low | Low | Fallback sang latest movies nếu popular empty |

---

# 11. Verification Plan

**Manual Smoke Tests:**

| Scenario | Steps | Expected |
|----------|-------|----------|
| A. Tạo phòng từ Lobby | 1. Mở Watch Party tab → 2. Tap FAB → 3. Search phim → 4. Chọn phim → 5. Nhập tên phòng → 6. Tap "Tạo" | Phòng được tạo, navigate vào room |
| B. Tạo phòng từ MovieDetail | 1. Vào chi tiết phim → 2. Tap "Xem cùng bạn bè" → 3. Nhập tên phòng → 4. Tap "Tạo" | Phòng được tạo với đúng phim đã chọn |
| C. Join bằng mã | 1. Mở Lobby → 2. Nhập mã phòng → 3. Tap "Vào" | Join phòng thành công |
| D. Empty state | 1. Mở Lobby khi không có phòng nào | Hiện empty state đẹp với CTA |
| E. No auth error | 1. Đảm bảo token valid → 2. Mở Lobby | Không hiện "Bạn không có quyền truy cập" |

**Success Criteria:**
- All 5 smoke test scenarios pass
- Feature matches spec behavior
- No regressions in existing Watch Party functionality
- Visual consistency with CineStream app theme

---

# 12. Rollout Plan

| Phase | Scope | Gate Criteria |
|-------|-------|---------------|
| 1 | Backend security fix | Deploy backend, verify GET /public works without token |
| 2 | Android UX changes | Build APK, test on real device |
| 3 | Full integration | Test end-to-end on real device with backend |

---

# 13. Future Improvements

- **Episode picker**: Cho phép chọn tập cụ thể khi tạo phòng (hiện chỉ chọn phim)
- **Deep link invite**: Gửi link mời bạn bè → mở app đúng phòng
- **Recently watched movies**: Hiện phim đã xem gần đây trong picker (nhanh hơn search)
- **Room search**: Tìm kiếm phòng theo tên (không chỉ mã phòng)
