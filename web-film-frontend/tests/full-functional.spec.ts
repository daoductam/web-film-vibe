import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';

const viewer = { id: 9, username: 'tester', fullName: 'Test Viewer', email: 'test@example.test', role: 'USER', avatarUrl: '' };
const film = { id: 11, slug: 'functional-film', title: 'Functional Film', originTitle: 'Original Film', year: 2026, thumbUrl: '', posterUrl: '', categories: [{ id: 1, slug: 'action', name: 'Hành động' }], countries: [], description: 'Movie description', casts: 'Actor One', director: 'Director', quality: 'HD', type: 'series', duration: '120 phút', averageRating: 4, viewCount: 100, currentEpisode: '2', servers: [{ serverName: 'Test server', episodes: [{ id: 21, name: 'Tập 1', slug: 'ep-1', serverName: 'Test server', linkM3u8: 'https://media.example.test/movie.m3u8', linkEmbed: '' }] }] };
const room = { id: 5, code: 'TESTROOM', name: 'Test room', host: viewer, movie: film, episode: { id: 21, name: 'Tập 1', slug: 'ep-1' }, roomType: 'PUBLIC', status: 'WAITING', maxMembers: 10, currentMemberCount: 1 };
const comment = { id: 4, username: viewer.username, fullName: viewer.fullName, content: 'Existing comment', createdAt: '2026-09-10T00:00:00Z', replies: [], likeCount: 0, isLiked: false, episodeName: 'Tập 1' };

async function setup(page: Page, role: 'GUEST' | 'USER' | 'ADMIN' = 'USER') {
    const errors: string[] = [];
    page.on('pageerror', error => errors.push(error.message));
    if (role !== 'GUEST') await page.addInitScript(user => localStorage.setItem('auth-storage', JSON.stringify({ state: { token: 'test-token', refreshToken: 'test-refresh', user }, version: 0 })), { ...viewer, role });
    const requests: { path: string; method: string; body: Record<string, unknown>; search: string }[] = [];
    let favorites: unknown[] = [];
    const paged = (content: unknown[]) => ({ content, totalElements: content.length, totalPages: 1, pageNumber: 0, last: true, page: { number: 0, totalPages: 1, totalElements: content.length } });
    await page.route('**/api/**', async route => {
        const req = route.request();
        const url = new URL(req.url());
        const path = url.pathname;
        const body = req.postData() && req.headers()['content-type']?.includes('application/json') ? req.postDataJSON() : {};
        requests.push({ path, method: req.method(), body, search: url.search });
        let data: unknown = [];
        if (path.endsWith('/auth/login') || path.endsWith('/auth/register')) data = { user: viewer, accessToken: 'new-token', refreshToken: 'new-refresh' };
        else if (path.endsWith('/users/me/avatar')) data = { ...viewer, avatarUrl: 'https://example.test/avatar.png' };
        else if (path.endsWith('/users/me')) data = { ...viewer, ...body };
        else if (path.endsWith('/favorites') && req.method() === 'POST') { favorites = [body]; data = body; }
        else if (path.includes('/favorites/') && req.method() === 'DELETE') { favorites = []; }
        else if (path.endsWith('/favorites')) data = favorites;
        else if (path.endsWith('/users/me/history')) data = [];
        else if (path.endsWith('/categories')) data = film.categories;
        else if (path.endsWith('/countries')) data = [{ id: 2, slug: 'vn', name: 'Việt Nam' }];
        else if (path.endsWith('/movies/functional-film')) data = film;
        else if (path.includes('/movies/')) data = paged([film]);
        else if (path.endsWith('/graphql')) return route.fulfill({ json: { data: { personalizedRecommendations: [film], similarMovies: [] } } });
        else if (path.endsWith('/ratings')) data = null;
        else if (path.includes('/ratings/')) data = { averageRating: 4, totalRatings: 2, userRating: 4 };
        else if (path.endsWith('/comments') && req.method() === 'POST') data = { ...comment, ...body, id: 7 };
        else if (path.endsWith('/like')) data = true;
        else if (path.includes('/comments/')) data = paged([comment]);
        else if (path.endsWith('/notifications/unread-count')) data = { unreadCount: 0 };
        else if (path.endsWith('/notifications')) data = paged([]);
        else if (path.endsWith('/ai/chat')) data = { aiMessage: 'Test AI response', isMovieQuery: true, movies: paged([film]) };
        else if (path.endsWith('/watch-rooms/public')) data = paged([room]);
        else if (path.endsWith('/watch-rooms/history')) data = paged([{ id: 8, roomName: room.name, movieTitle: film.title, movieSlug: film.slug, watchDurationSeconds: 300, joinedAt: '2026-09-10T00:00:00Z' }]);
        else if (path.endsWith('/join')) data = { role: 'HOST' };
        else if (path.endsWith('/state')) data = { currentTime: 0, isPlaying: false, playbackRate: 1 };
        else if (path.endsWith('/messages')) data = paged([]);
        else if (path.includes('/watch-rooms')) data = room;
        await route.fulfill({ json: { success: true, data } });
    });
    return { requests, errors };
}

