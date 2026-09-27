# Architecture Plan — trabalho corrente

## WiFi Casa — mapeamento espacial de Wi-Fi (grid 2D + Antes×Depois)

Aprovado por Luiz (produto): `docs_ai/functional/WIFI_CASA_MAPEAMENTO_SPEC.md`. Gate arquitetural
confirmado (AGENTS.md §5): entidade Room nova com FK/relação pai-filho, primeiro componente de
canvas 2D de posicionamento livre do app, edição do hub de Ferramentas.

> Use somente quando o gate arquitetural do `AGENTS.md` for acionado. Camillo mantém este artefato
> curto e proporcional à mudança.

(demais entradas históricas deste arquivo preservadas — ver git log; esta revisão substitui o topo
do arquivo pela fatia corrente. A entrada anterior — "Status de conectividade ao vivo na Home" —
está no histórico do commit `817b603c`)

---

### 1. Problema e comportamento esperado

Evoluir a ferramenta "Encontrar um bom lugar" (`TipoFerramenta.SINAL_WIFI`) — hoje um indicador
Wi-Fi ao vivo, ponto único, sem persistência — para "WiFi Casa": o usuário anda pela casa,
adiciona marcadores nomeados num grid 2D livre (sem planta real, sem triangulação), cada marcador
guarda uma medição real de RSSI classificada pelo motor existente. O conjunto de marcadores forma
uma sessão de mapeamento persistida (Room), retomável e revisitável. O usuário pode fechar um
mapeamento como baseline ("vou reposicionar o roteador") e, ao concluir o próximo, ver uma
comparação Antes×Depois marcador a marcador por rótulo correspondente. Escopo de produto e RFs
estão fechados na spec — este plano não redefine comportamento, só arquitetura.

### 2. Arquitetura atual relevante

**Ferramenta atual (`SinalWifiViewModel.kt`/`SinalWifiScreen.kt`, ambos em `:app`, não num módulo
`:feature:*`).** `SinalWifiViewModel` é uma classe simples criada via `remember{}` no Composable
(mesmo padrão de `ModoGamerViewModel`), sem Hilt, sem persistência — `iniciarAmostragem()` faz
polling de `WifiManager.getConnectionInfo()` a cada 1500ms só com a tela `RESUMED`
(`repeatOnLifecycle`), emitindo `SinalWifiUiState` (rssi, banda, padrão, MU-MIMO). A categoria
textual (Excelente/Bom/Regular/Fraco) usada na tela vem de `signalQuality(rssi, banda)`
(`SinalTopologiaHelpers.kt:162`), que delega a `classificarRssiWifiLocal` — que por sua vez segue o
mesmo threshold canônico de `MetricClassifier.classificarRssiWifi` usado por
`WifiSignalQualityEngine` (ADR-017, issue #1749: fonte única de classificação RSSI Wi-Fi, sem
threshold duplicado). WiFi Casa **não deve criar um terceiro vocabulário** — reaproveita
`signalQuality`/`classificarRssiWifiLocal` para classificar cada marcador.

**Precedente sessão pai + itens filhos (`ChatSessionEntity`/`ChatMessageEntity`,
`core/database/chat/`).** FK simples (`sessionId` → `chat_sessions.id`, `ON DELETE CASCADE`),
índice em `sessionId` e índice composto `(sessionId, createdAtEpochMs)` para ordenação cronológica
por sessão, mais índice solto em `atualizadoEmEpochMs` da sessão para listagem "mais recentes
primeiro". DAOs são consumidos diretamente pelas camadas acima — `core/database` não expõe
Repository (confirmado em `docs_ai/ARQUITETURA/MODULOS/core-database.md`). Esse é o precedente a
seguir para `mapeamento_wifi`/`marcador_mapeamento`.

