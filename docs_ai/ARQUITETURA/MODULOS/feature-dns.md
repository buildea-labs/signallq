---
title: "Módulo :featureDns"
description: "Benchmark de resolvedores DNS via DoH, recomendação de troca de provedor e avaliação de coerência do DNS ativo."
type: "técnico"
status: "ativo"
owner: "Camilo"
last_updated: "2026-10-04"
version: "1.1.0"
---

# `:featureDns`

- **Caminho físico:** `android/feature/dns/` (alias flat legado)
- **Namespace:** `io.signallq.app.feature.dns`

## Responsabilidade

Mede e compara resolvedores DNS: executa a suíte de benchmark (DNS do sistema + provedores públicos, 6 rounds cada — 1 warm-up descartado + 5 avaliados) por DNS-over-HTTPS com timeout global de 25s, produz `ResultadoBenchmarkDns` por provedor (tempo, amostras, taxa de sucesso, grade A–D, detecção de resposta inválida) e decide se existe vencedor técnico real, empate técnico ou dados insuficientes (`AvaliadorRecomendacaoDns`). Complementa com a avaliação de coerência do DNS ativo entre amostras (`AvaliadorCoerenciaDns`) e a orientação de configuração manual (`OrientadorConfiguracaoDns`).

Não é dele: renderizar a tela DNS (`DnsScreen.kt` vive no `:app`), aplicar configuração de DNS no sistema (Android não permite; o módulo só sugere), persistir resultados ou expor ViewModel — o estado é observado direto do `snapshotFlow` pelo `MainViewModel` do `:app`.

## Dependências

Extraídas de `android/feature/dns/build.gradle.kts`.

| Tipo | Dependência | Observação |
|---|---|---|
| Plugin | `com.android.library` | — |
| Plugin | `org.jetbrains.kotlin.android` | — |
| `implementation` | `libs.androidx.core.ktx` | — |
| `implementation` | `libs.kotlinx.coroutines.android` | — |
| `implementation` | `libs.okhttp` | transporte DoH (`application/dns-message` em GET base64url) |
| `implementation` | `libs.timber` | — |
| `testImplementation` | `libs.junit`, `libs.kotlinx.coroutines.test` | — |
| `androidTestImplementation` | `libs.androidx.junit`, `libs.androidx.espresso.core` | não há `src/androidTest` |

Sem Hilt, sem Compose e **sem nenhuma dependência de projeto** — nem `:core*`, nem `feature`. Junto de `:featureHome`, é um dos dois módulos totalmente independentes do resto do grafo.

## Consumidores

`grep` por `project(":featureDns")` em `android/**/build.gradle.kts`:

| Consumidor | Arquivo |
|---|---|
| `:app` | `android/app/build.gradle.kts` |

No código do `:app`, os tipos aparecem em `di/AppModule.kt`, `MainViewModel.kt`, `ui/screen/AppShell.kt` e `ui/screen/DnsScreen.kt`.

## Componentes principais

| Arquivo / classe | Responsabilidade |
|---|---|
| `android/feature/dns/src/main/kotlin/io/signallq/app/feature/dns/BenchmarkDnsDoh.kt` | Implementação da suíte: monta a query DNS binária, consulta por DoH cada provedor, aplica warm-up + rounds avaliados, timeout global (`TIMEOUT_SUITE_DNS_MS = 25_000`), detecta resolvedor ativo e emite progresso no `snapshotFlow`. |
| `.../feature/dns/OrientadorConfiguracaoDns.kt` | Sugere primário/secundário/hostname de DNS privado por provedor; devolve `null` quando o ativo já é o melhor e não há alerta de coerência. |
| `.../feature/dns/AvaliadorCoerenciaDns.kt` | Janela deslizante (5 amostras) de divergências entre DNS esperado e observado; produz `NivelAlertaCoerenciaDns` (`none`/`attention`/`critical`). |
| `.../feature/dns/DetectorEnderecoIpPrivado.kt` | Classifica IP do resolvedor como privado/local via `InetAddress` — IPv4 RFC-1918/link-local/loopback e IPv6 `::1`, `fe80::/10`, `fc00::/7`. Unificou duas implementações duplicadas (uma aqui, outra em `DnsScreen.kt`). |
| `.../feature/dns/AvaliadorRecomendacaoDns.kt` | Decide `Vencedor` / `EmpateTecnico` / `SemDadosSuficientes` com margem de 10 ms e taxa de sucesso mínima de 80%. |
| `.../feature/dns/ResultadoBenchmarkDns.kt` | Contrato por provedor (tempo, amostras, tentativas × tentativas avaliadas, taxa de sucesso, grade, `isGatewayLocal`, `respostaInvalida`). |
| `.../feature/dns/BenchmarkDns.kt` | Interface (`snapshotFlow`, `executar(hostConsulta, resolvedoresAtivos, privateDnsHostname)`). |
| `.../feature/dns/SnapshotBenchmarkDns.kt` / `EstadoBenchmarkDns.kt` | Snapshot e estados (`idle`, `executando`, `concluido`, `erro`). |
| `.../feature/dns/FeatureDnsModulo.kt` | Factory `criarBenchmarkDns()` → `BenchmarkDnsDoh`. |

## Riscos e dívidas

- **Lacunas de teste.** Só 3 dos 10 arquivos de produção têm teste (`AvaliadorRecomendacaoDnsTest`, `BenchmarkDnsDohTest`, `DetectorEnderecoIpPrivadoTest`). Ficam descobertos `AvaliadorCoerenciaDns` (janela deslizante e contagem de consecutivas — lógica pura e com estado) e `OrientadorConfiguracaoDns` (mapa de provedores e regra de supressão da sugestão), ambos triviais de testar.
- **`BenchmarkDnsDoh.kt`** é o único ponto do módulo que concentra rede, encoding de pacote DNS, política de rounds e agregação estatística.
- **Regra de negócio dentro de Composable, no `:app`.** `DnsScreen.kt` (em `:app`, acima de 800 linhas) já foi a casa de uma segunda implementação do detector de IP privado (duplicação eliminada em GH#1212 item 10, conforme o KDoc de `DetectorEnderecoIpPrivado`). Vale vigiar a reincidência: a tela é o consumidor direto dos resultados e a fronteira entre apresentação e regra é tênue aqui.
- **Sem ViewModel próprio.** Diferente de `:featureSpeedtest` e `:featureDevices`, o estado do benchmark é consumido direto pelo `MainViewModel` do `:app`, o que engorda o ViewModel monolítico em vez de seguir o padrão de ViewModel por feature já adotado nos outros dois módulos.
- **Regra de dependência entre features: respeitada.** Nenhum `project(":feature…")` — na prática, nenhuma dependência de projeto sequer.