for (const path of ['/', '/movies', '/series', '/popular', '/search?q=film', '/movie/functional-film', '/profile?tab=favorites', '/profile?tab=history', '/profile?tab=party-history', '/profile?tab=settings', '/notifications', '/watch-party']) {
    test(`renders ${path} without runtime errors on desktop and mobile`, async ({ page }) => {
        const { errors } = await setup(page);
        await page.goto(path);
        await expect(page.locator('main')).toBeVisible();
        await expect(page.locator('main')).not.toBeEmpty();
        await page.setViewportSize({ width: 390, height: 844 });
        expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBeTruthy();
        expect(errors).toEqual([]);
    });
}

test('login submits credentials and logout clears authentication', async ({ page }) => {
    const { requests } = await setup(page, 'GUEST');
    await page.goto('/login');
    await page.locator('#email').fill('test@example.test');
    await page.locator('#password').fill('password123');
    await page.locator('button[type=submit]').click();
    await expect(page).toHaveURL('/');
    expect(requests.find(req => req.path.endsWith('/auth/login'))?.body).toEqual({ username: 'test@example.test', password: 'password123' });
    await page.locator('header button').filter({ has: page.locator('svg.lucide-user') }).click();
    await page.getByRole('button', { name: 'Đăng xuất' }).click();
    await expect.poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('auth-storage') || '{}').state.token)).toBeNull();
});

test('registration submits backend fields', async ({ page }) => {
    const { requests } = await setup(page, 'GUEST');
    await page.goto('/register');
    await page.locator('#name').fill('Test Viewer');
    await page.locator('#email').fill('test@example.test');
    await page.locator('#password').fill('password123');
    await page.locator('button[type=submit]').click();
    await expect.poll(() => requests.some(req => req.path.endsWith('/auth/register'))).toBeTruthy();
    expect(requests.find(req => req.path.endsWith('/auth/register'))?.body.email).toBe('test@example.test');
});

test('login API failure displays feedback', async ({ page }) => {
    await setup(page, 'GUEST');
    await page.route('**/auth/login', route => route.fulfill({ status: 401, json: { success: false, message: 'Wrong test password' } }));
    await page.goto('/login');
    await page.locator('#email').fill('test@example.test');
    await page.locator('#password').fill('wrong');
    await page.locator('button[type=submit]').click();
    await expect(page.getByText('Wrong test password')).toBeVisible();
});

test('catalog filter sends category and search sends query', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/movies');
    await page.getByRole('button', { name: 'Bộ lọc', exact: true }).click();
    await page.locator('select').first().selectOption('action');
    await page.getByRole('button', { name: /Áp dụng/ }).click();
    await expect.poll(() => requests.some(req => req.search.includes('category=action'))).toBeTruthy();
    await page.goto('/search?q=Functional');
    await expect(page.locator('main').getByText(film.title).first()).toBeVisible();
    expect(requests.some(req => req.path.endsWith('/movies/search') && req.search.includes('q=Functional'))).toBeTruthy();
});

