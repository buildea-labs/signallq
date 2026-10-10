# Auditoria PWA, SEO e monetização

Data da auditoria: 1º de agosto de 2026. Esta evidência cobre a parcela
versionável da issue #21; as validações que dependem da publicação em
`https://signallq.com` só podem ser concluídas depois da ativação do domínio
da issue #20.

## Identidade e instalação

- O `manifest.json` declara nome, nome curto, idioma, `start_url`, `scope`,
  modo standalone e ícones `192`, `512` e maskable.
- O manifest, favicon e Apple Touch Icon apontam diretamente para o asset
  oficial da marca Q em `public/assets/signallq-icon-512-play-store.png`.
  O arquivo-fonte tem 1024×1024 px, excedendo os tamanhos mínimos de 192×192 e
  512×512 exigidos pelos instaladores. O mesmo asset tem margem segura para o
  uso maskable; não há derivação ou redesenho da marca.
- `theme_color` e o viewport usam o fundo escuro oficial `#131217`.
- O Service Worker só ativa uma atualização após confirmação explícita da
  pessoa usuária; os ícones fazem parte do precache com hash da build, evitando
  que uma versão nova mantenha os assets antigos indefinidamente.
- A instalação usa o prompt nativo quando disponível e oferece orientação
  específica para iOS. Nenhum teste ou diagnóstico é simulado offline.

## SEO técnico

- `metadataBase`, canonicals, Open Graph, Twitter Cards, compartilhamento,
  `robots.txt` e `sitemap.xml` usam a origem canônica
  `https://signallq.com`.
- O redirecionamento permanente de `www.signallq.com` para o host canônico
  preserva caminho e query string. A publicação/validação desse
  redirecionamento permanece dependente da Vercel e do DNS.
- As rotas de histórico local declaram `noindex`; as demais rotas públicas
  usam a Metadata API com conteúdo correspondente.

## Publicidade e privacidade

- `ads.txt` é servido estaticamente e o script AdSense só é carregado quando
  há publisher ID público configurado e consentimento explícito.
- Recusar o consentimento impede o carregamento do script. A decisão é
  persistida localmente e a política explica como removê-la.
- Não há componente que crie slots, anúncios automáticos ou anúncios durante
  a medição, no diagnóstico, nos botões de ação ou em estados de erro.

## Validação pendente de publicação

1. Concluir a configuração DNS/TLS/redirects da issue #20 na Vercel, Hostinger
   e Cloudflare Pages.
2. Executar um deploy manual de produção e validar no domínio oficial:
   manifest, instalação, atualização, favicon, `ads.txt`, sitemap, robots e
   canonical por rota.
3. Inserir o token real de verificação no Search Console exclusivamente pela
   configuração apropriada e enviar o sitemap após a publicação. Nenhum token
   é versionado neste repositório.
4. Caso a marca oficial seja substituída, fornecer os arquivos finais em
   tamanhos adequados; não gerar ou reinterpretar uma marca nova a partir de
   imagens existentes.

