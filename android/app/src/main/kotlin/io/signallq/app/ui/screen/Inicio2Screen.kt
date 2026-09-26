package io.signallq.app.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.signallq.app.core.diagnostico.EstagioRede
import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.ui.LkSpacing
import io.signallq.app.ui.LocalLkTokens
import io.signallq.app.ui.SignallQTheme
import io.signallq.app.ui.component.BetaBadge
import io.signallq.app.ui.component.SignallQButton
import io.signallq.app.ui.component.SignallQFeedbackTone
import io.signallq.app.ui.component.SignallQTopAppBar

@Composable
internal fun Inicio2Screen(
    uiState: Inicio2UiState,
    onAnalisarConexao: () -> Long?,
    onAbrirPerfil: () -> Unit,
    onAlternarTema: () -> Unit = {},
    connectionTrail: Inicio2ConnectionTrailState? = null,
    onAbrirVideos: () -> Unit = {},
    // Architecture Plan "Status de conectividade ao vivo na Home", seções 4.4/5/6 — vem do
    // StatusConectividadeAoVivoCoordinator via AppShell, null antes da 1ª leitura ou quando
    // não em Wi-Fi (decisão 4.5, sem alteração de escopo pra móvel/ethernet).
    statusAoVivo: Inicio2StatusAoVivo? = null,
    diagnosticoBruto: ConnectivityDiagnosis? = null,
) {
    val c = LocalLkTokens.current
    var estagioSelecionado by remember { mutableStateOf<String?>(null) }
    var geracaoSolicitada by remember { mutableStateOf<Long?>(null) }
    val iniciarDiagnostico = {
        if (geracaoSolicitada == null) {
            geracaoSolicitada = onAnalisarConexao()
        }
    }
    LaunchedEffect(uiState.geracaoDiagnostico, uiState.analise) {
        val geracao = geracaoSolicitada
        if (geracao != null && uiState.geracaoDiagnostico == geracao && uiState.analise !is Inicio2Analise.Carregando) {
            geracaoSolicitada = null
        }
    }
    Scaffold(
        containerColor = c.bgPrimary,
        topBar = {
            SignallQTopAppBar(
                title = "Início",
                actions = {
                    IconButton(onClick = onAbrirPerfil) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Abrir ajustes")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.xxl),
        ) {
            connectionTrail?.let {
                Column(
                    modifier = Modifier.padding(horizontal = LkSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(LkSpacing.sm),
                ) {
                    Inicio2ConnectionTrail(
                        state = it,
                        onEstagioClick = { nodeId -> estagioSelecionado = nodeId },
                    )
                }
            }
            Inicio2Hero(
                uiState = uiState,
                loading = uiState.analise is Inicio2Analise.Carregando || geracaoSolicitada != null,
                mostrarConexao = connectionTrail == null,
                onIniciarDiagnostico = iniciarDiagnostico,
                statusAoVivo = statusAoVivo,
            )
            Column(
                modifier = Modifier.padding(horizontal = LkSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(LkSpacing.sm),
            ) {
                Inicio2AtalhoProblema(
                    titulo = "Vídeos ou chamadas travam",
                    descricao = "Interrupções, áudio cortando ou imagem congelada",
                    icon = Icons.Outlined.Wifi,
                    onClick = onAbrirVideos,
                )
            }
        }
    }

    val nodeSelecionado = connectionTrail?.nodes?.firstOrNull { it.id == estagioSelecionado }
    val estagioDoNodeSelecionado =
        when (estagioSelecionado) {
            "Equipamento", "Wi-Fi" -> EstagioRede.WIFI
            "Internet" -> EstagioRede.PROVEDOR
            else -> null
        }
    if (nodeSelecionado != null && nodeSelecionado.tom != null && estagioDoNodeSelecionado != null) {
        Inicio2EstagioDetalheSheet(
            label = nodeSelecionado.label,
            estagio = estagioDoNodeSelecionado,
            tom = nodeSelecionado.tom,
            diagnostico = diagnosticoBruto,
            onDismiss = { estagioSelecionado = null },
        )
    }
}

/** Título/mensagem/tom/glifo do círculo central do Hero -- `glifo == null` preserva o "!" de
 *  texto legado (caminho não tocado por esta fatia: Carregando/Interrompida/fora do Wi-Fi). */
private data class Inicio2HeroCopy(
    val titulo: String,
    val mensagem: String,
    val tone: SignallQFeedbackTone,
    val glifo: androidx.compose.ui.graphics.vector.ImageVector? = null,
)

@Composable
private fun Inicio2Hero(
    uiState: Inicio2UiState,
    loading: Boolean,
    mostrarConexao: Boolean,
    onIniciarDiagnostico: () -> Unit,
    statusAoVivo: Inicio2StatusAoVivo? = null,
) {
    val c = LocalLkTokens.current
    // Decisão 4.4 do Architecture Plan: Hero deriva do status ambiente só quando NÃO há
    // diagnóstico pesado em andamento (Carregando/Interrompida continuam com o tratamento
    // atual, dedicado ao fluxo "Analisar minha conexão") e só em Wi-Fi -- mobile/ethernet
    // continuam 100% com MonitorConexaoLeveUseCase (decisão 4.5, não-objetivo desta fatia).
    val podeUsarStatusAmbiente =
        uiState.conexao == Inicio2Conexao.Wifi &&
            (uiState.analise is Inicio2Analise.StatusEmTempoReal || uiState.analise is Inicio2Analise.SemAnalise)
    // detekt (DestructuringDeclarationWithTooManyEntries) limita a 3 componentes -- Inicio2HeroCopy
    // tem 4 campos, então o `when` é atribuído a uma variável e os campos lidos por nome.
    val heroCopy =
        when {
            podeUsarStatusAmbiente && statusAoVivo != null -> copyStatusAmbiente(statusAoVivo)
            // Antes da 1ª leitura do coordenador, já em Wi-Fi (linha "Neutro" da tabela de
            // copy, decisão 4.4) -- ainda não é staleness porque nunca houve leitura anterior.
            podeUsarStatusAmbiente ->
                Inicio2HeroCopy(
                    "Verificando sua rede",
                    "Conferindo Wi-Fi e provedor agora.",
                    SignallQFeedbackTone.Neutral,
                    Icons.Outlined.Info,
                )
            else -> copyLegado(uiState.analise)
        }
    val titulo = heroCopy.titulo
    val mensagem = heroCopy.mensagem
    val tone = heroCopy.tone
    val glifo = heroCopy.glifo
    val (connectionLabel, connectionIcon) =
        when (uiState.conexao) {
            Inicio2Conexao.Wifi -> "Wi-Fi conectado" to Icons.Outlined.Wifi
            Inicio2Conexao.Movel -> "Dados móveis ativos" to Icons.Outlined.Wifi
            Inicio2Conexao.Ethernet -> "Rede cabeada conectada" to Icons.Outlined.Wifi
            Inicio2Conexao.Offline -> "Sem internet" to Icons.Outlined.WarningAmber
            Inicio2Conexao.Carregando -> "Verificando conexão" to Icons.Outlined.Wifi
        }
    val cor = tone.cor(c)
    Column(
        modifier = Modifier.padding(horizontal = LkSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LkSpacing.md),
    ) {
        if (mostrarConexao) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(connectionIcon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(LkSpacing.sm))
                Text(connectionLabel, style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
            }
        }
        Box(
            modifier =
                Modifier
                    .size(112.dp)
                    .border(8.dp, cor.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (glifo != null) {
                Icon(glifo, contentDescription = null, tint = cor, modifier = Modifier.size(48.dp))
            } else {
                Text("!", color = cor, fontSize = 52.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        Text(
            text = titulo,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = c.textPrimary,
        )
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = c.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            BetaBadge()
        }
        SignallQButton(
            label = "Analisar minha conexão",
            onClick = onIniciarDiagnostico,
            modifier = Modifier.fillMaxWidth(),
            loading = loading,
        )
    }
}

@Composable
private fun Inicio2AtalhoProblema(
    titulo: String,
    descricao: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val c = LocalLkTokens.current
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = c.textSecondary)
            Spacer(Modifier.width(LkSpacing.md))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LkSpacing.xs)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, textAlign = TextAlign.Start)
                Text(descricao, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Start)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = c.textSecondary)
        }
    }
}

