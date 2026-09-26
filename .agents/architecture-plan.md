# Architecture Plan — trabalho corrente

## Status de conectividade ao vivo na Home (badge de estágio Wi-Fi/Provedor + Hero coerente)

Decidido por Luiz. Gate arquitetural confirmado: cruza `:app` (Home/Hero/trilha), `:core:diagnostico`
e reusa um motor já existente em `:core:network`. Ver seção 9 para o veredito.

> Use somente quando o gate arquitetural do `AGENTS.md` for acionado. Camillo mantém este artefato
> curto e proporcional à mudança.

(demais entradas históricas deste arquivo preservadas — ver git log; esta revisão substitui o topo
do arquivo pela fatia corrente)

---

### 1. Problema e comportamento esperado

Na Home (`Inicio2Screen.kt`), sem o usuário rodar nenhum teste ativo, mostrar se a rede tem
problema e, quando tem, se é no Wi-Fi (rede interna) ou no provedor (rede externa) — via badge
sobreposto a cada ícone da trilha (`Inicio2ConnectionTrail.kt`) e um círculo central do Hero que
fala a mesma linguagem visual, nunca contradizendo a trilha. Toque num ícone com problema abre uma
sheet explicando aquele estágio. Regras não negociáveis (do brief de Luiz): sonda só em primeiro
plano (sem `MonitoramentoWorker`/opt-in), "causa incerta" é estado de primeira classe (nunca força
Wi-Fi/Provedor sem evidência), badge sempre visível (inclusive "tudo ok"), sem staleness (é ao
vivo — só existe "carregando" antes da primeira leitura), fonte única de verdade (Hero deriva da
trilha, nunca dois vereditos independentes), vocabulário único baseado em `SignallQFeedbackTone`
mais um 5º estado "Incerto" (símbolo "?").

### 2. Arquitetura atual relevante (achados de código, não do brief)

**Trilha e Hero hoje.** `Inicio2ConnectionTrailMapper.map()` (`Inicio2ConnectionTrail.kt`) é puro,
sem badge — ícones fixos por `node.id` (`Public`/`Router`/`Hub`/`Wifi`/`Smartphone`), cor única
`textSecondary`, itens não clicáveis. `Inicio2Hero` (`Inicio2Screen.kt:124-216`) tem glifo fixo
`"!"` e tom vindo de `uiState.analise.veredito.feedbackTone()` — que só existe quando `analise` é
`StatusEmTempoReal`, produzido por `MonitorConexaoLeveUseCase.calcularStatus()`
(`core/diagnostico/MonitorConexaoLeveUseCase.kt`), que só olha RSSI/tipo de transporte — **não sabe
nada sobre gateway, DNS ou rota externa**, isto é, não tem evidência nenhuma de "provedor".
Composição acontece em `AppShell.kt:857-886`: `MainViewModel`/`AppShell` já expõem `snapshotRede` e
`snapshotWifi` como estado observável; a trilha e o Hero são recompostos a partir deles.

**`MonitoramentoWorker` (background, opt-in, NÃO TOCAR).** `medirLatenciaHttp()`/
`medirDnsResolveTime()`/`medirRssiWifi()` são métodos privados de uma `CoroutineWorker` com
histerese e notificações, condicionada a `PreferenciasAppRepository.monitoramentoAtivoFlow`
(default `false`). Continua servindo só o monitoramento em background — este plano não lê, não
estende e não duplica essa lógica.

