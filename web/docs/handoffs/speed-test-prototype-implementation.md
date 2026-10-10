# Fluxo de Velocidade — implementação do protótipo (web mobile, PWA e desktop)

**Repositório:** `buildea-labs/signallq-web` · **Branch:** `feat/prototipos-webapp-desktop`

Fonte visual: `docs/prototypes/SignallQ - Protótipos de Tela.zip` (arquivo local,
não versionado, não alterado nesta rodada) — telas `SignallQ Speed Flow.dc.html`
(WebApp 1.1–1.5 e 2.1–2.4; Desktop 1.1–1.3, 2.1–2.2) e `Speedometer.dc.html`.

Fonte funcional: o código já existente — motor de medição, diagnóstico,
persistência, histórico, telemetria, consentimento, compartilhamento,
restauração e reteste seguem intactos.

---

## 1. Mapa protótipo → componentes

| Protótipo | Estado visual (`SpeedTestVisualState`) | Componentes |
| --- | --- | --- |
| 1.1 Entrada e formação do velocímetro | `forming` (e `*-running` na fase `preparando`) | `QuickTestJourney` → `QuickResult` → `Velocimetro` (mode `forming`, sem número, sem escala) + "Preparando sua medição…". **Sem tela ociosa**: nenhum controle de entrada. |
| 1.2 Teste rápido em execução | `quick-running` | `QuickResult` (`Velocimetro` mode `measuring`) + `TestRunning` → `MeasurementStatusLine` + "Cancelar teste" |
| 1.3 Resultado do teste rápido | `quick-result` | `ResultStamp` + `Velocimetro` (mode `settled`, rótulo `Download`) + link "Problemas com a sua internet?" + CTA "Fazer teste completo" + `QuickResultDetails` + `HomeProductContext` (Ferramentas) |
| 1.4 Falha na medição | `error` | `AlertScreen` — duas saídas: "Tentar novamente" e "Verificar conexão" (`/ping`) |
| 1.5 Sem conexão | `offline` | `AlertScreen` com uma saída só — sem internet não há o que verificar |
| 2.1 Seleção do motivo | — (não é estado próprio) | `DiagnoseSheet` — modal sobre o resultado rápido: grupos *Rede* e *Problema*, pergunta de aprofundamento e CTA "Diagnosticar minha internet" |
| 2.2 Teste completo em execução | `full-running` | `QuickResult` + `TestRunning` com o passo "1 de 2 · Download" / "2 de 2 · Upload" |
| 2.3 Processando diagnóstico | `full-running` na fase `processando` | `DiagnosingSteps` (lista de etapas derivada da fase real do motor) |
| 2.4 Resultado completo com diagnóstico | `full-result` / `diagnosing` | `CompleteDiagnosis` (overline "Diagnóstico" + conclusão única) → `FullResultMetrics` → `UseCaseSummary` → ações (reteste + compartilhar) → `ResultTechnicalDetails` ("Ver detalhes da medição") → `HomeProductContext` → `ResultAdSlot`. **Sem questionário.** |
| Desktop 1.1 Painel de velocidade | `forming` / `*-running` / `quick-result` | Mesma árvore; `PageShell align="center"` + `layout.contentMax` |
| Ferramentas (4 células) | — | `HomeProductContext` → Ping, DNS, Meu IP, Jogos |
| Desktop 2.1 Diagnóstico completo | `full-result` | `FullResultMetrics` vira grade de 4 tiles a partir de `lg` |
| Desktop 1.2/1.3 e 2.2 | `error` / `offline` / `processando` | Mesmos componentes, mesma composição |
| Último resultado restaurado | `restored-result` | `ResultStamp` "Último resultado · …" + mostrador (rápido) ou diagnóstico (completo) |

## 2. Componentes reaproveitados (sem substituição)

