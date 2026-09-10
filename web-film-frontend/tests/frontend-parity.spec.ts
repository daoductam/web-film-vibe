import { test, expect } from '@playwright/test';
import type { Page } from '@playwright/test';

const user = { id: 1, username: 'viewer', fullName: 'Test Viewer', email: 'viewer@example.test', role: 'USER', avatarUrl: '' };
const episode = (id: number) => ({ id, slug: `tap-${id}`, name: `Tập ${id}`, serverName: 'Server 1', linkM3u8: `https://media.example.test/${id}.m3u8`, linkEmbed: '' });
const movie = { id: 1, slug: 'test-film', title: 'Test Film', categories: [], countries: [], description: '<p>Test description</p>', servers: [{ serverName: 'Server 1', episodes: [episode(1), episode(2)] }] };
const history = { movieSlug: movie.slug, title: movie.title, lastEpisodeSlug: 'tap-1', lastEpisodeName: 'Tập 1', progressMs: 30000, durationMs: 120000, updatedAt: '2026-09-09T10:00:00' };

async function mockApi(page: Page, loggedIn = true) {
    page.on('pageerror', error => console.error('Browser error:', error.message));
    if (loggedIn) await page.addInitScript(value => localStorage.setItem('auth-storage', JSON.stringify({ state: { token: 'test-token', refreshToken: 'test-refresh', user: value }, version: 0 })), user);
    const writes: { path: string; body: Record<string, unknown> }[] = [];
    let read = false;
    await page.route('**/api/**', async route => {
        const request = route.request();
        const path = new URL(request.url()).pathname;
        let data: unknown = [];
        if (request.method() !== 'GET' && request.method() !== 'OPTIONS') {
            writes.push({ path, body: request.postData() ? request.postDataJSON() : {} });
            if (path.includes('/read')) read = true;
        }
        if (path.endsWith('/notifications/unread-count')) data = { unreadCount: read ? 0 : 1 };
        else if (path.endsWith('/notifications')) data = { content: [{ id: 1, title: 'Tập mới', content: 'Đã có tập mới', isRead: read, movieSlug: movie.slug, createdAt: '2026-09-09T10:00:00' }], totalPages: 1, last: true };
        else if (path.endsWith('/users/me/history')) data = request.method() === 'GET' ? [history] : request.postDataJSON();
        else if (path.endsWith('/movies/test-film')) data = movie;
        else if (path.includes('/ratings/')) data = { averageRating: 4, totalRatings: 1 };
        else if (path.endsWith('/users/me')) data = user;
        else if (path.endsWith('/graphql')) return route.fulfill({ json: { data: { similarMovies: [], personalizedRecommendations: [] } } });
        else if (path.includes('/comments/') || path.includes('/movies/') || path.endsWith('/watch-rooms/history')) data = { content: [], page: { number: 0, totalPages: 0, totalElements: 0 } };
        await route.fulfill({ json: { success: true, data } });
    });
    return writes;
}

test('notifications mark all read and navigate to the movie', async ({ page }) => {
    const writes = await mockApi(page);
    await page.goto('/notifications');
    await expect(page.getByText('Đã có tập mới')).toBeVisible();
    await page.getByRole('button', { name: /Đọc tất cả|Đánh dấu tất cả/ }).click();
    await expect.poll(() => writes.some(item => item.path.endsWith('/read-all'))).toBeTruthy();
    await page.getByRole('button', { name: /Tập mới/ }).click();
    await expect(page).toHaveURL(/movie\/test-film/);
});

test('password confirmation prevents request; valid form uses backend contract on mobile', async ({ page }) => {
    const writes = await mockApi(page);
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/profile?tab=settings');
    await page.getByLabel('Mật khẩu hiện tại', { exact: true }).fill('old-password');
    await page.getByLabel('Mật khẩu mới', { exact: true }).fill('new-password');
    await page.getByLabel('Xác nhận mật khẩu mới').fill('mismatch');
    await page.getByRole('button', { name: 'Đổi mật khẩu', exact: true }).click();
    await expect(page.getByText('Mật khẩu xác nhận không khớp.')).toBeVisible();
    expect(writes.some(item => item.path.endsWith('/password'))).toBeFalsy();
    await page.getByLabel('Xác nhận mật khẩu mới').fill('new-password');
    await page.getByRole('button', { name: 'Đổi mật khẩu', exact: true }).click();
    await expect(page.getByText('Đổi mật khẩu thành công.')).toBeVisible();
    expect(writes.find(item => item.path.endsWith('/password'))?.body).toEqual({ currentPassword: 'old-password', newPassword: 'new-password', confirmPassword: 'new-password' });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBeTruthy();
});

