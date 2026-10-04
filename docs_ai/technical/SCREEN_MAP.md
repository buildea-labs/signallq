---
title: "Screen Map — Android SignallQ"
description: "Mapa de navegação do app consumer (4 raízes + pilha de overlays) validado contra AppShell.kt e AppShellNavigation.kt."
type: "técnico"
status: "ativo"
owner: "Camilo"
last_updated: "2026-10-04"
version: "2.0.0"
---

# Screen Map — Android SignallQ

- **Fonte de verdade:** código — `android/app/src/main/kotlin/io/signallq/app/ui/screen/AppShell.kt`
  e `AppShellNavigation.kt` (enums `AppShellRoot` e `AppShellOverlay`)
- **Escopo:** navegação do app consumer (barra inferior + overlays). Padrões de extensão em
  [`appshell-root-content-registry.md`](appshell-root-content-registry.md) e
  [`appshell-overlay-registry.md`](appshell-overlay-registry.md)

Todas as telas residem em `android/app/src/main/kotlin/io/signallq/app/ui/screen/`. A navegação viva
é `AppShellNavigator` (raiz selecionada + pilha de overlays por raiz), não Compose Navigation.

---

## Barra inferior — 4 raízes

`AppShellRoot` em `AppShellNavigation.kt`; rótulos em `AppShellBottomBar.kt`.

| Índice | Raiz | Label | Composable | Arquivo |
|---|---|---|---|---|
| 0 | `Home` | Início | `Inicio2Screen` | `Inicio2Screen.kt` |
| 1 | `Speed` | Velocidade | `SpeedTestScreen` | `SpeedTestScreen.kt` |
| 2 | `History` | Histórico | `HistoricoScreen` | `AppShellHistoricoRoot.kt` → `HistoricoScreen.kt` |
| 3 | `Tools` | Ferramentas | `FerramentasScreen` | `AppShellFerramentasRoot.kt` → `FerramentasScreen.kt` |

Não existe aba "Ajustes" nem "Mais": Ajustes é overlay, aberto pelo acesso de perfil/menu de cada
raiz. Durante a execução do speedtest a barra inferior some (`shouldShowAppShellBottomBar`).

---

## Overlays

`AppShellOverlay` (`AppShellNavigation.kt`), empilhados em `overlayStack`, cada um em
`AnimatedVisibility` com z-index pela posição na pilha (`rememberOverlayZIndex`).

