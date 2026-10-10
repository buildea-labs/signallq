import { defineConfig } from 'vitest/config'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(dirname, './src'),
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    exclude: [
      '**/node_modules/**',
      // Playwright E2E directories (top-level and nested worktree copy)
      'e2e/**',
      'e2e-ad-slot/**',
      'e2e-visual/**',
      'signallq-web/e2e/**',
      'signallq-web/e2e-ad-slot/**',
      'signallq-web/e2e-visual/**',
      // Any Playwright spec file regardless of location
      '**/*.spec.ts',
      // Git worktree / shared workspace copies
      'worktree/**',
      'signallq-web/**',
    ],
  },
})