`HomeClient`, `PageShell`, `useSpeedTestJourney`, `deriveSpeedTestVisualState`,
`useSpeedTest`, `useSpeedTestController`, `QuickTestJourney`, `QuickResult`,
`TestRunning`, `CompleteDiagnosis`, `usePostResultProblem`,
`Velocimetro`, `gaugeMath`, `speedometerView`, `speedometerIdentity`,
`problemStates`, `homeCopy`, `UseCaseSummary`, `ResultTechnicalDetails`,
`RetestComparison`, `GuidedDiagnosis`, `ResultAdSlot`, `HomeProductContext`,
`speedTestJourneySession`, `speedTestJourneyComparison`,
`speedTestJourneySharing`, `classification`, fixtures e baseline visual
existentes.

Nenhuma biblioteca nova. Nenhum Redux/Zustand/XState/Storybook. Nenhuma
segunda jornada paralela: continua existindo uma única árvore funcional,
responsiva, alimentada pelo mesmo hook.

## 3. Componentes criados

| Arquivo | Papel |
| --- | --- |
| `src/lib/speedTestLayout.ts` | Função pura `speedTestLayoutFor(visualState, phase)` → `{ stage, dial, contentMax }`. Layout é consequência do estado visual, não uma segunda decisão dentro dos componentes. |
| `src/components/speedtest/MeasurementStatusLine.tsx` | Ponto pulsante + frase da fase corrente (`aria-live`), com passo opcional. |
| `src/components/speedtest/ResultStamp.tsx` | Selo de procedência do resultado ("Teste rápido · Executado agora", "Último resultado · …"). |
| `src/components/speedtest/AlertScreen.tsx` | Tela cheia de falha/offline: círculo com ícone, título, descrição, ações. |
| `src/components/speedtest/DiagnosingSteps.tsx` | Lista de etapas do processamento do diagnóstico, derivada da fase real. |
| `src/components/home/FullResultMetrics.tsx` | Hierarquia de métricas do resultado completo (+ `buildFullResultMetrics`, puro). |
| `src/components/home/QuickResultDetails.tsx` | "Detalhes da medição" do resultado rápido (disclosure curto). |
| `src/hooks/usePrefersReducedMotion.ts` | Lê `prefers-reduced-motion` para o mostrador, que interpola em JavaScript. |
| `src/components/speedtest/DiagnoseSheet.tsx` | Sheet "Diagnosticar minha internet" (tela 2.1): modal com trava de foco, Esc, scroll lock e devolução de foco ao gatilho. |
| `src/lib/networkEntry.ts` | Vocabulário da rede **declarada** (Wi-Fi / Dados móveis / Outra rede) — declaração, não detecção. |
| `src/app/ping/page.tsx`, `src/components/ping/PingTool.tsx`, `src/hooks/usePingTool.ts` | Ferramenta de Ping: quarta célula da grade do protótipo, sobre a mesma coleta de latência do motor. |
| `src/test/fixtures/speedTestJourney.ts` | `withDerivedJourneyState` — dublês de jornada com `visualState`/`layout` derivados pelas funções reais. |

## 4. Arquivos alterados

- `src/lib/gaugeMath.ts` — geometria do mostrador do protótipo: `dialCurve`
  (`t^0.78`), `dialPolar`/`dialAngle`/`dialArcPath` (cx 150, cy 170, r 125,
  início 188°, varredura 196°), `dialAutoMax`, `dialScaleSet`,
  `dialScaleForPhase`, `dialTickLabel`, `dialMajorTicks`, `dialMinorTicks`.
  Todas as funções antigas continuam exportadas.
- `src/components/Velocimetro.tsx` — reescrito para o desenho do protótipo
  (trilho discreto, arco ativo com pontas arredondadas, halo pulsante,
  marcador luminoso com anel, ponteiro fino com pivô, escala adaptativa,
  número tabular dentro do SVG). Ganhou os modos `forming | measuring |
  settled | restored | error | quiet` e os props opcionais `hideValue` e
  `showScale`; a API antiga continua válida.
