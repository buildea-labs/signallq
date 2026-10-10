# Speed Test Flow

Reintroduzido em 2026-10-10 (decisão do Luiz, revertendo a de 2026-08-27). A versão anterior — controller, jornada na Home, histórico, lock entre abas, baseline visual — foi removida junto com o resto do produto de medição e **não existe mais**; recuperável via `git show 311a899^:docs/architecture/speed-test-flow.md`. A implementação atual é deliberadamente mínima.

## Responsabilidades

Tudo em `src/app/teste-de-velocidade/`:

- `page.tsx`: só metadata (`PAGE_META['/teste-de-velocidade']`) e render.
- `SpeedTestContent.tsx`: orquestrador client; escolhe qual estado renderizar, dispara a telemetria de início e injeta o JSON-LD `WebApplication`.
- `useSpeedTestEngine.ts`: única peça que toca a rede. Máquina de estados `idle → running (ping → down → up) → done | error`, com cancelamento por `runId`.
- `speedTestVerdict.ts`: funções puras (veredito, níveis de uso, formatação). Sem React nem rede; coberto por `speedTestVerdict.test.ts`.
- `SpeedTestIdle|Running|Result|Error.tsx`: uma peça de UI por estado.

## Fluxo De Dados

O navegador chama direto `SPEEDTEST_DOWNLOAD_URL` / `SPEEDTEST_UPLOAD_URL` (`src/lib/config.ts`; default `speed.cloudflare.com/__down|__up`, sobrescritíveis por `NEXT_PUBLIC_SPEEDTEST_*`). Não há segredo envolvido nem proxy server-side. O projeto não define CSP/`connect-src`. Nada é persistido; o resultado vive só no estado do hook.

Telemetria: `trackFeatureUsed('teste_velocidade_iniciado')` (evento `feature_used` já whitelistado no admin-worker).

## Domínio speedtest.signallq.com

`src/middleware.ts` faz **rewrite** (não redirect) de qualquer caminho em `speedtest.signallq.com` para `/teste-de-velocidade`. O canonical continua `https://signallq.com/teste-de-velocidade` (absoluto, via `routeMetadata`), então o subdomínio não gera conteúdo duplicado. Registrar DNS e domínio no projeto Vercel é passo manual de infraestrutura.

## Não Duplicar

Sem segunda árvore mobile/desktop, sem store global, sem persistência local, sem proxy para o motor. Se precisar de histórico ou comparação, é decisão de escopo nova — o AGENTS.md exige aprovação do Luiz.
