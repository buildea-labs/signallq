---
name: architecture-guardrails
description: Prevent monolithic pages/hooks/stores in SignallQ Web — enforce composition limits, single-responsibility hooks, and layered data access before merge.
---

# Architecture guardrails

Contexto: auditoria de 2026-08 encontrou `src/app/page.tsx` com 850 linhas (God Component) concentrando modos de teste, questionário, reteste, telemetria, persistência e resultado, além de `useSpeedTest` com ~270 linhas misturando lock entre abas, heartbeat, estado de rede, motor, ciclo de vida, telemetria e persistência. Estas regras existem para impedir que esse padrão volte a acontecer.

## Regras obrigatórias

- `page.tsx` só compõe e define metadata; nenhuma lógica de estado, fetch, IndexedDB ou telemetria direta nele.
- Nenhuma página ou componente novo acima de ~150–200 linhas sem justificativa explícita registrada no PR.
- Componentes de UI não acessam IndexedDB, telemetria ou infraestrutura diretamente — isso vive em hooks/controllers/repositories dedicados.
- Um hook = uma responsabilidade. Se um hook cresce para cobrir lock, rede, motor, ciclo de vida e persistência ao mesmo tempo, ele deve ser dividido (ex.: `useXController`, `useXTabLock`, `useXNetworkGuard`, `persistXResult`).
- Stores/engines grandes (ex. `historyDatabase.ts`/`measurementRepository.ts`/`comparisonRepository.ts`/`historyExport.ts`/`historySelectors.ts` — antigo `historyStore.ts`, decomposto na issue #90 —, `speedEngine.ts`) são reavaliados a cada US que os expande: se a US adiciona uma responsabilidade nova (nova entidade, novo tipo de dado, novo transporte), extrair em módulo próprio (database, repository, export, selectors, configuração, transporte, cálculo) em vez de acrescentar ao arquivo existente.
- Nenhuma funcionalidade nova é adicionada diretamente a `src/app/page.tsx` — extrair componente ou controller antes de implementar, não depois.
- Utilitários duplicados entre módulos (ex. geração de ID replicada em motor e telemetria) viram um utilitário único compartilhado.
- `eslint --max-warnings=0` depois de qualquer limpeza — zero avisos tolerados: imports não usados, dependências ausentes em `useEffect`, valores calculados e nunca usados, componentes não utilizados ainda importados.

## Quando aplicar

Antes de aprovar qualquer PR que toque `src/app/page.tsx`, hooks de jornada (`useSpeedTest*`), a camada de histórico (`historyDatabase.ts`, `measurementRepository.ts`, `comparisonRepository.ts`, `historyExport.ts`, `historySelectors.ts`), `speedEngine.ts`, a página de Histórico ou qualquer arquivo que já esteja perto do limite de linhas. Bloquear merge se alguma regra acima for violada sem justificativa registrada no PR — não basta apontar, o gate barra.

## Responsável

Caio é dono deste gate e bloqueia merge em violação. Renan é responsável por decompor antes de adicionar, não depois — USs que tocam um arquivo já grande devem ser usadas para desmontá-lo, não para acrescentar mais condicionais.
