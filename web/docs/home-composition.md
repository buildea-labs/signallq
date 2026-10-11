# Composição da Home

Decomposição de `src/app/page.tsx` (era 850 linhas, God Component) executada na issue #90,
item 6 do plano de auditoria 2026-08 e regras de `skills/architecture-guardrails/`.

## Árvore

```
src/app/page.tsx                        (22 linhas — composição + metadata)
 ├─ QuickTestJourney                    repouso, execução, resultado imediato e falhas
 │   ├─ QuickResult                     conclusão, velocímetro, Rede Cloudflare
 │   │   ├─ MetricSidePanel             upload / latência / estabilidade
 │   │   └─ UseCaseSummary              selo de status + leitura por tipo de uso
 │   ├─ ProblemPrompt                   entrada por problema percebido
 │   └─ TestRunning                     trio ao vivo + cancelar
 ├─ CompleteDiagnosis                   próxima ação, detalhes, ações finais, questionário
 │   ├─ RetestComparison                antes e depois de uma repetição
 │   └─ ResultTechnicalDetails          contexto e detalhes técnicos (recolhidos)
 └─ HomeProductContext                  chips de diagnóstico + diferencial do app
```

Componentes ficam em `src/components/home/`. Copy e mapas de apresentação em `homeCopy.ts` e
`problemStates.ts`; cálculo do mostrador em `speedometerView.ts` (função pura).

## Estado

Todo o estado da jornada vive em `src/hooks/useSpeedTestJourney.ts`: modo de teste, entrada por
problema, questionário contextual, reteste/comparação, telemetria, persistência (histórico e
diagnóstico) e compartilhamento. Ele consome `useSpeedTest(modo)` — que continua responsável só
pela execução da medição e será dividido no item 11 do mesmo plano.

Nenhum componente da Home acessa IndexedDB, telemetria ou `sessionStorage` diretamente. A única
exceção herdada é `GuidedDiagnosis`, que já existia com essa responsabilidade e não foi tocado
nesta refatoração.

`ajudaEstabilidadeAberta` (tooltip de Estabilidade) fica em `QuickResult`, não em
`MetricSidePanel`: o painel desmonta enquanto o teste roda e o estado precisa sobreviver a isso,
como acontecia quando vivia na página.

## Regra permanente

Funcionalidade nova na Home entra em um componente/hook existente ou em um novo — nunca de volta
em `page.tsx`, que só compõe e declara metadata.