test('favorite add then remove updates profile library', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/movie/functional-film');
    await page.getByRole('button', { name: 'Yêu thích', exact: true }).click();
    await expect(page.getByRole('button', { name: 'Đã yêu thích', exact: true })).toBeVisible();
    await page.goto('/profile?tab=favorites');
    await page.getByRole('button', { name: `Bỏ yêu thích ${film.title}` }).click();
    await expect.poll(() => requests.some(req => req.method === 'DELETE' && req.path.endsWith('/favorites/functional-film'))).toBeTruthy();
    await expect(page.locator('main').getByText(film.title)).toHaveCount(0);
});

test('profile update and avatar upload use correct endpoints', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/profile?tab=settings');
    await page.getByLabel('Tên hiển thị').fill('Updated Viewer');
    await page.getByRole('button', { name: 'Lưu thay đổi' }).click();
    await expect(page.getByText('Cập nhật thông tin thành công!')).toBeVisible();
    expect(requests.find(req => req.method === 'PUT' && req.path.endsWith('/users/me'))?.body.fullName).toBe('Updated Viewer');
    await page.locator('#avatar-upload').setInputFiles({ name: 'avatar.png', mimeType: 'image/png', buffer: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aN3sAAAAASUVORK5CYII=', 'base64') });
    await expect(page.getByText('Cập nhật ảnh đại diện thành công!')).toBeVisible();
});

test('movie comments and replies submit episode and parent identifiers', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/movie/functional-film');
    await page.locator('textarea').first().fill('New test comment');
    await page.locator('form').filter({ has: page.locator('textarea') }).first().locator('button[type=submit]').click();
    await expect(page.getByText('New test comment', { exact: true })).toBeVisible();
    expect(requests.find(req => req.path.endsWith('/comments') && req.body.content === 'New test comment')?.body.episodeSlug).toBe('ep-1');
    await page.getByRole('button', { name: 'Phản hồi', exact: true }).last().click();
    await page.locator('textarea').last().fill('Test reply');
    await page.locator('form').filter({ has: page.locator('textarea') }).last().locator('button[type=submit]').click();
    await expect.poll(() => requests.some(req => req.body.content === 'Test reply' && req.body.parentId === 4)).toBeTruthy();
});

test('AI assistant sends query and displays returned movie', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/');
    await page.getByRole('button', { name: 'Cine-chan', exact: true }).click();
    const field = page.locator('input').filter({ hasNot: page.locator('[type=checkbox]') });
    await field.last().fill('Find a movie');
    await field.last().press('Enter');
    await expect(page.getByText('Test AI response')).toBeVisible();
    expect(requests.find(req => req.path.endsWith('/ai/chat'))?.body.query).toBe('Find a movie');
});

test('create watch party with preselected movie and episode', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/watch-party?movie=functional-film');
    await expect(page.locator('#party-episode')).toHaveValue('21');
    await page.getByRole('button', { name: 'Tạo phòng', exact: true }).click();
    await expect(page).toHaveURL(/watch-party\/room\/5/);
    expect(requests.find(req => req.method === 'POST' && req.path.endsWith('/watch-rooms'))?.body.movieId).toBe(11);
    await expect.poll(() => requests.some(req => req.path.endsWith('/5/join'))).toBeTruthy();
});

test('join room by code normalizes case', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/watch-party');
    await page.getByPlaceholder(/Mã phòng/).fill('testroom');
    await page.getByRole('button', { name: 'Tham gia', exact: true }).click();
    await expect(page).toHaveURL(/watch-party\/room\/5/);
    expect(requests.some(req => req.path.endsWith('/code/TESTROOM'))).toBeTruthy();
});

test('admin routes reject a regular viewer', async ({ page }) => {
    await setup(page);
    await page.goto('/admin/crawl');
    await expect(page).toHaveURL('/');
});

test('admin dashboard and crawler render for administrator', async ({ page }) => {
    const { errors } = await setup(page, 'ADMIN');
    await page.goto('/admin');
    await expect(page.getByText('Tổng số phim')).toBeVisible();
    await page.getByRole('link', { name: 'Crawl Dữ liệu' }).click();
    await expect(page.locator('main')).toContainText('Crawl');
    expect(errors).toEqual([]);
});