**`ResolvedorNetworkId` (`core/database/rede/`).** Já resolve um id estável de rede
(BSSID > SSID > operadora móvel) e já foi promovido para `core/database` justamente para ser
compartilhado entre `ConnectionProfile` (`:featureSettings`) e `MedicaoEntity.networkId` (reteste,
issue #1707). É a peça que RF-07 pede ("mesma rede/`networkId`") — reaproveitar, não recriar.

**Schema Room atual.** `SignallQDatabase` está na versão 21, 8 entities, 20 migrations
encadeadas em `CoreDatabaseModulo.kt`, todas aditivas (nenhum `fallbackToDestructiveMigration`).
Convenção observada: cada migração nova é um objeto `Migration(N, N+1)` com KDoc explicando o
racional e garantia de não perda de dado.

**Canvas 2D de posicionamento livre.** Busca por `detectDragGestures`/`Canvas(` no app não
encontrou nenhum componente de posicionamento livre por toque/arraste — `GaugeCircular.kt` usa
`Canvas` só para desenho não-interativo. Confirma o gatilho #2 do gate: é peça genuinamente nova,
sem precedente a reaproveitar.

**Hub de Ferramentas.** `TipoFerramenta.SINAL_WIFI` (`TipoFerramenta.kt`) já existe como id técnico
estável, compartilhado com o card do diagnóstico guiado. `FerramentasScreen.kt:215` tem o texto
hardcoded (`"Encontrar um bom lugar"` / `"Ande pela casa acompanhando o sinal Wi-Fi"`,
`Icons.Outlined.NetworkWifi`) num `when` de `TipoFerramenta.visual()` — ponto único de edição para
a renomeação (RF-12). `screenName()` hoje mapeia `SINAL_WIFI` para `"sinal_wifi"`, mesmo valor de
`SINAL_CANAIS_MOVEL` — como a tela muda de natureza (de indicador ao vivo para fluxo de
mapeamento com telas/sheets), recomendo dar um `screenName()` próprio (`"wifi_casa"`) só para essa
tela, evitando colisão de analytics entre duas telas agora bem diferentes.

### 3. Módulos afetados

- `core/database` (`:coreDatabase`) — 2 entities novas, 1 DAO novo, 1 migração (22).
- `:app` — ViewModel(s) e telas novas do fluxo WiFi Casa, canvas 2D, renomeação no hub.
- `TipoFerramenta.kt`/`FerramentasScreen.kt` (`:app`) — copy e `screenName()`.
- Nenhuma mudança em `core/diagnostico` (`MetricClassifier`/`WifiSignalQualityEngine` não são
  tocados — RF-03 e o critério de aceite correspondente exigem isso).
- `:featureWifi` **não é tocado**: hoje contém só `RedeVizinha`/`GrupoRedeWifi`/
  `MontarResumoWifiUseCase` (varredura de redes vizinhas) — responsabilidade distinta de sessão de
  mapeamento por marcador. Ver seção 4 para a alternativa rejeitada de colocar WiFi Casa lá.

### 4. Decisão proposta e alternativas rejeitadas

**4.1 Onde vive o ViewModel/UI: `:app`, não um módulo `:feature:*` novo nem `:featureWifi`
existente.**

Decisão: manter o padrão já usado por `SinalWifiViewModel`/`SinalWifiScreen`,
`DispositivosScreen`, `SinalScreen` — ferramentas do hub vivem em `:app/ui/screen` (e pacotes
irmãos como `io.signallq.app.sinalwifi`), não em módulos `:feature:*` dedicados. WiFi Casa é
evolução direta dessa mesma ferramenta (mesmo `TipoFerramenta.SINAL_WIFI`, mesma flag/permissão) —
manter no mesmo lugar é a menor mudança que preserva a convenção existente.

Alternativa rejeitada: mover para `:featureWifi`. Rejeitada porque (a) esse módulo hoje é sobre
varredura de redes vizinhas (BSSIDs ao redor), um domínio diferente de "sessão de mapeamento por
marcador"; misturar os dois no mesmo módulo vira gaveta genérica, contra
`.claude/rules/higiene-e-padronizacao-repositorio.md` §5; (b) nenhuma outra ferramenta do hub
equivalente foi promovida para módulo próprio — criar exceção só para WiFi Casa quebraria a
convenção sem ganho arquitetural correspondente. Se o app inteiro migrar as ferramentas do hub
para módulos `:feature:*` no futuro, isso deve ser uma decisão própria (dívida documentada, não
decidida a reboque desta feature).

Consequência de higiene: como `SinalWifiScreen.kt`/`SinalWifiViewModel.kt` continuam existindo
(ainda usados como o "modo ao vivo" reaproveitado por dentro do fluxo — ver 4.3), WiFi Casa nasce
em arquivos **novos e próprios**: `WifiCasaViewModel.kt`, `WifiCasaScreen.kt` (scaffold + roteamento
de sub-telas), mais um arquivo por sub-tela/sheet (grid ativo, lista de mapeamentos, comparação
Antes×Depois) — nunca inchando os arquivos existentes de "Sinal WiFi".

**4.2 Desenho das entidades Room.**

Duas tabelas novas, seguindo o precedente `chat_sessions`/`chat_messages`, em
`core/database/wificasa/`:

```kotlin
@Entity(
    tableName = "mapeamento_wifi",
    indices = [
        Index(value = ["atualizadoEmEpochMs"]),
        Index(value = ["networkId"]),
        Index(value = ["comparadoComSessaoId"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = MapeamentoWifiEntity::class,
            parentColumns = ["id"],
            childColumns = ["comparadoComSessaoId"],
            onDelete = ForeignKey.SET_NULL, // apagar uma sessão não pode apagar a outra do par
        ),
    ],
)
data class MapeamentoWifiEntity(
    @PrimaryKey val id: String,
    val nome: String,
    val networkId: String?,          // ResolvedorNetworkId, no momento da conclusão
    val criadoEmEpochMs: Long,
    val atualizadoEmEpochMs: Long,
    /** em_andamento | concluido | baseline_pendente | baseline_comparado */
    val status: String,
    /** Preenchido só na sessão "depois", apontando pro "antes". Null enquanto solteira. */
    val comparadoComSessaoId: String? = null,
)

@Entity(
    tableName = "marcador_mapeamento",
    foreignKeys = [
        ForeignKey(
            entity = MapeamentoWifiEntity::class,
            parentColumns = ["id"],
            childColumns = ["mapeamentoId"],
            onDelete = ForeignKey.CASCADE, // apagar o mapeamento apaga seus marcadores
        ),
    ],
    indices = [
        Index(value = ["mapeamentoId"]),
        Index(value = ["mapeamentoId", "criadoEmEpochMs"]),
    ],
)
data class MarcadorMapeamentoEntity(
    @PrimaryKey val id: String,
    val mapeamentoId: String,
    val rotulo: String,
    /** comodo | roteador */
    val tipo: String,
    val posX: Float,  // 0f..1f, normalizado — grid livre, sem escala real (spec 3.1)
    val posY: Float,
    val rssiDbm: Int?,     // null = marcador de roteador sem medição, ou medição não concluída
    val bandaWifi: String?, // nome do enum BandaWifi, mesmo padrão já usado em medicao.bandaWifi
    val criadoEmEpochMs: Long,
)
```

Decisões dentro do desenho:

- **`posX`/`posY` normalizados (0f..1f), não pixels/dp.** O grid é declarado pelo usuário sem
  escala (spec 3.1) — normalizar desacopla a posição salva de tamanho de tela/densidade/rotação, e
  a UI multiplica pelo tamanho real do canvas ao renderizar. Evita um `Migration` futura só para
  corrigir posições salvas em unidade de tela.
- **Sem coluna de categoria de sinal persistida.** `rssiDbm` + `bandaWifi` são o dado medido;
  a categoria (Excelente/Bom/...) é *inferência determinística* e deve ser recalculada a partir de
  `signalQuality(rssiDbm, banda)` no momento da leitura/comparação — nunca persistida como string
  solta. Isso é o requisito não-negociável do AGENTS.md §8 ("não duplique thresholds em múltiplos
  lugares"): se o threshold mudar no futuro, mapeamentos antigos são reclassificados
  automaticamente, sem migração de dado.
- **Vínculo baseline→comparação é um campo auto-referenciado em `MapeamentoWifiEntity`
  (`comparadoComSessaoId`), não uma tabela `comparacao` separada.** Alternativa rejeitada: tabela
  `wifi_casa_comparacao(id, baselineId, depoisId, criadoEmEpochMs)`. Rejeitada porque a spec (3.3,
  RF-07) modela isso como um relacionamento 1:1 opcional e unidirecional no tempo — nunca há mais
  de uma comparação por par de sessões, nunca um "histórico de recálculo" da comparação em si (o
  resultado do Antes×Depois é sempre recalculado on-the-fly a partir dos marcadores das duas
  sessões, nunca persistido). Uma tabela própria adicionaria uma terceira entidade e um terceiro
  join sem necessidade real; um campo nullable autorreferenciado é a menor mudança que expressa o
  mesmo contrato. Se um dia o produto pedir "comparar 3+ mapeamentos" ou histórico de recomparações,
  isso justifica revisitar para uma tabela própria — não-objetivo desta versão.
- **`ForeignKey.SET_NULL` em `comparadoComSessaoId`, `CASCADE` em `mapeamentoId` de
  `marcador_mapeamento`.** Apagar um mapeamento não pode apagar o outro lado do par Antes×Depois
  (SET_NULL preserva a sessão irmã, só perde o vínculo) — mas apagar um mapeamento deve apagar
  seus próprios marcadores (CASCADE, mesmo padrão de `chat_messages`).
- **Resolução do vínculo é lógica de aplicação, não trigger de banco.** Ao concluir uma sessão com
  a flag "vou reposicionar o roteador": `UPDATE mapeamento_wifi SET status='baseline_pendente' ...`.
  Ao concluir a *próxima* sessão da mesma `networkId`: DAO busca
  `SELECT * FROM mapeamento_wifi WHERE status='baseline_pendente' AND networkId=:id ORDER BY
  atualizadoEmEpochMs DESC LIMIT 1`; se encontrar, grava `comparadoComSessaoId` na sessão nova e
  atualiza a antiga para `baseline_comparado` — duas escritas simples em transação Room
  (`@Transaction`), sem trigger SQL (mais fácil de testar e de auditar em Kotlin).

**4.3 Reaproveitamento da amostragem — `WifiCasaViewModel` compõe `SinalWifiViewModel`, não
duplica a leitura de RSSI.**

Decisão: `WifiCasaViewModel` (novo) instancia internamente um `SinalWifiViewModel` (o mesmo usado
hoje) para capturar a medição de cada marcador — ao tocar "Adicionar marcador aqui", inicia
`iniciarAmostragem()` do `SinalWifiViewModel` interno por uma janela curta (ex.: até o primeiro
`amostrado && conectado`, ou N ciclos, a definir com Davi/Cora sem virar decisão de arquitetura),
lê o último `SinalWifiUiState` (rssi, banda) e cancela a amostragem — persiste só o resultado final
no marcador. Isso reaproveita 100% da lógica de leitura de `WifiManager`/tratamento de sentinela
RSSI/estado desligado já validada, sem criar um segundo caminho de leitura de RSSI.

Alternativa rejeitada: estender `SinalWifiViewModel` com um modo "capturar e persistir". Rejeitada
porque misturaria responsabilidade de indicador-ao-vivo-sem-estado com responsabilidade de
persistência/sessão — o próprio `SinalWifiViewModel` continua servindo sozinho como o indicador ao
vivo (ele não desaparece, é o motor de leitura reaproveitado, não substituído).

`WifiCasaViewModel` é diferente do `SinalWifiViewModel` num ponto estrutural: precisa sobreviver à
navegação entre "adicionar marcador" → volta pro grid → "adicionar outro marcador" ao longo de
minutos, e falar com o Room. Recomendo `@HiltViewModel` de verdade (não `remember{}`), com o DAO
novo injetado — o estado "em andamento" é reconstruído a partir do Room no `init` (RF-09: retomar
mapeamento incompleto), não guardado só em memória.

**4.4 Componente de canvas 2D — novo, sem dependência externa, lógica de posicionamento separada
da renderização.**

`WifiCasaGridCanvas.kt` (novo, `:app/ui/screen`): `Box`/`Canvas` do Compose Foundation (já usado em
`GaugeCircular.kt`) com `pointerInput` para tap (adicionar marcador na posição tocada) e drag
(reposicionar um marcador existente) — nenhuma biblioteca de canvas/grafo externa, conforme o NFR
da spec ("nenhuma dependência nova"). Marcadores desenhados como composables posicionados via
`offset()` calculado a partir de `posX/posY` normalizado × tamanho do canvas em pixels — mesma
lógica de conversão coordenada normalizada ↔ pixel precisa ser pura e testável isoladamente.

Estratégia de teste: extrair a lógica de posicionamento (clamping em 0f..1f, conversão
normalizado↔pixel, hit-test de "qual marcador está sob o toque") para uma classe/funções puras
sem `Compose`/`Context` (ex.: `WifiCasaGridPosicionamento.kt`), testável com JUnit puro — teste de
caracterização visual em Compose é caro e frágil (o projeto não tem esse padrão hoje) e não é
necessário para validar a lógica de posição. Teste de UI Compose fica restrito a smoke test de que
o gesto dispara o callback certo, não a geometria pixel-a-pixel.

Acessibilidade (NFR da spec): a lista equivalente ao grid (marcador + categoria + rótulo em lista
plana) é uma segunda forma de renderizar o mesmo estado de marcadores — não uma segunda fonte de
verdade; ambas leem do mesmo `WifiCasaUiState`.

**4.5 Renomeação no hub.**

Editar `FerramentasScreen.kt:215` (copy) e, se Davi confirmar na decomposição técnica que a tela
é suficientemente distinta, `TipoFerramenta.screenName()` (novo valor `"wifi_casa"` só para
`SINAL_WIFI`, deixando de compartilhar `"sinal_wifi"` com `SINAL_CANAIS_MOVEL"`) — mudança
analytics, avaliar com Ramon/Breno se telemetria existente depende do valor atual antes de trocar.
`TipoFerramenta.SINAL_WIFI` (id técnico/enum) não muda, conforme RF-12.

### 5. Contratos/schema (Room)

- Migração `21 → 22`, aditiva, seguindo o padrão de `MIGRATION_20_21`: dois `CREATE TABLE IF NOT
  EXISTS` (`mapeamento_wifi`, `marcador_mapeamento`) + `CREATE INDEX IF NOT EXISTS` para os 3
  índices de `mapeamento_wifi` e os 2 de `marcador_mapeamento`. Nenhuma tabela existente é alterada.
- `SignallQDatabase`: `version = 22`, adiciona as 2 entities à lista `entities = [...]`, expõe
  `mapeamentoWifiDao()`.
- Novo DAO único (`MapeamentoWifiDao`, cobre as duas tabelas — mesmo padrão de `ChatSessionDao`
  cobrindo `chat_sessions` + `chat_messages`): `observarMapeamentos()`, `observarMapeamento(id)`,
  `observarMarcadores(mapeamentoId)`, `salvarMapeamento`, `salvarMarcador`, `atualizarMarcador`,
  `apagarMarcador`, `buscarBaselinePendente(networkId)`, `concluirComoBaseline(id, ...)`,
  `vincularComparacao(idDepois, idAntes, ...)` (transação).
- Nenhum contrato de API externa é criado — tudo local (RF-11).

### 6. Fluxo de dados

1. Usuário toca "WiFi Casa" no hub → `WifiCasaViewModel` verifica Room por mapeamento
   `status='em_andamento'` mais recente; se existir, retoma; senão oferece "Começar novo".
2. Ao criar/retomar, resolve `networkId` atual via `ResolvedorNetworkId` (mesma leitura de
   SSID/BSSID já feita hoje via `WifiManager`) e grava/atualiza no `MapeamentoWifiEntity`.
3. "Adicionar marcador aqui": usuário toca uma posição no `WifiCasaGridCanvas` → abre
   nomeação (rótulo) → `WifiCasaViewModel` dispara a amostragem via `SinalWifiViewModel` interno
   (4.3) → ao capturar, grava `MarcadorMapeamentoEntity` (posX/posY/rótulo/rssi/banda) via DAO.
4. Grid observa `observarMarcadores(mapeamentoId)` (Flow) — cada marcador exibe
   `signalQuality(rssiDbm, banda)` calculado na leitura, nunca um valor persistido.
5. "Salvar e concluir" → `status='concluido'`. "Vou reposicionar o roteador" →
   `status='baseline_pendente'`.
6. Ao concluir qualquer mapeamento, `WifiCasaViewModel` chama `buscarBaselinePendente(networkId)`;
   se houver, grava o vínculo (4.2) e navega para a tela de comparação.
7. Tela de comparação lê os marcadores das duas sessões, casa por `rotulo` (normalizado —
   trim + case-insensitive, função pura testável), calcula
   melhorou/piorou/não mudou comparando `signalQuality`/`rssiDbm` de cada par, marca
   sobras de cada lado como "novo"/"removido" (RF-08) — tudo calculado em memória, nada persistido.

### 7. Persistência/migração

Ver seção 5. Sem dado legado a migrar/preservar (feature nova, tabelas novas) — não colide com
nenhuma entidade existente (`medicao`, `chat_*`, `recommendation_history`,
`connectivity_diagnosis_history`, `provider_directory_cache`, `analytics_outbox`,
`apelido_dispositivo`). `networkId` reaproveita o mesmo formato de string de
`ResolvedorNetworkId`/`MedicaoEntity.networkId` (prefixos `wifi-bssid:`/`wifi-ssid:`/`movel:`) —
comparável entre `medicao` e `mapeamento_wifi` se algum dia for útil correlacionar, sem exigir isso
agora (não-objetivo).

### 8. Falhas, timeout e fallback

- Amostragem sem leitura válida (Wi-Fi desligado, sem conexão, timeout da janela de captura): o
  marcador não é salvo até haver uma leitura válida — mesma semântica de "sem leitura ainda" já
  usada por `SinalWifiUiState.amostrado`; nunca salvar `rssiDbm = 0` como estado válido (mesmo
  cuidado do sentinela `-127`/`0` já tratado em `SinalWifiViewModel.amostrar()`).
  UI mostra o mesmo `SignallQStatefulScreen` (Offline/PermissionRequired) já usado hoje.
- Concluir mapeamento sem nenhum marcador: permitido (RF-01 não exige mínimo), mas a comparação
  Antes×Depois exige ao menos um marcador correspondente por rótulo (RF-08/critério de aceite) —
  se não houver nenhum, a tela de comparação declara "sem marcadores correspondentes para
  comparar", nunca inventa correspondência fraca.
- Baseline pendente numa rede diferente da do novo mapeamento: não é oferecida comparação
  automática (RF-07 exige mesma `networkId`) — o baseline permanece `baseline_pendente`
  indefinidamente até aparecer um mapeamento compatível ou ser apagado manualmente pelo usuário.
- App fechado no meio de uma captura de marcador: a captura em andamento é perdida (não há
  marcador parcial persistido), mas os marcadores já salvos e o mapeamento `em_andamento`
  permanecem no Room — retomada normal via RF-09.

### 9. Segurança/privacidade

Tudo local (Room, sem rede) — sem envio a nuvem, mesmo princípio já aplicado ao histórico de
medições (RF-11). Nenhum SSID/BSSID bruto é exibido na UI de WiFi Casa (só usados internamente
para resolver `networkId`, mesmo padrão de sanitização já aplicado em
`connectivity_diagnosis_history`, GH#1512). Nenhuma permissão nova é introduzida — reaproveita a
permissão de localização já exigida hoje por `SINAL_WIFI` para ler RSSI/BSSID.

### 10. Compatibilidade

- `TipoFerramenta.SINAL_WIFI` não muda de valor — nenhuma migração de flag/telemetria/deep link.
- Migração de schema é aditiva; instalações existentes não perdem nenhum dado ao atualizar.
- Testes a atualizar (spec, critério de aceite): `FerramentasScreenTest.kt`,
  `AppShellRootRegistryTest.kt` (texto/ícone novos), mais os testes já existentes de
  `SinalWifiViewModelTest.kt`/`SinalWifiScreenTest.kt` — não devem quebrar, já que
  `SinalWifiViewModel`/`SinalWifiScreen` continuam existindo como estão (reaproveitados por
  composição, não removidos).

### 11. Estratégia de testes

- **Migration test** `Migration21Para22Test` (androidTest, `MigrationTestHelper`), seguindo o
  padrão dos 9 testes de migração já existentes: banco v21 populado → migra → tabelas novas vazias
  e íntegras; instalação nova direto na v22 sem migração.
- **DAO test** `MapeamentoWifiDaoTest` (androidTest): CASCADE de `marcador_mapeamento` ao apagar
  `mapeamento_wifi`; SET_NULL de `comparadoComSessaoId` ao apagar o lado oposto do par; query de
  `buscarBaselinePendente` por `networkId`.
- **Unit test** da lógica de vínculo baseline→comparação e do casamento de marcadores por rótulo
  (função pura, sem Room/Compose) — cobre "sem correspondência" (novo/removido), "mesmo rótulo,
  case/espaço diferente", "sem baseline pendente".
- **Unit test** de `WifiCasaGridPosicionamento` (clamping, conversão normalizado↔pixel, hit-test) —
  sem Compose.
- **Unit test** de `WifiCasaViewModel` reaproveitando o padrão de `SinalWifiViewModelTest.kt`
  (fake `WifiManager`/DAO em memória) para a orquestração de captura-e-persistência.
- Nenhum teste novo em `core/diagnostico` — `MetricClassifier`/`WifiSignalQualityEngine` não mudam
  (critério de aceite da spec).

### 12. Riscos

- **Duração real da captura por marcador não está definida na spec** (RF-02 diz só "medição feita
  no momento da adição"). Definir com Davi/Cora antes de implementar (não é decisão de arquitetura,
  mas afeta UX: captura longa demais frustra o fluxo de andar pela casa).
- **`screenName()` compartilhado hoje entre `SINAL_WIFI` e `SINAL_CANAIS_MOVEL`** — trocar para um
  valor próprio é mudança de contrato de analytics; confirmar com Ramon/Breno se algum dashboard/
  funil depende do valor atual antes de separar.
- **`WifiCasaViewModel` como `@HiltViewModel`** é uma mudança de padrão em relação ao
  `remember{}` do `SinalWifiViewModel`/`ModoGamerViewModel` — justificada pela necessidade de
  sobreviver à navegação multi-tela e falar com Room, mas é a primeira ferramenta do hub a fazer
  isso; Davi deve confirmar que o DI grafo (`:app` Hilt) já suporta injetar o DAO novo sem fricção.
- **Tamanho de `:app`**: WiFi Casa adiciona ~5-8 arquivos novos em `:app/ui/screen` e
  `:app/wificasa` (viewmodel/estado). Nenhum arquivo existente deve crescer significativamente —
  se algum ultrapassar 400 linhas, extrair antes de continuar (regra de higiene §7).

### 12.1 Reconciliação pós-revisão do Breno — forma de obtenção do `WifiCasaViewModel`

Achado do Breno (revisão de `WifiCasaScreen.kt`): a seção 4.3 aprovou `@HiltViewModel` como
*anotação*, mas não decidiu explicitamente a *forma de obtenção* da instância — `WifiCasaScreen.kt`
nasceu usando `viewModel: WifiCasaViewModel = hiltViewModel()` direto no Composable folha, o que
contraria o comentário já documentado em `MainActivity.kt` ("AppShell/Inicio2Screen/
ResultadoVelocidadeScreen são 100% data-driven, sem `hiltViewModel()` em Composables leaf") e em
`StatusConectividadeAoVivoCoordinator.kt`.

**Decisão: manter `hiltViewModel()` em `WifiCasaScreen.kt`, como exceção documentada — não
padronizar para `by viewModels()` na `MainActivity` + parâmetro via `AppShellSinalWifiOverlay`.**

Motivos:

- O app é single-`Activity` com `MainActivity` como único `@AndroidEntryPoint` — `hiltViewModel()`
  dentro de `WifiCasaScreen` resolve para o mesmo `ViewModelStoreOwner` (a própria `MainActivity`)
  que um campo `by viewModels()` resolveria. A diferença entre as duas formas é *onde* a instância é
  obtida, não o ciclo de vida nem o escopo do `ViewModel` — Breno confirmou isso em device real
  (sem crash, sobrevive a rotação/navegação).
- `WifiCasaViewModel` é estruturalmente diferente de `DevicesViewModel`/`SpeedtestViewModel`: esses
  dois são convertidos em dado puro (estado primitivo + callbacks) antes de chegar a qualquer
  Composable — nenhuma tela do hub recebe o objeto `ViewModel` em si (`DispositivosScreen`,
  `SpeedTestScreen` etc. só recebem `SnapshotScanDispositivos`, `SnapshotRede`, lambdas). Passar
  `WifiCasaViewModel` por parâmetro através de `AppShellSinalWifiOverlay`/`AppShell` não
  produziria esse mesmo padrão data-driven — produziria uma terceira variante (objeto `ViewModel`
  inteiro passado por parâmetro), que não é mais "correta" arquiteturalmente do que
  `hiltViewModel()` local, só move o acoplamento para `AppShell`, que já é dívida de tamanho
  conhecida (§4.3 da regra de higiene do repositório). Não há ganho real em forçar essa mudança.
- Já existe precedente parcial de um Composable resolver sua própria instância de `ViewModel`:
  `DiagnosticoOfflineDialog.kt:64` (`viewModel(factory = ...)`, não-Hilt) — instancia
  `DiagnosticoOfflineViewModel` dentro do próprio diálogo porque o fluxo (stepper de 4 etapas) é
  autocontido e não precisa de wiring cross-cutting no nível da Activity. `WifiCasaViewModel` está
  na mesma categoria: fluxo multi-tela autocontido (grid → captura → lista → comparação) que não
  precisa aparecer em `MainActivity`/`AppShell` para nada além de existir.
- Forçar o padrão antigo (threading do `ViewModel` por parâmetro) aumentaria acoplamento de
  `AppShell`/`AppShellSinalWifiOverlay` sem nenhum ganho de testabilidade ou clareza — o
  `WifiCasaViewModel` já é testável isoladamente via Hilt fake modules, como qualquer
  `@HiltViewModel` do app.

**Não é uma licença geral para `hiltViewModel()` no resto do hub.** A exceção vale especificamente
para telas cujo `ViewModel` (a) precisa sobreviver a uma navegação interna multi-tela própria e (b)
fala com persistência (Room) de forma autocontida, sem que a Activity ou o `AppShell` precisem do
seu estado para nenhum outro propósito. `DevicesViewModel`/`SpeedtestViewModel`/
`StatusConectividadeAoVivoCoordinator` continuam no padrão data-driven porque seu estado É
consumido fora da própria tela (notificações, badge da Home, etc.) — outra ferramenta do hub que
quiser repetir esse padrão precisa da mesma revisão do Camillo, não pode citar WiFi Casa como
precedente automático.

Comentários atualizados como registro da exceção: `MainActivity.kt` (junto a
`operadoraDirectoryResolver`) e `StatusConectividadeAoVivoCoordinator.kt` (topo da classe).
Nenhuma mudança de código pedida ao Davi — `WifiCasaScreen.kt:89` permanece como está.

### 13. Não-objetivos (herdados da spec, reafirmados aqui)

- Importar/desenhar planta real, escala real, triangulação.
- Detecção automática de reposicionamento de roteador via BSSID/RSSI.
- Recomendação automática de onde posicionar o roteador via IA.
- Comparação entre mapeamentos de `networkId` diferentes.
- Exportar/compartilhar mapeamento.
- Tabela própria de "comparação" (ver 4.2) — revisitar só se o produto pedir histórico de
  recomparações ou comparação entre 3+ mapeamentos.
- Promover ferramentas do hub para módulos `:feature:*` dedicados como consequência desta feature.
