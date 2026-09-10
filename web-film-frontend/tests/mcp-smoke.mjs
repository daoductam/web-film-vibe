import { createRequire } from 'node:module';
import { pathToFileURL } from 'node:url';
import { resolve, join } from 'node:path';
import { mkdirSync, writeFileSync } from 'node:fs';

// Usage: node tests/mcp-smoke.mjs <npm root -g> [frontend URL]
const globalRoot = process.argv[2];
if (!globalRoot) throw new Error('Pass the directory printed by npm root -g');
const packageRoot = join(globalRoot, '@executeautomation/playwright-mcp-server');
const requireMcp = createRequire(join(packageRoot, 'package.json'));
const { Client } = await import(pathToFileURL(requireMcp.resolve('@modelcontextprotocol/sdk/client/index.js')).href);
const { StdioClientTransport } = await import(pathToFileURL(requireMcp.resolve('@modelcontextprotocol/sdk/client/stdio.js')).href);
const output = resolve('artifacts/debug-fe/mcp');
mkdirSync(output, { recursive: true });
const client = new Client({ name: 'cinestream-functional-audit', version: '1.0.0' });
const transport = new StdioClientTransport({ command: process.execPath, args: [join(packageRoot, 'dist/index.js')], stderr: 'pipe' });
const results = [];
try {
    await client.connect(transport);
    const available = await client.listTools();
    writeFileSync(join(output, 'tools.json'), JSON.stringify(available, null, 2));
    const base = process.argv[3] || 'http://127.0.0.1:5173';
    for (const route of ['/login', '/register', '/search', '/watch-party', '/']) {
        const navigation = await client.callTool({ name: 'playwright_navigate', arguments: { url: `${base}${route}`, browserType: 'firefox', headless: true, width: 1440, height: 900, waitUntil: 'networkidle', timeout: 20000 } });
        const snapshot = await client.callTool({ name: 'playwright_get_visible_text', arguments: {} });
        results.push({ route, navigation, snapshot });
        await client.callTool({ name: 'playwright_screenshot', arguments: { name: route.replaceAll('/', '') || 'home', savePng: true, storeBase64: false, downloadsDir: output, fullPage: true } });
        console.log(`MCP inspected ${route}: ${navigation.isError ? 'ERROR' : 'OK'}`);
    }
    results.push({ console: await client.callTool({ name: 'playwright_console_logs', arguments: { type: 'error', limit: 30 } }) });
    writeFileSync(join(output, 'results.json'), JSON.stringify(results, null, 2));
} finally {
    await client.close();
}