- `src/components/home/QuickResult.tsx` — passou a decidir mostrador, selo e
  cor a partir de `journey.layout` / `journey.visualState`.
- `src/components/home/QuickTestJourney.tsx` — composição por estado visual;
  falha/offline agora usam `AlertScreen`; CTA de repouso saiu de dentro do
  mostrador.
- `src/components/home/TestRunning.tsx` — passo + linha de estado do
  protótipo; na fase `processando` do modo Completo, delega a
  `DiagnosingSteps`.
- `src/components/home/CompleteDiagnosis.tsx` — diagnóstico lidera a tela;
  `FullResultMetrics` + `UseCaseSummary` logo abaixo; botão de reteste
  contornado, como no protótipo.
- `src/components/home/HomeProductContext.tsx` — seção "Ferramentas" em
  cartões baixos com descrição.
- `src/components/home/homeCopy.ts` — `DIAG_ITEMS` ganhou `description`.
- `src/components/home/speedometerView.ts` — número central inteiro
  (`formatDialNumber`), como no protótipo.
- `src/components/home/ProblemPrompt.tsx`, `PostResultProblemPrompt.tsx` —
  chips e CTA no acabamento do protótipo; o CTA passou a se chamar
  "Fazer teste completo".
- `src/hooks/useSpeedTestJourney.ts` — expõe `layout`; `shellAlign` passou a
  derivar dele.
- `src/app/HomeClient.tsx` — `contentMax` vem de `layout`.
- `src/app/layout.tsx` — `<main>` virou coluna flex com `flex-1`, para que
  `PageShell align="center"` tenha altura para centralizar.
- `src/index.css` — animações do mostrador (`sq-dial-*`) e do ponto de
  estado, todas neutralizadas em `prefers-reduced-motion`.
- `src/components/historico/HistoryDetail.tsx` — usa `mode="quiet"` (leitura
  arquivada não "se forma" agora).
- `.gitignore` — ignora `docs/prototypes/` (arquivo local do Luiz).
- `public/sitemap.xml` — registra `/ping` junto das demais ferramentas
  indexáveis. **Indexação é alçada do Renan**: se a decisão for não indexar a
  ferramenta, basta remover essa linha.
- Removidos: `src/components/home/ProblemPrompt.tsx` e
  `src/components/home/PostResultProblemPrompt.tsx` — a entrada por problema
  pré-teste e o questionário inline não existem mais na jornada.
- Testes: ver §8.

## 5. Decisões arquiteturais e divergências registradas

### 5.1 Layout derivado, não decidido na UI
`speedTestLayoutFor` traduz cada um dos nove estados em `stage`/`dial`/
`contentMax`. Sem isso, cada componente reinventaria a regra de "quando o
velocímetro sai de cena" — a segunda fonte de verdade que o protótipo torna
inevitável (telas 2.3 e 2.4 não têm mostrador).

### 5.2 A jornada é a do protótipo, e ela não tem tela ociosa
Correção da primeira entrega, que preservou a jornada antiga e trocou só o
acabamento. Saíram o seletor Rápido/Completo e a declaração de problema
antes da medição: nenhum dos dois existe no protótipo, e ambos ofereciam um
caminho paralelo ao que ele desenha. A rota entra medindo; o único caminho
declarado para o teste completo é o resultado rápido — pelo CTA direto ou
pelo sheet.

Consequências registradas: a telemetria `FEATURE_SPEEDTEST_PROBLEMA_ABANDONADO`
deixou de ter evento que a dispare (não havia mais tela para abandonar) e foi
removida do fluxo; `FEATURE_SPEEDTEST_ENTRADA_PROBLEMA` e
`FEATURE_SPEEDTEST_PROBLEMA_SELECIONADO` passaram a marcar a entrada por rota
editorial. Nenhuma constante de telemetria foi apagada de `telemetry.ts`.