**Achado central — já existe o motor certo, não construído para isto ainda usado.**
`android/core/network/.../connectivity/`: `ConnectivityDiagnosisEngine` + `ConnectivityStatusResolver`
+ `ConnectivityDiagnosisRunner` (GH#1512) já fazem exatamente a distinção Wi-Fi-interno vs.
provedor-externo que o brief pede, com proveniência declarada:

- sondagem sequencial (só avança se a etapa anterior confirmou sucesso): **gateway** (TCP connect
  portas 53/80/443, timeout 1,2s, amarrado à `Network` do Wi-Fi via `AndroidNetworkProbeBinding`,
  nunca à rede default do sistema) → **DNS** → **rota externa** (IP puro 1.1.1.1/8.8.8.8/9.9.9.9 +
  hostname, timeout 1,5s cada, em paralelo);
- `ConnectivityStatus` (`INTERNET_AVAILABLE`, `GATEWAY_UNREACHABLE`, `NO_LOCAL_ADDRESS`,
  `DNS_FAILURE`, `EXTERNAL_ROUTE_FAILURE`, `WIFI_WITHOUT_INTERNET`, `PARTIAL_CONNECTIVITY`,
  `CAPTIVE_PORTAL`, `INCONCLUSIVE`, `WIFI_DISCONNECTED`) já separa camada local (gateway) de camada
  externa (DNS/rota) — mapeia quase 1:1 para "Wi-Fi" vs. "Provedor";
- `NivelConfianca` (ALTA/MEDIA/BAIXA) já existe por decisão de design (nunca trata etapa não
  alcançada/timeout de teto global como evidência forte) — é o material bruto para o 5º estado
  "Incerto" que Luiz pediu, sem inventar um enum de confiança novo;
- custo por sondagem é TCP connect de poucos bytes (SYN), não GET com payload — bem mais barato que
  o GET de 0 bytes ao Cloudflare do `MonitoramentoWorker`, adequado a polling frequente;
- já é `@Singleton` via DI (`AppModule.kt:395-404`, `ConnectivityDiagnosisRunner`/
  `ConnectivityDiagnosisSource`), já testado (`ConnectivityDiagnosisEngineTest`,
  `ConnectivityStatusResolverTest`), já consumido em produção por
  `ConnectivityDiagnosisRepositoryImpl` (`:feature:speedtest/.../connectivity/`) e por
  `DiagnosticoOfflineViewModel` (fluxo guiado, opt-in, só quando já offline — CTA do
  `SignallQOfflineBanner`, não wired à Home, não é polling contínuo).

**Motor local × ADR-017.** ADR-017 substitui o motor pesado (`InternetDiagnosticEngine`/
`MetricClassifier`/`ScoreEngine`) pelo NDS remoto — não se aplica aqui. `MonitorConexaoLeveUseCase`
já é o precedente de um classificador leve, **sempre local, sem NDS**, convivendo com o motor
pesado; este plano estende esse mesmo precedente, não o motor que a ADR-017 está descontinuando.

**Vocabulário visual.** `SignallQFeedbackTone` (`ui/component/SignallQFeedbackTone.kt`) e
`SignallQBadgeTone` (`ui/component/SignallQControls.kt`) são enums de 4 valores
(Neutral/Success/Warning/Error) com ícones canônicos (`Info`/`CheckCircle`/`WarningAmber`/
`ErrorOutline`) e uma função de conversão entre eles já existente (`toBadgeTone()`). Uso confinado a
`:app` (6 arquivos, todos em `ui/component`, `ui/screen/Inicio2Screen.kt` e testes de contrato) —
raio de impacto pequeno e local ao adicionar um 5º valor.

### 3. Módulos afetados

- `:app` — `Inicio2ConnectionTrail.kt` (badge + clicável + sheet), `Inicio2Screen.kt` (Hero glifo/
  tom), `SignallQFeedbackTone.kt`/`SignallQControls.kt` (5º valor "Incerto" + ícone "?"),
  `AppShell.kt`/`MainViewModel.kt` (novo coordenador de polling foreground, novo `StateFlow` de
  estado ao vivo).
- `:core:diagnostico` — novo classificador puro (estágio → tom), ao lado de
  `MonitorConexaoLeveUseCase`.
- `:core:network` — **nenhuma mudança de contrato.** Só reuso de `ConnectivityDiagnosisSource`/
  `ConnectivityDiagnosisEngine`/`ConnectivityStatus`/`NivelConfianca`, já públicos.
- `:core:database`/`ConnectivityDiagnosisHistoryDao` — **não tocar** (ver decisão 4.3).

### 4. Decisão proposta e alternativas rejeitadas

**4.1 Sonda: reusar `ConnectivityDiagnosisEngine`/`ConnectivityDiagnosisSource`, não extrair
`MonitoramentoWorker`.**
Alternativa do brief inicial (extrair `medirLatenciaHttp`/`medirDnsResolveTime`/`medirRssiWifi` do
Worker para um use case novo) é **rejeitada**: duplicaria, com heurística mais pobre (limiar único
de latência/DNS, sem separar gateway de rota externa, sem proveniência), um motor que já existe,
já testado, já mais barato e que já responde exatamente à pergunta "Wi-Fi ou provedor". Extrair do
Worker só faria sentido se nenhum motor equivalente existisse — não é o caso aqui (regra do
inventário/verificar-modulo: preferir a menor mudança, não recriar).

**4.2 Onde vive o classificador de estágio: `:core:diagnostico`, ao lado de
`MonitorConexaoLeveUseCase`.**
Alternativa rejeitada: colocar a regra de mapeamento tom-por-estágio dentro de `:app` (junto do
`Inicio2ConnectionTrailMapper`, que é só apresentação). Rejeitada porque a régua "que `ConnectivityStatus`
+ `NivelConfianca` viram qual tom" é uma regra de negócio de diagnóstico (mesma categoria de
`MonitorConexaoLeveUseCase`), deve ser testável isoladamente sem Compose, e `:core:diagnostico` já
depende de `:coreNetwork` (`build.gradle.kts` linha 53) — sem inversão de dependência nova.
Novo tipo puro proposto:

```kotlin
// io.signallq.app.core.diagnostico
enum class EstagioRede { WIFI, PROVEDOR }

data class StatusEstagio(
    val estagio: EstagioRede,
    val tom: TomDiagnostico, // ver 4.4 — não é o SignallQFeedbackTone do :app
)

object ClassificadorConectividadeAoVivo {
    fun classificar(diagnostico: ConnectivityDiagnosis): List<StatusEstagio>
}
```

`:core:diagnostico` não deve conhecer `SignallQFeedbackTone` (tipo de `:app`) — devolve um enum
próprio equivalente (`TomDiagnostico { NEUTRO, SUCESSO, ATENCAO, ERRO, INCERTO }`), e o mapper de
`:app` (`Inicio2ConnectionTrailMapper`) faz a tradução final para `SignallQFeedbackTone`. Evita
`:core:diagnostico` (módulo de regra pura) depender de um componente de UI de `:app`.

**4.3 Não persistir o polling ambiente no `ConnectivityDiagnosisHistoryDao`.**
`ConnectivityDiagnosisRepositoryImpl.diagnosticar()` grava toda chamada no histórico Room — correto
para eventos discretos (1 diagnóstico por speedtest), errado para um loop de poucos segundos
enquanto a Home estiver visível (inundaria a tabela com ruído ambiente, sem valor de histórico
real). Decisão: o coordenador da Home chama `ConnectivityDiagnosisSource` (a interface, já
`@Singleton` via `ConnectivityDiagnosisRunner`) **diretamente**, não o `ConnectivityDiagnosisRepository`
de `:feature:speedtest`. Nenhuma mudança de schema, nenhuma migration.

**4.4 Hero deriva da trilha; não guarda um segundo veredito — texto incluído (decisão de Cora,
2026-09-26).**
`statusGeral` do Hero = pior caso entre os `StatusEstagio` correntes (ERRO > ATENCAO > INCERTO >
SUCESSO > NEUTRO/carregando) — função pura, um único lugar. Escopo do que muda no Hero: **glifo, tom
E texto (título/mensagem)** do círculo passam a vir do classificador ambiente em vez de
`veredito.feedbackTone()`/`"!"` fixo/`MonitorConexaoLeveUseCase`, e **só quando não há diagnóstico
pesado em andamento** (`uiState.analise` é `SemAnalise` ou `StatusEmTempoReal` — nunca
`Carregando`/`Interrompida`, que continuam com o tratamento atual, dedicado ao fluxo "Analisar minha
conexão") **e só em Wi-Fi com `Inicio2StatusAoVivo` já disponível** (mobile/ethernet e os primeiros
segundos antes da 1ª leitura continuam 100% com `MonitorConexaoLeveUseCase`, sem alteração — decisão
4.5 não muda). Cora rejeitou a alternativa "duas fontes + tabela de precedência" (RSSI alto não é
evidência sobre o provedor — manteria uma segunda fonte de verdade disfarçada, reabrindo o risco que
esta arquitetura existe para fechar).

Copy definitivo (usar exatamente estes textos; jargão técnico como "DNS"/"gateway"/"rota externa"
fica reservado para a sheet por estágio, não para o Hero):