> Nota (03/08/2026): o parágrafo de "Identidade e instalação" acima cita
> `public/assets/signallq-icon-512-play-store.png` como fonte do manifest,
> favicon e Apple Touch Icon — esse arquivo foi removido no commit `b7e8cae`
> (mesma issue #21). O manifest agora usa os ícones gerados `signallq-icon-
> {192,512}-{any,maskable}.png`; o favicon do `layout.tsx` usa
> `signallq-favicon-web-{light,dark}-bg.png`; o Apple Touch Icon usa
> `signallq-icon-512-play-store-dark.png`. Ver `docs/pwa-seo-monetization-audit.md`
> git history / commit `b7e8cae` para o detalhe — não reescrevendo o parágrafo
> original para preservar o registro histórico da auditoria de 01/08.

## Auditoria de rotas, canonical, robots, JSON-LD e Core Web Vitals (03/08/2026)

Continuação da issue #21, escopo: auditar `pageMetaCatalog.ts` contra
`sitemap.xml`, canonical/robots por rota, cobertura de JSON-LD e uma
checklist manual de Core Web Vitals (sem Lighthouse real disponível nesta
sessão).

### Sitemap x pageMetaCatalog — confirmado

- `pageMetaCatalog.ts` (`PAGE_META`) tem 16 entradas, uma por `page.tsx`
  existente em `src/app/**` (mapeamento 1:1 confirmado por busca no
  filesystem — nenhuma rota órfã, nenhuma entrada de catálogo sem página
  correspondente).
- Das 16, 3 são `noindex,follow` (`/historico`, `/historico/[id]`,
  `/historico/comparar`) — corretamente ausentes do sitemap por dependerem
  de dado local (IndexedDB) sem URL pública indexável.
- As 13 restantes estão todas no `sitemap.xml`, incluindo `/privacidade/matriz`
  — **gap do commit anterior (`b7e8cae`) confirmado corrigido**: a rota existe
  em `pageMetaCatalog.ts`, em `src/app/privacidade/matriz/page.tsx`, no
  sitemap e foi gerada com sucesso no build de produção (`npm run build`,
  rota `○ /privacidade/matriz` na lista de saída).
- Nenhum outro gap sitemap x catálogo encontrado.

### Canonical e robots por rota

- Todas as 16 rotas usam `routeMetadata()` (`src/lib/routeMetadata.ts`), que
  deriva `alternates.canonical` de `SITE_ORIGIN + meta.path` e
  `robots.index` de `meta.robots !== 'noindex,follow'` — uma única fonte de
  verdade, sem canonical hardcoded duplicado em nenhuma página.
- As 3 rotas de histórico apontam canonical para `/historico` (a rota-mãe)
  e são `noindex,follow` — não geram conteúdo duplicado indexável porque
  não são indexadas; a decisão já está documentada em comentário no próprio
  catálogo e não foi reaberta.
- Nenhuma rota indexável divide o mesmo canonical com outra — 13 canonicals
  únicos para as 13 rotas indexáveis.
- Candidatas a `noindex` já cobertas: apenas as 3 de histórico (estado
  interno dependente de dado local). `/app` (teste fechado) e `/comparativo`
  (rascunho pendente de validação com Marcos, comentário no catálogo) têm
  conteúdo textual público suficiente para indexação e não foram tratadas
  como candidatas — não há página vazia, formulário sem conteúdo ou tela de
  erro pública no catálogo hoje.

### JSON-LD (`structuredData.ts`) — achado real, não corrigido nesta rodada

- Busca no repositório (`grep -rn` em `src/`) por qualquer uso de
  `buildOrganizationJsonLd`, `buildWebSiteJsonLd`,
  `buildSpeedTestWebApplicationJsonLd`, `buildArticleJsonLd` ou
  `buildProSoftwareApplicationJsonLd` retorna **somente as próprias
  definições em `structuredData.ts`** — nenhuma página, layout ou
  componente importa ou renderiza esses builders. Não existe
  `<script type="application/ld+json">` em nenhum arquivo de `src/`.
- Os comentários do arquivo dizem que a injeção acontece via
  `functions/_middleware.ts` (Cloudflare Pages Function, HTMLRewriter,
  issue #1369 Fase 2) — esse arquivo **não existe neste repositório**
  (`find` por `_middleware*` não retornou nada). O projeto atual é Next.js
  App Router com Metadata API (`routeMetadata.ts`), arquitetura diferente da
  descrita nos comentários — parece resíduo de uma versão anterior
  (SPA/Cloudflare Pages) que migrou para Next.js sem que o JSON-LD fosse
  religado.
- **Consequência real**: Organization, WebSite, SpeedTestWebApplication e
  Article JSON-LD são código morto hoje — nenhuma página do site emite
  dados estruturados. Isso não é um dado inventado nem duplicação (os dois
  riscos que o pedido de auditoria pautava), é ausência total.
- Não implementei a religação nesta rodada: religar JSON-LD por rota
  (decidir Organization/WebSite na home, `Article` nos textos editoriais,
  evitar duplicar `Organization`/`WebSite` em toda página) é uma mudança de
  escopo maior que uma auditoria e não estava no pedido desta tarefa.
  Reportando para Luiz/Renan decidirem abrir uma issue de acompanhamento.

### robots.txt — achado menor

- `public/robots.txt` tem `Disallow: /brand`, mas não existe nenhuma rota
  `/brand` no `src/app` nem em `pageMetaCatalog.ts` (busca confirmada,
  zero resultados). Inofensivo (não bloqueia nada que exista), mas é uma
  regra referenciando uma rota que não existe mais ou nunca existiu neste
  repo — candidato a limpeza numa PR futura de SEO técnico.

### Achado adicional fora do pedido original — `src/middleware.ts`

- O `middleware.ts` do host de preview `signallq.pages.dev` redireciona
  (308) para `signallq.com`, mas só preserva o `pathname` se ele estiver no
  `Set` `validPaths` — e esse `Set` **não inclui `/privacidade/matriz`**
  (nem estava atualizado desde a criação da rota). Resultado: uma visita a
  `signallq.pages.dev/privacidade/matriz` seria redirecionada para
  `signallq.com/` em vez de `signallq.com/privacidade/matriz`.
- Não corrigi: é uma rota pública/redirecionamento de produção, e o escopo
  autorizado desta tarefa foi auditoria de `pageMetaCatalog.ts` x sitemap,
  canonical/robots e JSON-LD — não alteração de middleware. Reportando para
  aprovação explícita do Luiz antes de qualquer ajuste em `validPaths`.

### Checklist manual de Core Web Vitals (sem Lighthouse — honestamente sem medição automatizada)

Não há ferramenta de Lighthouse disponível nesta sessão/ambiente; os itens
abaixo são inspeção manual de código, não métricas medidas (LCP/CLS/INP
reais só virão de Lighthouse/CrUX/PageSpeed Insights contra o domínio
publicado, issue #20).

- [x] **Imagens sem `width`/`height` (risco de CLS)**: toda ocorrência de
  imagem em `src/` usa `next/image` (`SiteNav.tsx`, `PlayStoreBadge.tsx`,
  `AppLandingComponents.tsx`) — não há `<img>` nativo no código, então não
  há dimensão ausente causando reflow. `next/image` exige `width`/`height`
  (ou `fill`) em tempo de tipo.
- [x] **JS de terceiros bloqueante**: o único script de terceiros é o
  AdSense (`AdSenseScript.tsx`), carregado com `next/script`
  `strategy="afterInteractive"` e `async` — não bloqueia o parse inicial, e
  hoje nem chega a rodar (nenhum slot de anúncio existe ainda, consentimento
  é opt-in). Sem risco de INP/LCP por script de terceiros nesta rodada.
- [ ] **CSS render-blocking no `<head>`**: `layout.tsx` carrega 3 folhas de
  estilo via `<link rel="stylesheet">` síncrono (`google-sans-flex.css` e
  2 arquivos do bundle `_ds`) antes do primeiro paint — clássico bloqueio de
  render que pode adiar FCP/LCP. Não medido (sem Lighthouse); recomendação
  não implementada nesta rodada: considerar `preload` + `font-display` na
  fonte, ou `<link rel="preload" as="style">` para as folhas do design
  system, numa PR dedicada a performance.
- [x] **Script inline síncrono no `<head>`** (toggle de tema dark):
  intencional e pequeno (poucas linhas, sem I/O), existe para evitar FOUC de
  tema — trade-off aceitável, não é o tipo de bloqueio que preocupa CWV.
- [ ] **Fontes web e `font-display`**: não auditado nesta rodada (arquivo
  `google-sans-flex.css` não inspecionado quanto a `font-display: swap` vs.
  `block`) — pendente para a PR de performance mencionada acima.
- [x] **Rotas estáticas vs. dinâmicas**: `npm run build` confirma 13 rotas
  públicas indexáveis geradas como `○` (estático/prerenderizado); só
  `/historico/[id]` e `/historico/comparar` (ambas `noindex`) são `ƒ`
  (dinâmicas) — coerente com dependerem de dado local por id/query, sem
  custo de TTFB adicional nas rotas que importam para SEO.

Em resumo: nenhum problema óbvio e grave de CLS ou de JS bloqueante de
terceiros nas rotas públicas; o ponto que mais provavelmente pesa em
LCP/FCP real é o CSS síncrono no `<head>`, mas isso só um Lighthouse/CrUX
real contra o domínio publicado vai confirmar com números — não estou
inventando um score aqui.

### Validações reais desta rodada

- `npm run lint` — pass (0 warnings).
- `npm run typecheck` — pass.
- `npm test -- --run` — pass: 48 arquivos de teste, 254 testes.
- `npm run build` — pass (Next.js 16.2.12 Turbopack, 19 rotas, incluindo
  `/privacidade/matriz` gerada como estática).
- `npm run test:e2e` não foi executado nesta rodada (nenhuma mudança de
  código de produto/UI — apenas documentação e auditoria; sem novo risco de
  regressão de fluxo e2e a validar).
