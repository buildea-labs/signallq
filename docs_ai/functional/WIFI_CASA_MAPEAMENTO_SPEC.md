---
title: "WiFi Casa — mapeamento espacial de Wi-Fi"
description: "Evolução da ferramenta 'Encontrar um bom lugar' para mapeamento de cômodos com grid 2D e comparação Antes×Depois de reposicionamento de roteador/mesh."
type: "funcional"
status: "ativo"
owner: "Cora"
last_updated: "2026-09-26"
version: "1.0.0"
---

# WiFi Casa — mapeamento espacial de Wi-Fi

## 1. Status e objetivo

Esta especificação descreve a evolução da ferramenta que era **"Encontrar um bom lugar"**
(`TipoFerramenta.SINAL_WIFI`, tela `SinalWifiScreen.kt`) para **WiFi Casa**: um mapeamento espacial
de cobertura Wi-Fi por cômodo, com comparação Antes×Depois ao reposicionar roteador ou nó mesh.

Implementado e validado em 2026-09-26 (Camillo — architecture plan; Davi — implementação; Breno —
QA independente, veredito PRONTO PARA ENTREGA). `TipoFerramenta.SINAL_WIFI` continua como
identificador técnico; a tela vigente é `WifiCasaScreen.kt`.

Origem: inspirada no app irmão da família Buildea **WiFi Casa (iOS)** (`gmmattey/wifi-check`), que
resolve o mesmo problema em iOS via inferência LAN/WAN (RSSI não é acessível na plataforma). No
Android o SignallQ já lê RSSI real e já tem motor de classificação de sinal mais granular — o que
se importa do app iOS **não é o motor de diagnóstico**, é a camada de UX espacial: mapear cômodos e
comparar cobertura antes/depois de uma mudança física.

## 2. Evidência do estado atual

A ferramenta hoje (`SinalWifiScreen.kt`, `SinalWifiViewModel.kt`):

- é um indicador **ao vivo, ponto único, sem persistência de posição** — mostra categoria de sinal
  (Excelente/Bom/Regular/Fraco), dBm, velocidade do link, padrão Wi-Fi e suporte a MU-MIMO;
- amostra RSSI a cada 1500 ms via `WifiManager.getConnectionInfo()`, só com a tela em `RESUMED`;
- não salva onde cada leitura foi feita — o estado é recriado a cada entrada na tela;
- é listada no hub Ferramentas como **"Encontrar um bom lugar"** / "Ande pela casa acompanhando o
  sinal Wi-Fi", ícone `Icons.Outlined.NetworkWifi` (`FerramentasScreen.kt:215`), atrás da flag
  `featureFlags.wifiEnabled` e de permissão de localização.

Decisão de produto já registrada em `SinalWifiScreen.kt:63-76` (2026-08-19): categoria simples em
destaque, dBm como detalhe secundário, **sem prometer precisão de cômodo/planta que o celular não
sustenta**. Essa decisão continua válida e não é revertida por esta spec — ver seção 3.1.

O que não existe hoje e esta spec introduz: mapeamento multi-ponto, rótulo de cômodo definido pelo
usuário, grid visual, sessão persistente de mapeamento, e fluxo Antes×Depois vinculado a
reposicionamento físico de equipamento (distinto do reteste temporal já existente em
`ComparacaoRetesteUiState.kt` e da comparação de duas linhas de histórico do mesmo `networkId`).

## 3. Decisão de produto

### 3.1 O grid é declarado pelo usuário, não inferido pelo app

O WiFi Casa **não faz triangulação nem infere geometria da casa a partir do sinal**. O usuário
posiciona livremente marcadores num grid 2D em branco (sem planta importada, sem escala real) e
rotula cada um ("Sala", "Quarto 2", "Roteador"). A precisão da medição em cada marcador continua
sendo exatamente a mesma leitura pontual de RSSI que a ferramenta já faz hoje — a novidade é
permitir salvar essa leitura associada a um rótulo e a uma posição relativa no grid, e comparar
entre marcadores. Isso preserva a decisão de 2026-08-19: o app nunca afirma "seu sinal aqui é X
porque você está a Y metros do roteador" — ele mostra "o sinal medido no marcador que você chamou
de 'Quarto 2' foi X".

### 3.2 Renomeação no hub de Ferramentas

"Encontrar um bom lugar" passa a se chamar **"WiFi Casa"** no hub. Mantém-se `TipoFerramenta.SINAL_WIFI`
como identificador técnico (evita migração de flag/telemetria); a rota interna
(`screenName()`) pode ganhar um valor próprio se a tela for suficientemente distinta da atual para
não colidir com `SINAL_CANAIS_MOVEL`, a confirmar com Davi na decomposição técnica.

