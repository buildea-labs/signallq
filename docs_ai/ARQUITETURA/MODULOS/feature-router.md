---
title: "Módulo :featureRouter"
description: "Driver somente-leitura do roteador TP-Link Archer C6/A6 (protocolo stok-luci) que normaliza o estado para o contrato compartilhado LocalNetworkDeviceSnapshot."
type: "técnico"
status: "ativo"
owner: "Camilo"
last_updated: "2026-10-04"
version: "1.0.0"
---

# `:featureRouter`

- **Caminho físico:** `android/feature/router/` (alias flat legado, `projectDir` remapeado em `android/settings.gradle.kts`)
- **Namespace:** `io.signallq.app.feature.router`
- **Tipo:** biblioteca Android

## Responsabilidade

Driver de equipamento local para roteador TP-Link Archer C6/A6 (família `tplink-stok-luci`): faz o
login por senha, lê as páginas de status (`status/all`, `cloud_account/get_deviceInfo`, topologia
OneMesh quando existir) e normaliza tudo para `LocalNetworkDeviceSnapshot` de `:coreNetwork`
(`DeviceType.ROUTER`, `SupportLevel.LAB_VALIDATED`). Só leitura.

Não é dele: a UI de "Equipamento de Internet" e o fluxo de pedir a senha (em `:app`,
`AppShell.kt`), a escolha do host (gateway detectado pelo `:app`), o armazenamento de credenciais
(`CredenciaisModemStore`, `:coreDatastore`) e o driver de ONT Nokia (`:featureFibra`).

## Dependências

| Dependência | Para quê |
|---|---|
| `project(":coreNetwork")` | Contratos `localdevice` (`LocalNetworkDeviceSnapshot`, `DeviceType`, `SupportLevel`…) |
| `libs.androidx.core.ktx`, `libs.org.json` | Utilitários e parsing JSON |
| `libs.junit`, `libs.okhttp.mockwebserver` (test) | Testes JVM com servidor HTTP em loopback |

Sem Hilt, sem OkHttp em produção: o HTTP usa `HttpURLConnection` (`UrlConnectionTpLinkTransport`).

## Consumidores

| Consumidor | Local |
|---|---|
| `:app` | `android/app/build.gradle.kts`; `AppShell.kt` chama `TpLinkArcherC6Driver.probe(ip)` e `TpLinkArcherC6Driver(ip).loginAndRead(senha)` |

## Componentes principais

| Arquivo / classe | Responsabilidade |
|---|---|
| `TpLinkArcherC6Driver.kt` | Fachada pública. `probe(host)` — sondagem sem credencial dos dois endpoints de bootstrap (`TpLinkProbeResult.STOK_LUCI_PASSWORD_ONLY`/`NOT_SUPPORTED`); `loginAndRead(password)` → `TpLinkReadResult.Success`/`Failure(TpLinkFailure)`. Modelo só é confirmado C6/A6 após o login. Contém `TpLinkStokLuciClient` e o transporte HTTP injetável (`TpLinkHttpTransport`) |
| `TpLinkStokLuciCrypto.kt` | Handshake criptográfico do protocolo stok-luci (chaves RSA, cifra da senha e do corpo) |
| `TpLinkArcherMapper.kt` | `TpLinkArcherMapper.map(...)` — converte as respostas JSON em `LocalNetworkDeviceSnapshot` (rádios 2.4/5 GHz, clientes cabeados e da malha); devolve `null` se o modelo não for suportado |

`TpLinkFailure` é um conjunto fechado (`INVALID_CREDENTIALS`, `SESSION_EXPIRED`, `UNSUPPORTED_MODEL`,
`COMMUNICATION`, `INVALID_RESPONSE`); a UI traduz uma vez e nunca expõe detalhe HTTP/cripto.

Mapa de campos do levantamento: `docs_ai/technical/TPLINK_ARCHER_ROUTER_FIELD_MAP.md`.

## Riscos e dívidas

- **Um único modelo/família.** Sem abstração de família de driver; `:featureFibra` e este módulo
  repetem a mesma forma (login → leitura → mapper) sem interface comum.
- **Protocolo por engenharia reversa.** Atualização de firmware pode quebrar o login sem sinal de
  compilação; o handshake é coberto por `TpLinkStokLuciCryptoTest`/`TpLinkStokLuciClientTest`
  contra fixtures locais, não contra hardware em CI.
- **Wiring no `AppShell.kt`:** a chamada ao driver e o fluxo de senha vivem em arquivo já acima do
  limiar de 800 linhas; candidato a extração dedicada.