| Tom | Causa (`causaPrincipal`) | Título | Mensagem |
|---|---|---|---|
| Sucesso | — | Conexão estável | Wi-Fi e provedor funcionando bem agora. |
| Atenção | Wi-Fi | Wi-Fi pode estar instável | O sinal do seu Wi-Fi está oscilando; vídeos e chamadas podem engasgar. |
| Atenção | Provedor | Provedor com lentidão | Sua internet externa está mais lenta que o normal. |
| Erro | Wi-Fi | Problema no seu Wi-Fi | Não conseguimos falar com seu roteador. Aproxime-se dele ou reinicie o Wi-Fi. |
| Erro | Provedor | Problema no provedor | Seu Wi-Fi está bem, mas a internet externa não está respondendo. |
| Incerto | null | Não conseguimos confirmar sua conexão | Vamos continuar checando; toque em "Analisar" para um diagnóstico completo. |
| Neutro (antes da 1ª leitura, já em Wi-Fi) | — | Verificando sua rede | Conferindo Wi-Fi e provedor agora. |

Fora de escopo desta decisão (refinamento futuro, não bloqueia Davi): copy dedicado para
`CAPTIVE_PORTAL` — hoje cai em Erro/Atenção Wi-Fi genérico.

**4.5 Trilha Wi-Fi-only para a distinção Wi-Fi/Provedor — mobile/ethernet não ganham essa
granularidade agora.**
`ConnectivityDiagnosisEngine` só sonda quando `wifiConnected = true` (por desenho, GH#1512). A
trilha em modo móvel/ethernet (`mapSemWifi`) não tem hoje um nó "Provedor" separado de "Internet" —
manter assim. Não-objetivo desta fatia: sondar gateway/DNS/rota externa em dados móveis. O nó
"Internet" em modo não-Wi-Fi continua refletindo só conectado/desconectado (como hoje).

### 5. Contrato entre sonda/classificador e UI

```kotlin
// io.signallq.app.ui.screen (novo tipo, ao lado de Inicio2ConnectionTrailState)
data class Inicio2StatusAoVivo(
    val porEstagio: Map<String, SignallQFeedbackTone>, // chave = Inicio2TrailNode.id ("Wi-Fi", "Internet", ...)
    val geral: SignallQFeedbackTone,                    // pior caso, já resolvido
    val causaPrincipal: EstagioRede?, // decisão de Cora: fonte única também para o texto do Hero.
    // Propagado do mesmo List<StatusEstagio> que ClassificadorConectividadeAoVivo já produz — não é
    // sondagem nova. null quando o pior tom não tem estágio único atribuível (ex.: geral == Incerto,
    // ou dois estágios empatados no mesmo tom pior) — nunca inventar causa combinada; tratar como
    // Incerto/null.
)
```

- `Inicio2ConnectionTrailMapper.map(...)` ganha parâmetro opcional `statusAoVivo:
  Inicio2StatusAoVivo? = null`; quando presente, cada `Inicio2TrailNode` carrega o tom
  correspondente (novo campo `tom: SignallQFeedbackTone` no data class, default `Neutral` para não
  quebrar os dois `@Preview` existentes que constroem `Inicio2TrailNode` sem esse campo).
  `null`/ausência de entrada no mapa para um `node.id` = estágio não avaliado (ex.: Mesh, Este
  aparelho) → sem badge, comportamento atual preservado.
- `Inicio2Hero` recebe o mesmo `SignallQFeedbackTone` + `causaPrincipal` (via `uiState` ou parâmetro
  novo) para tom, glifo e texto (título/mensagem via tabela de copy da decisão 4.4).
- Toque no ícone da trilha (novo `onEstagioClick: (String) -> Unit`) abre uma sheet (conteúdo/copy é
  decisão de Cora/Davi, não desta arquitetura) — este plano só define que a sheet recebe o
  `node.id` + tom + (quando disponível) o `ConnectivityDiagnosis` bruto para montar a explicação
  humana, sem duplicar regra de classificação na sheet.
- Área de toque de cada item da trilha (ícone + rótulo) deve ter no mínimo 48dp (padrão Material —
  hoje o `Box` do ícone é 32dp/`LkSpacing.xxl`; usar `Modifier.minimumInteractiveComponentSize()` ou
  padding equivalente; a `Row` com `SpaceBetween`/`weight(1f)` comporta isso sem redesenho —
  confirmado por Breno, 2026-09-26). `contentDescription` no elemento clicável (não no ícone
  isolado, com `mergeDescendants`/`clearAndSetSemantics` para TalkBack anunciar como um único
  elemento), padrão: `"{rótulo do nó}: {tone.accessibleLabel()}. Toque para ver detalhes."` — estender
  `SignallQFeedbackTone.accessibleLabel()` com "Incerto". O ícone do 5º valor "Incerto" deve ser um
  ícone vetorial (`Icons.Outlined.QuestionMark`/`HelpOutline`), não um glifo de texto "?" solto, para
  manter consistência com os demais tons (todos usam ícone vetorial, nunca só cor).

### 6. Fluxo de dados

```
Home visível + app foreground
        ↓ (novo coordenador — MainViewModel ou state holder dedicado em :app)
loop: aguarda conclusão da sondagem anterior → ConnectivityDiagnosisSource.diagnosticar()
      → espera 5s (recomendação de Breno, 2026-09-26 — faixa aceitável 4-6s) → repete
        ↓
ConnectivityDiagnosis (ConnectivityStatus + NivelConfianca)
        ↓
ClassificadorConectividadeAoVivo.classificar() [:core:diagnostico, puro, testável]
        ↓
StatusEstagio (WIFI, PROVEDOR) → tradução para SignallQFeedbackTone [:app]
        ↓
Inicio2StatusAoVivo → Inicio2ConnectionTrailMapper (badges) + Inicio2Hero (círculo, pior caso)
```

Início/parada do loop: inicia quando `AppShellRoot.Home` está selecionada E o processo está em
foreground; para quando qualquer uma das duas condições deixa de valer. Antes da primeira resposta,
`Inicio2StatusAoVivo` é `null` (estado "carregando" — sem badge ou badge neutro de carregamento,
decisão visual de Davi/Cora). Ao voltar a foreground/à Home, reseta para `null` (carregando) até a
próxima leitura — nunca reexibe o último valor como se fosse atual (regra "sem staleness").

### 7. Falhas, timeout e fallback

- Etapas já tipadas (`ProbeResult.Success/Failure/Timeout/NotExecuted/Unavailable`) — o classificador
  nunca trata timeout como sucesso (regra dura do `AGENTS.md` §8) nem inventa dado ausente.
- Teto do próprio engine: 8s globais (`GLOBAL_TIMEOUT_MS_DEFAULT`); coordenador da Home não precisa
  de teto adicional — só evita rodadas sobrepostas (aguarda a rodada terminar antes de agendar a
  próxima, nunca fixed-rate).
- Exceção inesperada de `diagnosticar()` (fora do modelo `ProbeResult`) → capturada pelo coordenador,
  vira `Inicio2StatusAoVivo` com todos os estágios `Incerto`, nunca derruba a Home.
- `NivelConfianca.BAIXA` em qualquer resolução (`ConnectivityStatusResolution.confidence`) força o(s)
  estágio(s) afetado(s) a `Incerto`, mesmo que o `ConnectivityStatus` resolvido sugira uma causa —
  é a implementação direta da regra "nunca apresentar causa raiz sem evidência suficiente".
- `WIFI_DISCONNECTED`/transporte não-Wi-Fi: sem badge de estágio Wi-Fi/Provedor (ver 4.5) — trilha
  cai no modo atual sem essa granularidade.

### 8. Compatibilidade

- `MonitoramentoWorker`, histerese, notificações e preferências de monitoramento: **zero mudança**.
- `ConnectivityDiagnosisRepositoryImpl`/`ConnectivityDiagnosisHistoryDao`/consumo por
  `:feature:speedtest`: **zero mudança** (decisão 4.3 evita qualquer efeito colateral cruzado).
- `MonitorConexaoLeveUseCase`: continua sendo a fonte do título/mensagem textual do Hero; não é
  removido nem substituído nesta fatia.
- `SignallQFeedbackTone`/`SignallQBadgeTone`: adicionar `Incerto` é aditivo, mas **quebra
  exaustividade de `when`** em todo call site existente (6 arquivos, listados na investigação) —
  compilador força a atualização, não há risco de esquecer um branch silenciosamente.
- `Inicio2TrailNode`: adicionar campo `tom` com default preserva os 2 `@Preview` existentes em
  `Inicio2Screen.kt` sem alteração.

### 9. Gate Camillo: decisão

**Aprovado.** Cruza `:app`, `:core:diagnostico` e reusa contrato público de `:core:network` — gate
do §5 se aplica (item 1: múltiplos módulos com mudança de responsabilidade; item 7 tangencial: não é
o motor central de diagnóstico do ADR-017, mas é uma extensão do "motor leve" que convive com ele).
Escopo real é menor do que "criar sonda nova": a maior parte do trabalho é **wiring** de um motor já
maduro (`ConnectivityDiagnosisEngine`, GH#1512) que ninguém tinha ainda ligado à Home, mais um
classificador leve e pequeno, mais extensão de um enum de 4 para 5 valores.

Condições:
1. Não recriar sondagem gateway/DNS/rota externa — usar `ConnectivityDiagnosisSource` como está.
2. Não persistir o polling ambiente no `ConnectivityDiagnosisHistoryDao` (decisão 4.3).
3. `ClassificadorConectividadeAoVivo` não importa tipo de `:app` (decisão 4.2) — mantém a direção de
   dependência `:app → :core:diagnostico → :coreNetwork`.
4. Hero só troca a fonte do tom/glifo do círculo quando não há diagnóstico pesado em andamento
   (decisão 4.4) — nunca dois estados "ativos" ao mesmo tempo tentando pintar o mesmo círculo.
5. Extensão revisada e aprovada (2026-09-26): campo `causaPrincipal: EstagioRede?` em
   `Inicio2StatusAoVivo` (seção 5), para a decisão de Cora de usar fonte única também para o texto do
   Hero. Aditivo dentro do escopo já aprovado — propaga dado já produzido por
   `ClassificadorConectividadeAoVivo`/`List<StatusEstagio>` (seção 4.2), sem sondagem nova, sem módulo
   novo e sem alterar a direção de dependência `:app → :core:diagnostico → :coreNetwork` (condição 3
   continua valendo). Não reabre o gate.

Riscos:
- **Custo de bateria/dados mesmo em foreground.** Menor que o GET do Worker (TCP connect vs. HTTP
  completo). Breno confirmou no código (`ConnectivityDiagnosisEngine`/`GatewayReachabilityProbe`/
  `ExternalIpReachabilityProbe`) que uma rodada em rede saudável é ~4 handshakes TCP curtos sem
  payload (~centenas de ms); pior caso (rede degradada, múltiplas portas/IPs tentados) chega ao teto
  de 8s. Intervalo de espera fixado em **5s** entre rodadas (decisão acima, seção 6) — ciclo total
  ~5,3s em rede saudável (percebido como "ao vivo" para um badge, sem justificar 1-2s que só
  multiplicaria handshakes sem ganho perceptível). Validação em device real (bateria via
  Battery Historian/Profiler, cadência real de chegada, device OEM com otimização agressiva,
  consumo de dados, e necessidade de back-off após timeouts repetidos em rede ruim) fica com Breno,
  **depois da implementação** — não bloqueia o início dela.
- **Falso incerto por excesso de cautela.** Se o critério de `Incerto` (NivelConfianca BAIXA) for
  aplicado com timeout global do engine ainda em andamento (etapa nunca alcançada), o badge pode
  piscar "?" com frequência em redes só um pouco lentas — precisa de teste de caracterização com
  cenários reais de rede lenta (não só rede boa/rede quebrada).
- ~~Descompasso copy-vs-cor no Hero~~ — **resolvido** (decisão de Cora, seção 4.4): texto passa a
  vir da mesma fonte que a cor, não mais de `MonitorConexaoLeveUseCase`, quando o status ao vivo
  está disponível.
- **Toque no ícone da trilha é novo comportamento de interação** (hoje `Inicio2TrailItem` não é
  clicável) — requisitos de acessibilidade definidos por Breno (seção 5: área de toque ≥48dp,
  `contentDescription` padronizado, ícone vetorial para "Incerto"); validação final em device real
  ainda cabe a ele depois da implementação.

Não-objetivos: isto não substitui o diagnóstico completo/NDS (`analisarProblema`/"Analisar minha
conexão" continuam intactos), não é o `ScoreEngine`/motor pesado do ADR-017, não estende a
distinção Wi-Fi/Provedor para dados móveis, não persiste histórico do estado ambiente, não altera
`MonitoramentoWorker`.

**Correção de Ramon ao `ClassificadorConectividadeAoVivo` (2026-09-26, aceita):** `WIFI_WITHOUT_INTERNET`
mapeia para `INCERTO` nos dois estágios, não `ERRO`/`WIFI` como a primeira instrução previa. O próprio
doc-comment do status em `core:network` já o descreve como "conclusão honesta quando não dá para
atribuir a causa a uma camada específica", e o resolver só chega nele depois de gateway e DNS
confirmados — atribuir a causa ao Wi-Fi aqui seria inventar evidência, violando AGENTS.md §8. Efeito
visual: esse status vira badge "?" em vez de erro no ícone de Wi-Fi.

**Segunda correção, achada rodando o app em emulador (2026-09-26, aceita):** `GATEWAY_UNREACHABLE`/
`NO_LOCAL_ADDRESS` marcavam `PROVEDOR = SUCESSO` (erro só no Wi-Fi). Isso também inventava evidência:
a sondagem é sequencial e nunca alcança DNS/rota externa quando o gateway falha, então não há
nenhuma evidência sobre o provedor nesse cenário. Corrigido para `PROVEDOR = NEUTRO` (não avaliado),
via função dedicada `erroWifiSemEvidenciaExterna()` — não reusa mais `erroEm()`/`estagioOposto()`
para esses dois status (essa suposição "oposto = sucesso" só vale para `DNS_FAILURE`/
`EXTERNAL_ROUTE_FAILURE`, onde a etapa oposta foi de fato testada com sucesso antes da falha).
Efeito visual: o nó "Internet" mostra um badge neutro ("Info") em vez de check verde quando o
Wi-Fi está com erro de gateway — sem afetar o Hero (o pior caso continua vindo do Wi-Fi/Erro).

### 10. Estratégia de testes

- `ClassificadorConectividadeAoVivo` (`:core:diagnostico`): testes unitários puros por
  `ConnectivityStatus` × `NivelConfianca` (matriz completa dos 10 valores de status combinados com
  os 3 níveis de confiança) — sem depender de speedtest real, sem Robolectric.
- `Inicio2ConnectionTrailMapper`: estender os testes existentes com `statusAoVivo` presente/ausente,
  confirmando que nós sem entrada no mapa não ganham badge.
- Hero: teste de caracterização confirmando que `Carregando`/`Interrompida` preservam o
  comportamento atual (não usam o tom ambiente) e que `SemAnalise`/`StatusEmTempoReal` passam a usar
  o pior caso da trilha.
- Coordenador de polling (`:app`): teste com `TestDispatcher`/fake `ConnectivityDiagnosisSource`
  confirmando start ao entrar na Home+foreground, stop ao sair/backgroundar, sem sondagens
  sobrepostas.
- Breno: validação em device real — rede boa, Wi-Fi sem internet (gateway ok, sem rota externa),
  Wi-Fi totalmente offline, DNS bloqueado/lento, troca Wi-Fi↔móvel com Home aberta, app indo a
  background no meio de uma sondagem.

### 11. Resumo para implementação (Davi/Ramon)

Ordem sugerida:
1. **Ramon** — `EstagioRede`/`TomDiagnostico`/`StatusEstagio`/`ClassificadorConectividadeAoVivo` em
   `:core:diagnostico`, com testes da matriz completa.
2. **Davi** — 5º valor `Incerto` em `SignallQFeedbackTone`/`SignallQBadgeTone` (+ ícone "?"),
   corrigindo os `when` que deixam de compilar.
3. **Davi** — coordenador de polling foreground em `:app` (liga ao ciclo de vida Home+processo),
   consumindo `ConnectivityDiagnosisSource` diretamente (não o `ConnectivityDiagnosisRepository`).
4. **Davi** — `Inicio2TrailNode`/`Inicio2ConnectionTrailMapper`/`Inicio2ConnectionTrail` (badge +
   clicável), `Inicio2Hero` (glifo/tom condicionados), wiring em `AppShell.kt`.
5. **Davi** — sheet de explicação por estágio (copy definida com Cora).
6. **Breno** — regressão + validação em device real conforme seção 10.