private fun SignallQFeedbackTone.cor(c: io.signallq.app.ui.LkTokens): Color =
    when (this) {
        SignallQFeedbackTone.Success -> c.success
        SignallQFeedbackTone.Warning -> c.warning
        SignallQFeedbackTone.Error -> c.error
        SignallQFeedbackTone.Neutral -> c.primary
        SignallQFeedbackTone.Incerto -> c.onSurfaceVariant
    }

/** Copy legado do círculo central (caminho não tocado por esta fatia: Carregando/Interrompida/
 *  SemAnalise-Wi-Fi-antes-do-coordenador/fora do Wi-Fi) -- extraído do corpo de [Inicio2Hero]
 *  só para reduzir a complexidade ciclomática do Composable, sem mudar comportamento. */
private fun copyLegado(analise: Inicio2Analise): Inicio2HeroCopy =
    when (analise) {
        is Inicio2Analise.StatusEmTempoReal ->
            Inicio2HeroCopy(
                tituloConexao(analise.veredito),
                analise.motivo,
                analise.veredito.feedbackTone(),
            )
        Inicio2Analise.SemAnalise ->
            Inicio2HeroCopy(
                "Internet lenta",
                "Vídeos em HD e chamadas podem travar agora.",
                SignallQFeedbackTone.Neutral,
            )
        Inicio2Analise.Carregando ->
            Inicio2HeroCopy(
                "Analisando sua conexão",
                "Estamos reunindo evidências da rede.",
                SignallQFeedbackTone.Neutral,
            )
        is Inicio2Analise.Interrompida ->
            Inicio2HeroCopy(
                "Análise interrompida",
                analise.mensagem,
                SignallQFeedbackTone.Error,
            )
    }