### 5.3 O contexto vive num sheet modal, como no protótipo
Também correção da primeira entrega, que havia posto os chips inline. O
`DiagnoseSheet` sobe sobre o resultado pelo motivo que o próprio protótipo
dá: o resultado do teste continua com uma única URL indexável. Ele é modal de
verdade (trava de foco, Esc, scroll lock, foco devolvido ao gatilho) e fica
acima do banner de consentimento — que, em `z-[1010]`, cobriria o CTA no
mobile.

A pergunta de aprofundamento do fluxo contextual foi movida para dentro dele.
É o que permite a tela 2.4 cumprir "não reapresentar o questionário" sem
perder a funcionalidade.

### 5.3.1 A ferramenta de Ping passou a existir
A grade do protótipo tem quatro células e a primeira é Ping. Em vez de
mostrar três e fingir que a grade era essa, a rota `/ping` foi criada,
reaproveitando a mesma coleta de latência do motor (`collectLatency` +
`summarizeLatency`) contra a mesma sonda — não há uma segunda definição de
"ping" no produto. A página declara o que mede: o navegador não faz ICMP,
então o número é o tempo de ida e volta de 20 requisições HTTP até a borda da
sonda.

### 5.4 Tipografia: uma família só
O protótipo usa `Sora` para display e `Plus Jakarta Sans` para texto. O
repositório tem regra explícita de fonte única (`src/styles/tokens.css`:
"nenhuma segunda família em nenhuma tela"). Foram adotados os **pesos,
tamanhos, tracking e hierarquia** do protótipo sobre `Google Sans Flex`.
Nenhuma fonte nova foi baixada.

### 5.5 Cor do arco
Medindo: sempre o accent, como no protótipo. As cores por fase do motor
(verde no download, âmbar no upload) faziam o arco trocar de cor no meio da
mesma medição sem que nada tivesse piorado. Assentado: a cor vem da
classificação do download (`classifyDownload`), não do fato de o teste ter
terminado — atende diretamente ao "não usar verde só porque o teste acabou".

### 5.6 Resultado rápido continua só com download
O painel desktop do protótipo (`desktop-1`, "Resultado do teste") mostra
Upload/Ping/Servidor sob o rótulo "Teste rápido". Isso contradiz o contrato
funcional ("resultado rápido apenas com download", e a rodada rápida não mede
upload). O contrato funcional venceu; o desktop mostra a mesma leitura do
mobile.

### 5.6.1 Medir passou a exigir ação explícita
Antes, um efeito disparava o aprofundamento assim que o fluxo de perguntas
concluía — a medição começava por causa de uma resposta, não de um "pode
medir". Agora só o CTA do sheet ("Diagnosticar minha internet") ou o "Fazer
teste completo" do resultado iniciam. O segundo segue disponível sem
nenhum contexto declarado, como o protótipo permite.

### 5.7 "Processando diagnóstico" é uma fase, não um estado terminal
A tela 2.3 do protótipo corresponde à fase `processando` do modo Completo,
não ao estado `diagnosing` de `SpeedTestVisualState` (que já é um resultado
completo com aprofundamento). A lista de etapas é renderizada na fase real; o
estado `diagnosing` continua sendo o resultado completo com a conclusão
contextual.

### 5.8 Mostrador escala por CSS, não por degraus de `size`
O protótipo troca espessuras e tamanhos de fonte em degraus de `size` (250,
300, 340, 356). Aqui o desenho inteiro vive na viewBox 300×206 e escala com o
contêiner — uma implementação só para todas as larguras, sem tabela de
tamanhos para manter.

### 5.9 Dependência do Android
Nada do app Android foi lido, alterado ou sincronizado. As referências de
paridade que já existiam no código (cortes de `classification.ts` e
vocabulário de `speedometerIdentity.ts`, ambos portados do Android em rodadas
anteriores) foram **preservadas como estão**. Nenhuma nova dependência foi
criada.

## 6. Decisões responsivas

