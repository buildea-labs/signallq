# Contrato imutavel do diagnostico via Cloudflare (v1)

Status: **fonte de integracao para Site/PWA**. Atualizado em 2026-08-01 para a US #3.

## Autoridade e limite

O motor oficial de diagnostico e o mesmo do Android e roda no Worker Cloudflare `signallq-diagnostic`. O Site/PWA so monta um `DiagnosticSnapshot`, envia-o e apresenta o `DiagnosticReportPayload` devolvido. Nao pode criar regras, pesos, classificacoes, causas, recomendacoes, score, fallback conclusivo nem reinterpretar campos de saida.

Fontes verificadas nesta versao:

- `C:\Projetos\SignallQ\docs_ai\CONTRATOS\openapi\signallq-diagnostic-worker.yaml`;
- `C:\Projetos\SignallQ\integrations\cloudflare\signallq-diagnostic-worker\src\contracts.ts`;
- `C:\Projetos\SignallQ\integrations\cloudflare\signallq-diagnostic-worker\src\diagnostic-report.ts`;
- `C:\Projetos\SignallQ\android\feature\diagnostico\src\main\kotlin\io\veloo\app\kotlin\feature\diagnostico\remote\DiagnosticSnapshotMapper.kt`.

## Transporte e compatibilidade

- Endpoint canonico: `POST /diagnostic/evaluate`; `/api/diagnostic/evaluate` e apenas alias.
- Entrada: `DiagnosticSnapshot`; `schemaVersion` e o unico campo exigido pelo validador atual. O Android envia a versao **6**.
- Sucesso e falha interna do motor respondem HTTP 200 com `DiagnosticReportPayload`. A falha interna retorna `decisao.status = "inconclusive"` e `podeConcluir = false`; isso deve ser apresentado como inconclusivo, nunca convertido em certeza.
- JSON/shape invalido recebe HTTP 400 com `error` e possiveis `details`.
- A versao de schema enviada deve ser mantida enquanto suportada pelo Worker. Aumento exige revisao conjunta do OpenAPI, `DiagnosticSnapshotMapper` Android, tipos deste repositorio e testes de compatibilidade. Nenhuma mudanca de versao e inferida pelo frontend.
- O endpoint sera chamado por uma camada server-side do Next.js na US #4. URL, segredos e politica de acesso nao entram em codigo cliente nem em `NEXT_PUBLIC_*`.

Os tipos de transporte desta versao estao em `src/lib/diagnosticContract.ts`. Eles espelham o contrato publico; nao sao uma segunda implementacao do motor.

## Matriz Android x Web/PWA

| Grupo / campos do snapshot | Android | Web/PWA | Tratamento Web |
| --- | --- | --- | --- |
| `schemaVersion` | mapper oficial envia 6 | disponivel | enviar 6; nunca omitir |
| `sessionId`, `appVersion`, `platform` | disponivel quando a origem fornece | sessao/versao podem ser geradas; plataforma e `web` | metadados opcionais, sem semantica diagnostica |
| `connection.type`, `hasInternet`, `ipv6Available` | APIs nativas | `navigator.onLine` e Network Information parcial | enviar apenas evidencia disponivel; nao deduzir tipo/IPv6 |
| `speed.*`, `quality.*` | speedtest Android | speedtest existente mede download, upload, latencia, jitter, perda e latencia sob carga | mapear metricas medidas, preservando ausencias/medicao parcial |
| `dns.*` | leitura/comparacao Android | teste DNS atual fornece latencia/provedor de forma limitada | enviar apenas valores efetivamente medidos |
| `wifi.*`, `wifiScan.*` | permissao e APIs Wi-Fi Android | indisponivel em navegadores comuns | omitir; nao perguntar nem inventar scan |
| `fiber.*`, `localEquipment` | leitura local segura quando suportada | indisponivel | omitir; nao tentar acesso a roteador |
| `mobile.*` | telemetria celular Android | indisponivel/confiabilidade insuficiente | omitir |
| `gateway.rttMs` | probe TCP do gateway | nao disponivel de modo portavel | omitir |
| `historical.*` | historico local Android | IndexedDB local ja existe | calcular somente a partir dos registros locais na US #9; antes omitir |

Omissao e o sinal correto para dado indisponivel. O Worker informa refinamentos em `dadosAusentes` e pode retornar fluxo `sem_dados_suficientes`.

## Saida e apresentacao

Apresentar literalmente os buckets `*Resultados`, `decisao`, `achadosSecundarios`, `hipotesesDescartadas`, `recomendacoes`, `scoreEngineResultado`, `perfisUso` e `gameReadiness` da resposta. `categoriaOrigem`, `podeConcluir`, `evaluationSource`, `dadosAusentes` e `limitacoesEquipamentoLocal` precisam permanecer visiveis para que a UI nao esconda incerteza ou proveniencia.

`aiAssist` e apenas metadado/solicitacao do Worker; o frontend nao executa `systemPrompt` nem `userPrompt` e nao chama modelo algum por conta propria.

## Plano de substituicao da logica local existente

`src/components/speedtest/GuidedDiagnosis.tsx` hoje calcula titulos e acoes a partir de quatro respostas locais. Essa arvore contradiz a autoridade unica do motor, mas sua remocao/UX pertence as USs funcionais posteriores:

1. US #4 cria o cliente server-side e converte somente metricas medidas em `DiagnosticSnapshot`.
2. US #5 preserva o teste direto e diferencia os modos de medicao; nao acrescenta conclusoes locais.
3. US #7/#8 substituem as perguntas fixas por contexto/questoes derivados do fluxo oficial quando o contrato os disponibilizar.
4. US #9 renderiza o `DiagnosticReportPayload` devolvido, incluindo inconclusivo e ausencias; entao remove `calculateDiagnosis` e a arvore fixa em `GuidedDiagnosis.tsx`.

Enquanto a substituicao nao chega, nenhuma nova regra pode ser adicionada ao componente. A integracao so pode avancar quando o Worker de producao responder ao contrato v1; se nao responder, registrar bloqueio na issue em vez de criar diagnostico alternativo.