/**
 * Copy definitivo do círculo central quando o Hero deriva do status ambiente (Architecture
 * Plan, decisão 4.4, seção 4.4 -- tabela de Cora). `causaPrincipal == null` só é alcançável
 * defensivamente aqui: pelo desenho de `ClassificadorConectividadeAoVivo`, Warning/Error
 * sempre têm exatamente um estágio no pior tom (o outro fica Sucesso/Neutro) -- os únicos
 * empates reais (INTERNET_AVAILABLE, incerto()) caem nos ramos Success/Incerto, que não
 * consultam `causaPrincipal`.
 */
private fun copyStatusAmbiente(status: Inicio2StatusAoVivo): Inicio2HeroCopy =
    when (status.geral) {
        SignallQFeedbackTone.Success ->
            Inicio2HeroCopy(
                "Conexão estável",
                "Wi-Fi e provedor funcionando bem agora.",
                SignallQFeedbackTone.Success,
                Icons.Outlined.CheckCircle,
            )
        SignallQFeedbackTone.Warning ->
            when (status.causaPrincipal) {
                EstagioRede.WIFI ->
                    Inicio2HeroCopy(
                        "Wi-Fi pode estar instável",
                        "O sinal do seu Wi-Fi está oscilando; vídeos e chamadas podem engasgar.",
                        SignallQFeedbackTone.Warning,
                        Icons.Outlined.WarningAmber,
                    )
                EstagioRede.PROVEDOR ->
                    Inicio2HeroCopy(
                        "Provedor com lentidão",
                        "Sua internet externa está mais lenta que o normal.",
                        SignallQFeedbackTone.Warning,
                        Icons.Outlined.WarningAmber,
                    )
                null ->
                    Inicio2HeroCopy(
                        "Sua conexão pode estar instável",
                        "Alguma parte da sua rede está com lentidão.",
                        SignallQFeedbackTone.Warning,
                        Icons.Outlined.WarningAmber,
                    )
            }
        SignallQFeedbackTone.Error ->
            when (status.causaPrincipal) {
                EstagioRede.WIFI ->
                    Inicio2HeroCopy(
                        "Problema no seu Wi-Fi",
                        "Não conseguimos falar com seu roteador. Aproxime-se dele ou reinicie o Wi-Fi.",
                        SignallQFeedbackTone.Error,
                        Icons.Outlined.ErrorOutline,
                    )
                EstagioRede.PROVEDOR ->
                    Inicio2HeroCopy(
                        "Problema no provedor",
                        "Seu Wi-Fi está bem, mas a internet externa não está respondendo.",
                        SignallQFeedbackTone.Error,
                        Icons.Outlined.ErrorOutline,
                    )
                null ->
                    Inicio2HeroCopy(
                        "Encontramos um problema na sua conexão",
                        "Alguma parte da sua rede não está respondendo.",
                        SignallQFeedbackTone.Error,
                        Icons.Outlined.ErrorOutline,
                    )
            }
        SignallQFeedbackTone.Incerto ->
            Inicio2HeroCopy(
                "Não conseguimos confirmar sua conexão",
                "Vamos continuar checando; toque em \"Analisar\" para um diagnóstico completo.",
                SignallQFeedbackTone.Incerto,
                Icons.Outlined.HelpOutline,
            )
        SignallQFeedbackTone.Neutral ->
            Inicio2HeroCopy(
                "Verificando sua rede",
                "Conferindo Wi-Fi e provedor agora.",
                SignallQFeedbackTone.Neutral,
                Icons.Outlined.Info,
            )
    }