- **Uma árvore só.** Nenhum componente duplicado por breakpoint; a diferença
  entre mobile e desktop é composição (`stage` vs `document`), largura
  (`contentMax`) e a grade de métricas (`grid-cols-2` → `lg:grid-cols-4`).
- **Etapas curtas centralizadas.** Formação, medição, processamento, falha e
  offline usam `align="center"`; com `<main>` agora em coluna flex, isso
  centraliza de fato na altura disponível — antes o conteúdo ficava colado no
  topo com um vazio embaixo.
- **Larguras.** Etapa e resultado rápido em 560px; resultado completo em
  720px, que é o que os quatro tiles de métrica pedem. Sem cartões laterais
  inventados para preencher a tela.
- **Mobile durante a medição.** Sem rolagem: o mostrador, a linha de estado e
  o cancelamento cabem na viewport de 390×844.
- **Áreas de toque.** Botões de ação com `min-h-[44px]`/`48px`, incluindo os
  links "Cancelar teste" e "Cancelar diagnóstico".

## 7. Estados implementados

Os nove estados de `SpeedTestVisualState`: `forming`, `quick-running`,
`quick-result`, `full-running`, `diagnosing`, `full-result`,
`restored-result`, `error`, `offline` — mais a fase `processando` do modo
Completo, que ganhou tela própria (tela 2.3).

Mais o sheet de diagnóstico (tela 2.1), que não é um estado da jornada mas é
uma tela do fluxo, e por isso também tem captura própria na baseline.

Jornada: sem resultado restaurável, o velocímetro se forma e o teste rápido
começa sozinho — inclusive vindo de rota editorial (`/?context=...`), caso em
que o problema declarado lá vira o contexto da medição e sai da URL. Com
resultado restaurável, o último resultado é restaurado e **nenhuma** medição
é disparada (provado por contagem de requisições no teste visual). Reteste só
por ação explícita. Cancelamento, erro, offline e resultados
parcial/inconclusivo/contaminado seguem os contratos existentes.

Não há guarda de "já autostartou nesta sessão": cancelar leva ao estado de
falha (com "Tentar novamente" explícito) e concluir deixa um resultado
restaurável — nenhum caminho volta a `idle`, então não há laço de medição.

## 8. Cobertura de teste

Novos/atualizados:

- `src/lib/gaugeMath.test.ts` — geometria do mostrador (curva, varredura,
  caminho do arco, escala automática, rótulos, traços que não se sobrepõem).
- `src/lib/speedTestLayout.test.ts` — os nove estados → layout.
- `src/components/home/FullResultMetrics.test.ts` — hierarquia das métricas e
  o *fallback* de latência sob carga → jitter.
- `src/components/speedtest/DiagnosingSteps.test.tsx` — progresso derivado da
  fase real e cancelamento.
- `src/components/home/QuickResult.test.tsx`, `QuickTestJourney.test.tsx` —
  passaram a derivar `visualState`/`layout` pelas funções reais
  (`withDerivedJourneyState`), em vez de fixá-los à mão.
- `src/components/home/statusDeduplication.test.tsx` — atualizado para a nova
  composição (`UseCaseSummary` agora vive dentro de `CompleteDiagnosis`).
- `e2e/journey-accessibility.spec.ts`, `e2e/aprofundamento-retry-network.spec.ts`,
  `e2e-visual/…` — rótulo do CTA ("Fazer teste completo") e espera de
  assentamento do mostrador antes das capturas de medição.

## 9. Acessibilidade

- `<h1 className="sr-only">` da Home preservado; `<h1>` da conclusão do
  resultado completo continua único na tela.
- A narração da fase vive num único `role="status" aria-live="polite"`
  (`MeasurementStatusLine`) — o rótulo duplicado sob o mostrador foi removido.
- `DiagnosingSteps` marca cada etapa com texto (`(concluído)` /
  `(em andamento)` / `(aguardando)`), não só com cor/ícone.