test('player restores history, saves milliseconds and advances episodes', async ({ page }) => {
    const writes = await mockApi(page);
    // Test media events deterministically; real HLS transport requires a reachable stream.
    await page.addInitScript(() => { HTMLMediaElement.prototype.canPlayType = () => 'probably'; });
    await page.route('https://media.example.test/**', route => route.fulfill({ status: 200, contentType: 'application/vnd.apple.mpegurl', body: '#EXTM3U\n#EXT-X-TARGETDURATION:6\n#EXT-X-ENDLIST' }));
    await page.goto('/movie/test-film?episode=tap-1');
    await page.getByRole('button', { name: 'Tiếp tục xem phim', exact: true }).click();
    const video = page.locator('video');
    await expect(video).toHaveAttribute('title', 'Test Film - Tập 1');
    const restored = await video.evaluate(element => {
        Object.defineProperty(element, 'duration', { configurable: true, value: 120 });
        Object.defineProperty(element, 'currentTime', { configurable: true, writable: true, value: 0 });
        element.dispatchEvent(new Event('loadedmetadata'));
        return (element as HTMLVideoElement).currentTime;
    });
    expect(restored).toBe(30);
    await video.evaluate(element => { (element as HTMLVideoElement).currentTime = 45; element.dispatchEvent(new Event('pause')); });
    await expect.poll(() => writes.find(item => item.body.progressMs === 45000)?.body.lastEpisodeSlug).toBe('tap-1');
    await video.evaluate(element => element.dispatchEvent(new Event('ended')));
    await expect(video).toHaveAttribute('title', 'Test Film - Tập 2');
    await expect(page).toHaveURL(/episode=tap-2/);
});

test('guest profile redirects to login', async ({ page }) => {
    await mockApi(page, false);
    await page.goto('/profile');
    await expect(page).toHaveURL(/\/login$/);
});

test('concurrent expired requests share one refresh and both retry', async ({ page }) => {
    await mockApi(page);
    let refreshes = 0;
    await page.route('**/api/v1/auth/refresh', async route => {
        refreshes++;
        await new Promise(resolve => setTimeout(resolve, 100));
        await route.fulfill({ json: { success: true, data: { accessToken: 'renewed-token', refreshToken: 'renewed-refresh' } } });
    });
    await page.route('**/api/v1/refresh-test*', route => route.fulfill({
        status: route.request().headers().authorization === 'Bearer renewed-token' ? 200 : 401,
        json: { success: true },
    }));
    await page.goto('/profile');
    const statuses = await page.evaluate(async () => {
        const path = '/src/services/api.ts';
        const { default: api } = await import(path);
        const results = await Promise.allSettled([api.get('/refresh-test?a'), api.get('/refresh-test?b')]);
        return results.map(result => result.status);
    });
    expect(statuses).toEqual(['fulfilled', 'fulfilled']);
    expect(refreshes).toBe(1);
});

test('failed refresh rejects every pending request instead of hanging', async ({ page }) => {
    await mockApi(page);
    await page.route('**/api/v1/auth/refresh', async route => {
        await new Promise(resolve => setTimeout(resolve, 100));
        await route.fulfill({ status: 401, json: { success: false } });
    });
    await page.route('**/api/v1/refresh-test*', route => route.fulfill({ status: 401, json: { success: false } }));
    await page.goto('/profile');
    const statuses = await page.evaluate(async () => {
        const path = '/src/services/api.ts';
        const { default: api } = await import(path);
        const results = await Promise.allSettled([api.get('/refresh-test?a'), api.get('/refresh-test?b')]);
        return results.map(result => result.status);
    });
    expect(statuses).toEqual(['rejected', 'rejected']);
    await expect(page).toHaveURL(/\/login$/);
});

test('effect replay shares membership and fast navigation still leaves after joining', async ({ page }) => {
    await mockApi(page);
    let joins = 0;
    let leaves = 0;
    await page.route('**/api/v1/watch-rooms/5/join', async route => {
        joins++;
        await new Promise(resolve => setTimeout(resolve, 100));
        await route.fulfill({ json: { success: true, data: { role: 'HOST' } } });
    });
    await page.route('**/api/v1/watch-rooms/5/leave', async route => {
        leaves++;
        await route.fulfill({ json: { success: true } });
    });
    await page.goto('/profile');
    await page.evaluate(async () => {
        const path = '/src/pages/watchparty/roomMembership.ts';
        const { acquireRoomMembership } = await import(path);
        const first = acquireRoomMembership(5, 1, 'test-token');
        first.release(false);
        const replay = acquireRoomMembership(5, 1, 'test-token');
        replay.release(false);
        await replay.joined;
    });
    await expect.poll(() => leaves).toBe(1);
    expect(joins).toBe(1);
});