internal fun String.feedbackTone(): SignallQFeedbackTone =
    when (this) {
        "Excelente", "Bom" -> SignallQFeedbackTone.Success
        "Regular" -> SignallQFeedbackTone.Warning
        "Fraco" -> SignallQFeedbackTone.Error
        else -> SignallQFeedbackTone.Neutral
    }

internal fun tituloConexao(veredito: String): String =
    when (veredito) {
        "Excelente" -> "Conexão excelente"
        "Bom" -> "Conexão boa"
        "Regular" -> "Conexão regular"
        "Fraco" -> "Conexão fraca"
        else -> "Conexão ${veredito.lowercase()}"
    }

@Preview(name = "Início 2 claro", showBackground = true)
@Composable
private fun Inicio2ScreenPreview() {
    SignallQTheme {
        Inicio2Screen(
            uiState = Inicio2UiState(Inicio2Conexao.Wifi, "Casa", Inicio2Analise.StatusEmTempoReal("Excelente", "Sinal Wi-Fi forte e conexões estáveis.")),
            onAnalisarConexao = { null },
            onAbrirPerfil = {},
            connectionTrail =
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Internet", "Internet", "Conectada"),
                            Inicio2TrailNode("Equipamento", "Equipamento principal", "Roteador ou modem"),
                            Inicio2TrailNode("Wi-Fi", "Casa", "Casa"),
                            Inicio2TrailNode("Este aparelho", "Este aparelho", "Conectado por Wi-Fi"),
                        ),
                    supportingMessage = null,
                ),
        )
    }
}

@Preview(name = "Início 2 escuro 200%", uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun Inicio2ScreenDarkPreview() {
    SignallQTheme {
        Inicio2Screen(
            uiState = Inicio2UiState(Inicio2Conexao.Offline, null, Inicio2Analise.Interrompida("Seu contexto foi preservado.")),
            onAnalisarConexao = { null },
            onAbrirPerfil = {},
            connectionTrail =
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Internet", "Internet", "Sem acesso"),
                            Inicio2TrailNode("Este aparelho", "Este aparelho", "Sem conexão ativa"),
                        ),
                    supportingMessage = "Conecte-se a uma rede para completar a trilha.",
                ),
        )
    }
}