- `AlertScreen` usa `<h2>` real e botões nativos; nada depende só de cor.
- `prefers-reduced-motion`: além do CSS, o mostrador lê a preferência em JS
  (`usePrefersReducedMotion`) e passa a desenhar o valor final direto.
- `underline` da Política de Privacidade e o cancelamento pendente do
  controller não foram tocados.
- Sem overflow horizontal em 320/360/390/430/600/768/1024/1280/1440.
- `DiagnoseSheet` é modal de verdade: `role="dialog"`, `aria-modal`,
  `aria-labelledby`, trava de foco em Tab/Shift+Tab, Esc fecha, o foco volta
  ao gatilho e o corpo não rola por trás. Os dois grupos de chips são
  `radiogroup`/`radio` com `aria-checked`.
- `journey-accessibility.spec.ts` (axe, foco, teclado) segue verde, agora
  cobrindo o sheet no lugar do popover do seletor de modo.

## 10. Comandos executados e resultados

| Comando | Resultado |
| --- | --- |
| `npm run lint` | verde (0 erros, `--max-warnings=0`) |
| `npm run typecheck` | verde (`tsc --noEmit`, sem `any`, sem cast genérico, sem `eslint-disable` novo) |
| `npm test` | **317 testes / 56 arquivos**, todos verdes (baseline da rodada: 291) |
| `npm run build` | verde — 29 rotas geradas (com `/ping`) |
| `npm run test:visual` | **20/20** (9 estados + sheet de diagnóstico, × 2 viewports) |
| `npm run test:e2e` | **31 casos** (7 novos, cobrindo as ferramentas). Verde por caso; ver a instabilidade conhecida abaixo |

### Instabilidade conhecida no `test:e2e` (pré-existente, não introduzida aqui)

Os testes que chamam `AxeBuilder.analyze()` falham de forma intermitente com
`Execution context was destroyed, most likely because of a navigation` — erro
de infraestrutura, nunca de asserção. A vítima muda de execução para execução.

Evidência de que não vem desta branch: rodando a suíte **sem** os testes novos
de Ferramentas, a falha migrou para `comparativo-accessibility.spec.ts`, que
esta branch não toca. Todos os casos passam quando executados isoladamente.

Causa provável: o `webServer` do `playwright.config.ts` é `npm run dev`, e o
Next compila rotas sob demanda durante a execução; o HMR resultante destrói o
contexto no meio da análise do axe. Não bloqueia a CI — `ci.yml` roda lint,
typecheck, vitest e build, **não** roda e2e — e o config já usa `retries: 2`
quando `CI` está definido.

Correção de raiz sugerida (fora do escopo desta branch, muda o laço local de
todo mundo): rodar o e2e contra build de produção (`next build` + `next
start`) em vez do dev server. Não alterei o config para não mascarar nem para
decidir sozinho por infraestrutura compartilhada.

### Falhas reais encontradas e corrigidas

Nenhum timeout foi aumentado, nenhum teste foi desativado e nenhuma falha foi
mascarada. Estas apareceram e foram corrigidas na origem:

- axe `definition-list`: invólucro extra dentro do `<dl>` de
  `FullResultMetrics` — corrigido na estrutura, não na asserção;
- o banner de consentimento (`z-[1010]`) cobria o CTA do sheet no mobile,
  deixando-o inalcançável — o sheet subiu para `z-[1100]`;
- a fase `preparando` mostrava um travessão no centro do mostrador, como se
  houvesse leitura — passou a manter o mostrador em formação.

## 11. Screenshots

`test-results/speed-test-flow-baseline/` — dez telas × duas viewports
(390×844 e 1440×900), geradas por `npm run test:visual`: os nove estados de
`SpeedTestVisualState` mais o sheet de diagnóstico.

## 12. Diferenças restantes em relação ao protótipo

1. `Sora` + `Plus Jakarta Sans` → família única do design system (§5.4).
2. Resultado rápido no desktop não mostra upload/ping (§5.6) — o protótipo
   mostra, mas a rodada rápida não os mede.