Copy proposta (mantendo o tom direto e em segunda pessoa já usado):

- Nome no hub: **"WiFi Casa"**
- Descrição no hub: **"Mapeie o sinal em cada cômodo e compare antes e depois de mudar o roteador"**
- Título da tela: **"WiFi Casa"**

### 3.3 Antes×Depois é iniciado manualmente pelo usuário

O usuário decide quando fechar um mapeamento como baseline (botão explícito, ex. "Vou reposicionar
o roteador/mesh") e abrir um novo mapeamento vinculado para comparar depois. Não há detecção
automática de troca de posição do roteador (BSSID/RSSI) nesta versão — reduz complexidade e falsos
positivos. Ver seção 8 (fora de escopo) para essa possibilidade futura.

### 3.4 Sessão de mapeamento é persistida

Cada mapeamento é uma sessão com N marcadores medidos, persistida localmente (Room), seguindo o
precedente arquitetural de sessão pai + itens filhos já usado em `ChatSessionEntity` +
`ChatMessageEntity` (FK com cascade, índice composto por ordem cronológica). Isso permite: retomar
um mapeamento incompleto, revisitar mapeamentos antigos, e vincular duas sessões como
baseline→comparação para o fluxo Antes×Depois.

## 4. Personas e casos de uso

- **Pessoa com Wi-Fi fraco em cômodos específicos** — quer entender onde exatamente o sinal cai
  antes de decidir comprar um repetidor/mesh ou mudar o roteador de lugar.
- **Pessoa que já comprou um mesh ou vai reposicionar o roteador** — quer confirmar, com dado e não
  achismo, se a mudança realmente melhorou a cobertura e em quais cômodos.
- **Pessoa em mudança de casa/apartamento** — quer decidir onde instalar o roteador antes de
  organizar os móveis, testando 2-3 posições candidatas.

## 5. Histórias de usuário

- Como usuário, quero adicionar um marcador no grid e nomeá-lo com o cômodo onde estou, para que a
  medição de sinal feita ali fique registrada com esse rótulo.
- Como usuário, quero ver a classificação de sinal (mesma categoria já usada hoje: Excelente/Bom/
  Regular/Fraco) em cada marcador do grid, para comparar cômodos visualmente sem precisar decorar
  valores de dBm.
- Como usuário, quero indicar onde o roteador/nó mesh está posicionado no grid, para ter uma
  referência visual de origem do sinal.
- Como usuário, quero marcar "vou reposicionar o roteador" ao final de um mapeamento, para que o
  app saiba que o próximo mapeamento deve ser comparado com este como baseline.
- Como usuário, quero ver um resumo comparando os dois mapeamentos (antes/depois) por cômodo — o
  que melhorou, o que piorou, o que não mudou — para confirmar se a mudança valeu a pena.
- Como usuário, quero retomar um mapeamento que comecei e não terminei, para não perder marcadores
  já medidos se eu sair do app no meio do processo.
- Como usuário, quero ver meus mapeamentos anteriores numa lista, para revisitar o histórico de
  cobertura da minha casa ao longo do tempo.

## 6. Fluxo principal

1. Usuário abre "WiFi Casa" pelo hub Ferramentas (mesma flag/permissão de localização já exigidas
   hoje por `SINAL_WIFI`).
2. Se não há mapeamento em andamento: tela inicial oferece "Começar mapeamento novo" ou, se existir
   histórico, "Ver mapeamentos anteriores".
3. Ao iniciar, usuário posiciona o marcador do roteador no grid (opcional, pode pular).
4. Usuário anda até um cômodo, toca "Adicionar marcador aqui", posiciona no grid e nomeia (sugestões
   de nomes comuns: Sala, Quarto, Cozinha, Escritório — mais campo livre).
5. App faz a mesma amostragem de RSSI já existente (reaproveita `SinalWifiViewModel`) por alguns
   segundos naquele marcador e salva o resultado classificado.
6. Passos 4-5 repetem para quantos cômodos o usuário quiser.
7. Usuário encerra o mapeamento: "Salvar e concluir" (mapeamento fica no histórico) ou "Vou
   reposicionar o roteador" (fecha como baseline e sinaliza intenção de comparação).
8. Se havia um baseline aberto, ao concluir o novo mapeamento o app oferece automaticamente a tela
   de comparação Antes×Depois, marcador a marcador (por posição/rótulo correspondente, quando o
   rótulo bate; marcadores sem correspondência aparecem como "novo" ou "removido").

## 7. Requisitos funcionais

- **RF-01**: O usuário deve poder criar um novo mapeamento com nome opcional (padrão: data/hora).
- **RF-02**: O usuário deve poder adicionar um marcador no grid 2D em qualquer posição livre,
  associado a um rótulo de texto (obrigatório) e a uma medição de sinal Wi-Fi feita no momento da
  adição.
- **RF-03**: A medição de cada marcador deve reutilizar o motor de classificação existente
  (`MetricClassifier`/`signalQuality`), sem criar um segundo vocabulário ou threshold.
- **RF-04**: O usuário deve poder marcar a posição do roteador/nó mesh principal no grid,
  opcionalmente.
- **RF-05**: O usuário deve poder editar ou remover um marcador antes de concluir o mapeamento.
- **RF-06**: O usuário deve poder concluir um mapeamento normalmente (fica salvo no histórico) ou
  concluir sinalizando "vou reposicionar o roteador" (fica marcado como baseline aguardando
  comparação).
- **RF-07**: Ao concluir um mapeamento com um baseline pendente vinculável (mesma rede/`networkId`),
  o app deve oferecer a tela de comparação Antes×Depois.
- **RF-08**: A comparação Antes×Depois deve mostrar, por marcador correspondente (mesmo rótulo),
  categoria de sinal antes vs. depois e uma indicação textual (melhorou/piorou/não mudou), sem
  inventar correspondência entre marcadores com rótulos diferentes.
- **RF-09**: O usuário deve poder retomar um mapeamento incompleto após fechar o app.
- **RF-10**: O usuário deve poder ver a lista de mapeamentos anteriores e reabrir cada um em modo
  leitura.
- **RF-11**: Mapeamentos e marcadores são persistidos localmente (Room), sem envio a nuvem, seguindo
  o mesmo princípio de privacidade já aplicado ao histórico de medições.
- **RF-12**: A ferramenta deve exibir "WiFi Casa" como nome no hub e na tela, mantendo
  `TipoFerramenta.SINAL_WIFI` como identificador técnico.

## 8. Requisitos não funcionais

- A amostragem de RSSI por marcador deve seguir o mesmo padrão de ciclo de vida já existente
  (`repeatOnLifecycle(RESUMED)`), sem manter polling em background entre marcadores.
- O grid 2D é um componente novo no app (primeiro caso de canvas de posicionamento livre) — deve
  seguir tokens de Design System (`LkTokens`) e suportar acessibilidade (Dynamic Type/TalkBack:
  marcadores precisam ter alternativa não-visual, ex. lista equivalente ao grid).
- Escrita/leitura de sessão de mapeamento não deve bloquear a thread principal (mesmo padrão Room
  assíncrono já usado nos demais DAOs do módulo).
- Nenhuma dependência nova paga ou serviço externo é introduzida por este recurso.

## 9. Critérios de aceite

- Usuário consegue criar um mapeamento, adicionar ao menos 2 marcadores rotulados com medição real
  de RSSI, e ver a classificação de sinal de cada um.
- Usuário consegue fechar um mapeamento como baseline, criar um segundo mapeamento, e ver a
  comparação Antes×Depois com pelo menos um marcador correspondente por rótulo.
- Fechar o app no meio de um mapeamento e reabrir restaura os marcadores já salvos.
- Hub de Ferramentas exibe "WiFi Casa" no lugar de "Encontrar um bom lugar", sem quebrar testes
  existentes de listagem (`FerramentasScreenTest.kt`, `AppShellRootRegistryTest.kt` — a atualizar).
- Nenhuma alteração no motor de classificação de sinal existente (`MetricClassifier`,
  `WifiSignalQualityEngine`) — o WiFi Casa consome, não duplica.

## 10. Fora de escopo (nesta versão)

- Importar ou desenhar planta real da casa (grid é livre, sem escala).
- Detecção automática de reposicionamento de roteador via BSSID/RSSI.
- Recomendação automática de "onde posicionar o roteador" gerada por IA a partir do grid.
- Comparação entre mapeamentos de redes diferentes (`networkId` diferente).
- Exportar ou compartilhar o mapeamento.

## 11. Métricas de sucesso

- Taxa de conclusão de mapeamentos iniciados (não abandonados no primeiro marcador).
- Proporção de mapeamentos que chegam a ter um Antes×Depois comparado.
- Recorrência: usuários que abrem "WiFi Casa" mais de uma vez em 30 dias.
