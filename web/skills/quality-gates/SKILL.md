---
name: quality-gates
description: Run SignallQ Web installation, lint, TypeScript, tests, and production build checks.
---

# Quality gates

Run in order: `npm ci`, `npm run lint`, `npm run typecheck`, `npm test`, and `npm run build`. Report commands, result and warnings. Do not claim success when a gate is skipped.