for (const path of ['/admin/movies', '/admin/users']) {
    test(`admin navigation target ${path} has a working page`, async ({ page }) => {
        await setup(page, 'ADMIN');
        await page.goto(path);
        await expect(page.locator('main')).toBeVisible();
    });
}

test('rating and comment like call the social API', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/movie/functional-film');
    await page.locator('button').filter({ has: page.locator('svg.lucide-star') }).last().click();
    await expect.poll(() => requests.find(req => req.path.endsWith('/ratings') && req.method === 'POST')?.body.score).toBe(5);
    await page.locator('button').filter({ has: page.locator('svg.lucide-thumbs-up') }).click();
    await expect.poll(() => requests.some(req => req.path.endsWith('/comments/4/like'))).toBeTruthy();
});

test('comment deletion confirms and removes the comment', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/movie/functional-film');
    page.on('dialog', dialog => dialog.accept());
    await page.locator('button').filter({ has: page.locator('svg.lucide-trash-2') }).click();
    await expect.poll(() => requests.some(req => req.path.endsWith('/comments/4') && req.method === 'DELETE')).toBeTruthy();
    await expect(page.getByText('Existing comment', { exact: true })).toHaveCount(0);
});

test('avatar rejects a non-image before upload', async ({ page }) => {
    const { requests } = await setup(page);
    await page.goto('/profile?tab=settings');
    await page.locator('#avatar-upload').setInputFiles({ name: 'document.txt', mimeType: 'text/plain', buffer: Buffer.from('test') });
    await expect(page.getByText('Chọn ảnh có dung lượng tối đa 5 MB.')).toBeVisible();
    expect(requests.some(req => req.path.endsWith('/avatar'))).toBeFalsy();
});

test('AI API failure displays a recoverable error message', async ({ page }) => {
    await setup(page);
    await page.route('**/ai/chat', route => route.fulfill({ status: 500, json: { success: false } }));
    await page.goto('/');
    await page.getByRole('button', { name: 'Cine-chan', exact: true }).click();
    await page.getByPlaceholder(/Hãy mô tả phim/).fill('Find a movie');
    await page.getByPlaceholder(/Hãy mô tả phim/).press('Enter');
    await expect(page.getByText(/hệ thống của Cine-chan đang gặp chút sự cố/)).toBeVisible();
});

test('notification error can be retried successfully', async ({ page }) => {
    await setup(page);
    let available = false;
    await page.route('**/api/v1/notifications?*', route => route.fulfill(available
        ? { json: { success: true, data: { content: [], totalPages: 0 } } }
        : { status: 400, json: { success: false } }));
    await page.goto('/notifications');
    await expect(page.getByText('Không thể tải thông báo.')).toBeVisible({ timeout: 15000 });
    available = true;
    await page.getByRole('button', { name: 'Thử lại', exact: true }).click();
    await expect(page.getByText('Chưa có thông báo nào.')).toBeVisible();
});

test('invalid watch-party code displays feedback without navigation', async ({ page }) => {
    await setup(page);
    await page.route('**/watch-rooms/code/**', route => route.fulfill({ status: 404, json: { success: false } }));
    await page.goto('/watch-party');
    await page.getByPlaceholder(/Mã phòng/).fill('invalid');
    await page.getByRole('button', { name: 'Tham gia', exact: true }).click();
    await expect(page.getByText('Không tìm thấy phòng phù hợp hoặc phòng đã đầy')).toBeVisible();
    await expect(page).toHaveURL('/watch-party');
});

test('share movie copies the current movie link', async ({ page }) => {
    await setup(page);
    await page.addInitScript(() => {
        Object.defineProperty(navigator, 'share', { value: undefined, configurable: true });
        Object.defineProperty(navigator, 'clipboard', { value: { writeText: async (value: string) => { sessionStorage.setItem('shared-link', value); } }, configurable: true });
    });
    await page.goto('/movie/functional-film');
    await page.getByRole('button', { name: 'Chia sẻ', exact: true }).click();
    await expect.poll(() => page.evaluate(() => sessionStorage.getItem('shared-link'))).toContain('/movie/functional-film');
});