3. Placeholders de anúncio do protótipo → `ResultAdSlot` real, que só aparece
   com consentimento e configuração (regra existente da issue #21).
4. Cores do protótipo em `oklch` → tokens do design system do repositório
   (mesma família de roxo/verde/âmbar/vermelho).
5. Textos de falha/offline usam os de `problemStates.ts` (específicos por
   causa: "Servidor indisponível", "Conexão interrompida", "Teste cancelado")
   em vez do genérico "Não conseguimos concluir a medição." do protótipo — a
   mensagem específica diz mais e já era o contrato funcional.

6. Na tela 2.4, um bloco extra: "Este é o resultado do teste completo — pode
   variar…", exibido quando o download do teste completo difere da estimativa
   rápida. O protótipo não modela a transição rápido→completo, então não tem
   equivalente. Fica como nota de apoio, com peso menor que o diagnóstico
   (11.5px, filete lateral, 80% do texto secundário — 5,31:1 no claro e
   7,91:1 no escuro).

Divergências que **deixaram de existir** ao longo do trabalho: a tela ociosa
com seletor de modo, os chips de contexto inline no lugar do sheet, a grade de
três ferramentas, os seis blocos a mais no resultado completo e a ordem
trocada entre ações e detalhes.

## 12.1 Correções vindas da validação em preview

Três defeitos só apareceram no uso real, num aparelho, e todos na mesma
região: as ferramentas. Nenhum teste abria um cartão da grade — a cobertura
inteira parava na tela de Velocidade.

| Defeito | Causa | Correção |
| --- | --- | --- |
| Painel do modal transparente, com a página aparecendo por trás do texto | `--surface` e `--surface-elevated` nunca foram definidos; custom property inexistente invalida a declaração e `background-color` cai para `transparent` (medido: `rgba(0,0,0,0)`). 33 usos em 4 componentes | mapeados para `--bg-card` e `--bg-secondary`; o vocabulário fantasma saiu de circulação |
| Texto sem acentos ("provedores pblicos", "boto direito") | arquivo já estava assim em `HEAD` — o `DnsModal` inteiro tinha um caractere acentuado | 16 trechos restaurados em `DnsModal` e `JogosModal` |
| Telas sem como voltar | as ferramentas têm dois modos; só o modal tinha saída. Na página inteira o "X" chamava `router.back()`, que sem entrada anterior sai do site ou não faz nada. `/ping` não tinha nem isso | `ToolBackLink` (`<Link href="/">`) na página inteira; o "X" ficou restrito ao modal, onde `back()` é correto por construção |

Não dá para decidir `back()` em runtime: este Next não guarda índice em
`history.state`, `history.length` acumula a aba inteira e `document.referrer`
vem vazio em navegação client-side — os três medidos.

`e2e/ferramentas-navegacao.spec.ts` fecha a lacuna: sete casos cobrindo os
dois modos de abertura das quatro ferramentas, a opacidade do painel e os
acentos.

## 12.2 Tema escuro em preto puro

`--bg-primary` do tema escuro passou de `#131217` para `#000000` a pedido do
Luiz, para avaliação. Contraste **melhora** em todos os tokens de primeiro
plano (`--text-primary` de 14,38:1 para 16,20:1; `--border` de 5,90:1 para
6,65:1); nenhum piora. O que aumenta é a amplitude luminosa (21:1 entre preto
e branco puros), que é o que causa halação para quem tem astigmatismo — e
isso a razão de contraste da WCAG não mede. Se incomodar, o ajuste é baixar
`--text-primary`, não voltar o fundo.

`themeColor` deixou de ser um valor único (pintava a barra do navegador de
`#131217` também no modo claro) e virou uma cor por esquema. O manifesto do
PWA acompanha.

**Achado junto:** o bloco de tema escuro de `tokens.css` era **inerte**. O
bundle sincronizado do design system declara os mesmos tokens sob `.dark`,
com a mesma especificidade, e é carregado depois (folha 2 contra folha 0) —
vencia por ordem de documento. Os valores eram idênticos, então nada parecia
quebrado, mas nenhum override de tema feito ali surtia efeito. O seletor subiu
para `html.dark` (0,1,1).

## 12.3 Pendências que não são minhas para fechar

| Item | Quem decide | Estado |
| --- | --- | --- |
| Revisão independente da entrega | **Caio** (`AGENTS.md`: não implementa a entrega que revisa) | pendente — eu implementei, não posso revisar |
| Indexar `/ping` (está no `sitemap.xml`, ao lado de `/dns` e `/jogos`) | **Renan** (SEO técnico: rotas, metadados, indexação) | implementado; confirmar ou remover a linha |
| Preto puro vira definitivo? | **Luiz** | validado em aparelho; reverter é uma linha em `tokens.css` |
| `manifest.json` com `theme_color`/`background_color` pretos | **Luiz** | em produção, altera a splash de quem já tem o PWA instalado |
| Remoção do seletor Rápido/Completo, da entrada por problema pré-teste e da telemetria `FEATURE_SPEEDTEST_PROBLEMA_ABANDONADO` | **Luiz / Claudete** | implementado conforme a jornada do protótipo |
| `--text-tertiary` é alias de `--text-secondary` (mesmo valor), usado em ~30 lugares | **Juliana / design system** | fora do escopo desta branch; registrado |

## 13. Escopo

Nenhum arquivo fora de `buildea-labs/signallq-web` foi lido, alterado ou
executado. Nenhum arquivo Kotlin/Compose, nenhum módulo mobile nativo, nenhum
Worker, nenhum outro repositório. Nenhum commit, issue ou PR fora deste
repositório. O ZIP do protótipo não foi movido, renomeado nem alterado.

## 14. Commits

| SHA | Mensagem |
| --- | --- |
| `ea47b7a` | `feat(speed-test): redesenha o velocímetro conforme o protótipo` |
| `4417582` | `feat(speed-test): implementa o fluxo do protótipo em uma árvore responsiva` |
| `703d2d8` | `test(speed-test): cobre a geometria, o layout e as telas novas do fluxo` |
| `b17fd19` | `docs(speed-test): registra o handoff da implementação do protótipo` |
| `c3536c4` | `feat(speed-test): adota a jornada do protótipo, sem tela ociosa` |
| `4440fa6` | `feat(ping): cria a ferramenta de Ping e completa a grade de ferramentas` |
| `dbaf8f4` | `test(speed-test): alinha a cobertura à jornada do protótipo` |
| _(commit deste arquivo)_ | `docs(speed-test): atualiza o handoff para a jornada do protótipo` |

Os quatro primeiros são a implementação visual; os três seguintes corrigem a
jornada, que a primeira entrega havia preservado da versão antiga.

Branch `feat/prototipos-webapp-desktop`. Sem merge em `main`, sem deploy, sem
release, sem alteração de configuração de domínio, sem PR.

## 15. `git status` final

```
$ git status --short
(vazio — árvore de trabalho limpa)

$ git log --oneline -8
<este commit> docs(speed-test): atualiza o handoff para a jornada do protótipo
dbaf8f4 test(speed-test): alinha a cobertura à jornada do protótipo
4440fa6 feat(ping): cria a ferramenta de Ping e completa a grade de ferramentas
c3536c4 feat(speed-test): adota a jornada do protótipo, sem tela ociosa
b17fd19 docs(speed-test): registra o handoff da implementação do protótipo
703d2d8 test(speed-test): cobre a geometria, o layout e as telas novas do fluxo
4417582 feat(speed-test): implementa o fluxo do protótipo em uma árvore responsiva
ea47b7a feat(speed-test): redesenha o velocímetro conforme o protótipo
```

`docs/prototypes/` continua fora do versionamento (agora via `.gitignore`) e
o ZIP segue intacto no lugar onde estava. `test-results/` já era ignorado.
