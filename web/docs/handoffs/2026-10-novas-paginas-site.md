# Handoff — novas páginas do site (protótipos de 2026-10-10)

Fonte: `SignallQ - Prototipos.zip` (Claude Design, local do Luiz, não versionado). Os `.dc.html` foram usados como referência de conteúdo, copy e fluxo; o código usa o design system do repo (tokens, `Banda`, `InstitutionalFoundation`).

## Decisões do Luiz nesta rodada

- Reintroduzir o teste de velocidade (reverte o corte de 2026-08-27).
- `/privacidade` e `/termos`: manter o texto atual auditado; sem importar o texto do protótipo.
- `speedtest.signallq.com` deve servir o teste de velocidade.
- Tela exemplo do hero: captura real enviada por ele.

## Protótipo → código

| Protótipo | Código |
|---|---|
| `SignallQ Home.dc.html` | `AppLandingClient.tsx` + `HowItWorksSteps`, `ResultOrder`, `TrustPromises`, `LandingFaq`; galeria existente mantida |
| `SiteHeader.dc.html` | `SiteNav.tsx` (links + `aria-current` via `usePathname`) |
| `SiteFooter.dc.html` | `SiteFooter.tsx` — já equivalente, sem mudança |
| `SignallQ Como Funciona.dc.html` | `src/app/como-funciona/` |
| `SignallQ Duvidas.dc.html` | `src/app/duvidas/` (JSON-LD `FAQPage`) |
| `SignallQ Guias.dc.html` + `GuideBody.dc.html` + 4 guias | `src/app/guias/` (`guides-data.ts`, `[slug]`, JSON-LD `Article`) |
| `SignallQ Teste.dc.html` | `src/app/teste-de-velocidade/` — ver `docs/architecture/speed-test-flow.md` |
| `SignallQ Privacidade/Termos.dc.html` | não importados (decisão acima) |
| `SignallQ Android.dc.html`, `Speed Flow.dc.html` | fora do escopo (referência do app / catálogo interno) |
| `seo/google-play-listing.md` | fora do escopo (ficha da Play Store) |

## Divergências deliberadas

- OG image/favicon do protótipo (`og-image.png`, `favicon.png`) não existem em `public/`; mantido o default real (`signallq-symbol.png`).
- Ilustrações PNG do zip não importadas; o repo já tem SVG tokenizado com dark mode.
- Sitemap segue a convenção local (`changefreq`/`priority`), não `lastmod`.
- `TelemetryInit` passou a chamar `initTelemetryDeferred()`, que existia mas nunca era chamado: sem isso o site não emitia `session_start`, e o informe de visitas não teria dados.
- `AppLandingClient.test.tsx` estava quebrado em `main` desde `afd3d3a` (esperava galeria de 8 telas e 2 CTAs; o código tem 4 e 1). Alinhado ao código real.

## Pendências

| Dono | Item |
|---|---|
| Luiz | DNS + domínio `speedtest.signallq.com` no projeto Vercel (o rewrite já está pronto) |
| Luiz | Captura do hero mostra SSID "Luiz-5G", operadora e modelo do aparelho; confirmar se publica assim |
| Caio | Revisão independente (rotas públicas novas, chamada externa do cliente a `speed.cloudflare.com`) |
| Renan | Indexação das 10 rotas novas; `middleware.ts` usa convenção depreciada no Next 16 (`proxy`), dívida pré-existente |
| Luiz | Deploy de produção (manual, `docs/deploy-vercel.md`) |
| — | Dívida pré-existente: `README.md` e `.env.example` ainda descrevem o produto anterior (AdSense, planos, worker de diagnóstico) |
| — | Informe diário de visitas no Discord: implementação no `signallq-admin-worker` (repo `signallq`), ainda não feita |