| Overlay | Composable | Arquivo | Origem típica |
|---|---|---|---|
| `ResultadoVelocidade` | `ResultadoVelocidadeScreen` | `AppShellResultadoVelocidadeOverlay.kt` | Teste de velocidade concluído |
| `Laudo` | `LaudoScreen` | `AppShellLaudoOverlay.kt` | "Gerar laudo" (Ferramentas, diagnóstico, atalhos) |
| `Dispositivos` | `DispositivosScreen` | `DispositivosScreen.kt` | Ferramentas, atalhos da Início |
| `EquipamentoConectar` | `EquipamentoConectarScreen` | `EquipamentoConectarScreen.kt` | Gateway sem endereço de equipamento salvo (GH#1806) |
| `EquipamentoInternet` | `EquipamentoInternetScreen` | `EquipamentoInternetScreen.kt` | Ferramentas, nó do gateway na Início |
| `Fibra` | `EquipamentoInternetScreen` | `EquipamentoInternetScreen.kt` | Conexão bem-sucedida ao equipamento (mesma tela de `EquipamentoInternet`; nome histórico) |
| `Ferramentas` | `FerramentasScreen` | `FerramentasScreen.kt` | Hub aberto como overlay a partir de card de ferramenta sugerida (Início); fecha limpando `ferramentaRecomendada` |
| `Ping` | `PingScreen` | `AppShellPingOverlay.kt` | Ferramentas |
| `Dns` | `DnsScreen` | `AppShellDnsOverlay.kt` | Ferramentas |
| `SinalWifi` | `WifiCasaScreen` | `AppShellSinalWifiOverlay.kt` | Ferramentas, trilha da Início (nome técnico preservado; `SinalWifiScreen` segue existindo) |
| `SinalCanais` | `SinalScreen` | `SinalScreen.kt` (+ `SinalWifiSection`/`SinalCanalSection`/`SinalMovelSection`) | Ferramentas |
| `DiagnosticoGuiado` | `DiagnosticoGuiadoScreen` | `AppShellDiagnosticoGuiadoOverlay.kt` | Início / Ferramentas |
| `DetalhesTecnicos` | `DetalhesTecnicosScreen` | `AppShellDetalhesTecnicosOverlay.kt` | Resultado de velocidade |
| `ModoGamer` | `ModoGamerScreen` | `ModoGamerScreen.kt` | Ferramentas, resultado de velocidade |
| `Ajustes` | `AjustesScreen` | `AjustesScreen.kt` | Acesso de perfil/menu de qualquer raiz |
| `Privacidade` | `PrivacidadeScreen` | `AppShellPrivacidadeOverlay.kt` | Ajustes |
| `Novidades` | `NovidadesScreen` | `AppShellNovidadesOverlay.kt` | Ajustes |
| `Termos` | `TermosDeUsoScreen` | `AppShellTermosOverlay.kt` | Ajustes |
| `Perfil` | — | — | Valor presente no enum **sem uso**: não há tela intermediária de Perfil (comentário em `AppShell.kt`); candidato a remoção |

**Sem rota no app:** telas de chat/IA conversacional (`SignallQScreen`, `SignallQPulseScreen`,
`LLMChatScreen`, `ChatDiagnosticoIaScreen`) e o fluxo "Jogos" (`JogosScreen`, `Overlay.Jogos`)
foram removidos (GH#937, GH#1682, GH#1487 — decisão de produto). O fluxo de IA real é a "Análise
avançada" no `LaudoScreen` (ver `docs_ai/technical/AI_FLOW.md`); o teste de jogo vive no Modo gamer.
Não reintroduzir rota para nenhuma delas.

Sheets sem back-stack (monitoramento, credenciais de equipamento, gerenciar dados, ajuda/suporte,
sobre) não usam `AppShellOverlay` — ver `appshell-overlay-registry.md`.

---

## Onboarding

| Composable | Arquivo | Acesso |
|---|---|---|
| `OnboardingScreen` | `OnboardingScreen.kt`, montado na `MainActivity` | Apenas primeira execução (`onboardingConcluidoFlow` no DataStore); boas-vindas + termos/LGPD, sem pedido de permissão em lote |

---

## Arquivos de suporte à navegação

| Arquivo | Papel |
|---|---|
| `AppShell.kt` | Shell: barra inferior de 4 raízes + pilha de overlays |
| `AppShellNavigation.kt` | `AppShellRoot`, `AppShellOverlay`, `AppShellNavigator`, back |
| `AppShellRootRegistry.kt` / `AppShellOverlayRegistry.kt` | Agregadores de raízes e overlays |
| `AppShellState.kt` | Grupos de estado (`AppShellXxxState`) passados ao shell |
| `MainViewModel.kt` | ViewModel raiz `@HiltViewModel`; dívida registrada em `.claude/rules/higiene-e-padronizacao-repositorio.md` §4.2 |

---

## Diagrama de navegação

```
OnboardingScreen (primeira execução)
    ↓
AppShell  (barra inferior com 4 raízes + overlays)
├── [0] Início ── Dispositivos · Laudo · EquipamentoInternet/Conectar · SinalWifi · DiagnosticoGuiado
├── [1] Velocidade ── ResultadoVelocidade ── DetalhesTecnicos · ModoGamer
├── [2] Histórico
└── [3] Ferramentas (hub)
        ├── Dispositivos · EquipamentoInternet · Ping · Dns
        ├── Laudo · DiagnosticoGuiado · ModoGamer
        └── SinalWifi · SinalCanais

Acesso de perfil/menu (qualquer raiz) → Ajustes
        ├── Privacidade
        ├── Novidades
        └── Termos
```
