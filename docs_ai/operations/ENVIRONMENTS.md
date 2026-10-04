---
title: "Ambientes"
description: "Ambientes locais, de CI e de backend do app Android e dos 5 Workers Cloudflare."
type: "técnico"
status: "ativo"
owner: "Camillo"
last_updated: "2026-10-04"
version: "2.0.0"
---

# Ambientes

- **Fonte de verdade:** `android/gradle/libs.versions.toml` (build), `integrations/cloudflare/*/wrangler.toml` (Workers)
- **Escopo:** ambientes locais/CI do app Android e dos 5 Workers Cloudflare

## Não há staging

O app **não** tem backend de staging: todos os builds, de qualquer trilha da Play Console (`beta`, `production`) ou do Firebase App Distribution, usam os **mesmos** Workers de produção. Não há product flavor nem endpoint alternativo por ambiente; `build.gradle.kts` só distingue `debug`/`release` (assinatura e feature flags). A validação pré-release é feita com build local e teste manual.

## Local

Android Studio, Gradle Wrapper, Android SDK (`local.properties`), emulador ou device. Assinatura local: `android/key.properties` + `android/segredos/signallq.jks` (ver `SIGNING.md`). Scripts: `SCRIPTS.md`.

## CI

GitHub Actions, JDK 17 (Temurin); ver `ci-cd.md`. Release e trilhas: `RELEASE.md`.

## Cloudflare Workers (`integrations/cloudflare/`)

| Pasta | `name` no `wrangler.toml` | Função |
|---|---|---|
| `ai-diagnosis-worker` | `linka-ai-diagnosis-worker` | Diagnóstico por IA |
| `signallq-admin-worker` | `signallq-admin` | Backend do Console/Admin (D1 `signallq-admin-db`) |
| `signallq-diagnostic-worker` | `signallq-diagnostic` | Diagnóstico/telemetria (D1 `signallq-diagnostic-db`) |
| `signallq-privacy-worker` | `signallq-privacy` | Política de privacidade e termos (público) |
| `game-latency-probe-worker` | `signallq-game-latency-probe` | Probe de latência para jogos |

Cada Worker tem seu `wrangler.toml`, bindings e secrets; não há arquivo único. Modelos e fallbacks de IA: `docs_ai/TECNICO.md` e `docs_ai/CONTRATOS/`.

## Riscos

- Sem staging, mudança em Worker afeta todos os usuários imediatamente; valide antes de `npx wrangler deploy`.
- Keystore e `key.properties` são geridos manualmente fora do git (`SIGNING.md`).
