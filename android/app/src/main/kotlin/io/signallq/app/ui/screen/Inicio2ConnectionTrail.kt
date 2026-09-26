package io.signallq.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.signallq.app.core.diagnostico.EstagioRede
import io.signallq.app.core.network.EstadoConexao
import io.signallq.app.core.network.SnapshotRede
import io.signallq.app.core.network.contracts.topologia.NivelConfianca
import io.signallq.app.core.network.contracts.topologia.PapelTopologia
import io.signallq.app.core.network.topologia.engine.TopologiaRedeEngine
import io.signallq.app.core.network.wifi.EstadoScanWifi
import io.signallq.app.core.network.wifi.SnapshotScanWifi
import io.signallq.app.ui.LkSpacing
import io.signallq.app.ui.component.SignallQFeedbackTone
import io.signallq.app.ui.component.accessibleLabel

internal data class Inicio2TrailNode(
    val id: String,
    val label: String,
    val detail: String,
    // Decisão de implementação (Davi, dentro do espaço aberto pela seção 5 do Architecture
    // Plan): nulo em vez de um `Neutral` sempre presente -- distingue "nó não avaliado pela
    // distinção Wi-Fi/Provedor" (Mesh, Este aparelho, Internet fora do modo Wi-Fi, e
    // qualquer nó antes da 1ª leitura) de "nó avaliado e com tom Neutro". Só o segundo caso
    // deve renderizar badge (seção 5: "nós sem entrada no mapa de status... não ganham
    // badge"); um default `Neutral` sempre visível violaria essa regra. Default `null`
    // preserva os 2 `@Preview` existentes tão bem quanto `Neutral` preservaria (nenhum dos
    // dois passa este parâmetro).
    val tom: SignallQFeedbackTone? = null,
)

internal data class Inicio2ConnectionTrailState(
    val nodes: List<Inicio2TrailNode>,
    val supportingMessage: String?,
)

/**
 * Contrato entre o coordenador de polling (`:app`) e a trilha/Hero da Home — Architecture
 * Plan "Status de conectividade ao vivo na Home", seção 5. Propagado do mesmo
 * `List<StatusEstagio>` que `ClassificadorConectividadeAoVivo` (`:core:diagnostico`) já
 * produz — não é uma sondagem nova.
 *
 * Público (não `internal`) -- atravessa a fronteira pública `AppShellConectividadeAoVivoState`/
 * `StatusConectividadeAoVivoCoordinator` (Kotlin proíbe API pública expor tipo `internal`).
 */
data class Inicio2StatusAoVivo(
    val porEstagio: Map<String, SignallQFeedbackTone>,
    val geral: SignallQFeedbackTone,
    // null quando o pior tom não tem estágio único atribuível (geral == Incerto, ou dois
    // estágios empatados no mesmo tom pior) -- nunca inventar causa combinada.
    val causaPrincipal: EstagioRede?,
)

internal object Inicio2ConnectionTrailMapper {
    fun map(
        snapshotRede: SnapshotRede,
        snapshotWifi: SnapshotScanWifi,
        temPermissaoLocalizacao: Boolean,
        temConfirmacaoRoteadorCentral: Boolean = false,
        ispName: String? = null,
        equipmentName: String? = null,
        deviceName: String? = null,
        // Decisão 4.5 do Architecture Plan: granularidade Wi-Fi/Provedor é exclusiva do modo
        // Wi-Fi -- nós fora dele nunca recebem badge, então este parâmetro só é consultado
        // dentro do ramo Wi-Fi abaixo.
        statusAoVivo: Inicio2StatusAoVivo? = null,
    ): Inicio2ConnectionTrailState {
        if (snapshotRede.estadoConexao != EstadoConexao.wifi) {
            return mapSemWifi(snapshotRede.estadoConexao, ispName, equipmentName, deviceName)
        }
        val classificacoes =
            if (temPermissaoLocalizacao && snapshotWifi.estado == EstadoScanWifi.concluido) {
                TopologiaRedeEngine.classificar(
                    redes = snapshotWifi.redes,
                    connectedBssid = snapshotRede.wifiLinkSnapshot?.bssid,
                    temConfirmacaoRoteadorCentral = temConfirmacaoRoteadorCentral,
                )
            } else {
                emptyList()
            }
        val meshConfirmado =
            temConfirmacaoRoteadorCentral &&
                classificacoes.any { (_, classificacao) ->
                    classificacao.papelProvavel == PapelTopologia.NO_MESH &&
                        classificacao.confianca == NivelConfianca.ALTA &&
                        classificacao.conflitos.isEmpty()
                }
        val porEstagio = statusAoVivo?.porEstagio.orEmpty()

        fun tomDoNo(id: String): SignallQFeedbackTone? = porEstagio[id]
        val nodes =
            buildList {
                add(
                    Inicio2TrailNode(
                        "Internet",
                        ispName?.takeIf { it.isNotBlank() } ?: "Internet",
                        "Conectada",
                        tom = tomDoNo("Internet"),
                    ),
                )
                add(
                    Inicio2TrailNode(
                        "Equipamento",
                        equipmentName?.takeIf { it.isNotBlank() } ?: "Equipamento principal",
                        "Roteador ou modem",
                        tom = tomDoNo("Equipamento"),
                    ),
                )
                // Mesh nunca ganha badge (Architecture Plan, seção 5) -- a distinção Wi-Fi/
                // Provedor não avalia nós mesh individualmente, só o enlace local como um todo.
                if (meshConfirmado) add(Inicio2TrailNode("Mesh", "Nó mesh da sala", "Nó confirmado pela topologia"))
                add(
                    Inicio2TrailNode(
                        "Wi-Fi",
                        snapshotRede.wifiLinkSnapshot?.ssid?.takeIf { it.isNotBlank() } ?: "Wi-Fi",
                        "Rede local",
                        tom = tomDoNo("Wi-Fi"),
                    ),
                )
                // "Este aparelho" nunca ganha badge -- não é um estágio sondado.
                add(Inicio2TrailNode("Este aparelho", deviceName?.takeIf { it.isNotBlank() } ?: "Este aparelho", "Conectado por Wi-Fi"))
            }
        val supportingMessage =
            when {
                !temPermissaoLocalizacao -> "Permita redes próximas para completar a trilha."
                snapshotWifi.estado == EstadoScanWifi.scanning -> "Atualizando os detalhes da rede…"
                snapshotWifi.estado == EstadoScanWifi.erro -> "Alguns detalhes da rede não estão disponíveis."
                else -> null
            }
        return Inicio2ConnectionTrailState(nodes, supportingMessage)
    }

    private fun mapSemWifi(
        estado: EstadoConexao,
        ispName: String?,
        equipmentName: String?,
        deviceName: String?,
    ): Inicio2ConnectionTrailState =
        when (estado) {
            EstadoConexao.movel ->
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Internet", ispName?.takeIf { it.isNotBlank() } ?: "Internet", "Conectada"),
                            Inicio2TrailNode("Rede móvel", "Dados móveis ativos", "Dados móveis ativos"),
                            Inicio2TrailNode("Este aparelho", deviceName?.takeIf { it.isNotBlank() } ?: "Este aparelho", "Conectado pela rede móvel"),
                        ),
                    supportingMessage = null,
                )
            EstadoConexao.ethernet ->
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Internet", ispName?.takeIf { it.isNotBlank() } ?: "Internet", "Conectada"),
                            Inicio2TrailNode("Equipamento", equipmentName?.takeIf { it.isNotBlank() } ?: "Equipamento principal", "Roteador ou modem"),
                            Inicio2TrailNode("Ethernet", "Ethernet", "Rede cabeada"),
                            Inicio2TrailNode("Este aparelho", deviceName?.takeIf { it.isNotBlank() } ?: "Este aparelho", "Conectado por cabo"),
                        ),
                    supportingMessage = null,
                )
            EstadoConexao.desconectado ->
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Internet", "Internet", "Sem acesso"),
                            Inicio2TrailNode("Este aparelho", deviceName?.takeIf { it.isNotBlank() } ?: "Este aparelho", "Sem conexão ativa"),
                        ),
                    supportingMessage = "Conecte-se a uma rede para completar a trilha.",
                )
            EstadoConexao.desconhecido ->
                Inicio2ConnectionTrailState(
                    nodes =
                        listOf(
                            Inicio2TrailNode("Conexão", "Conexão", "Verificando tipo de rede"),
                            Inicio2TrailNode("Este aparelho", deviceName?.takeIf { it.isNotBlank() } ?: "Este aparelho", "Aguardando identificação"),
                        ),
                    supportingMessage = "Aguarde enquanto identificamos a conexão.",
                )
            EstadoConexao.wifi -> error("Wi-Fi é mapeado pelo fluxo principal")
        }
}

@Composable
internal fun Inicio2ConnectionTrail(
    state: Inicio2ConnectionTrailState,
    modifier: Modifier = Modifier,
    onEstagioClick: (String) -> Unit = {},
) {
    val c = io.signallq.app.ui.LocalLkTokens.current
    val nodes = state.nodes.take(5)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LkSpacing.md)) {
        // Altura maior que o antigo 96dp -- comporta a área de toque de 48dp (era 32dp) sem
        // cortar o rótulo abaixo do ícone (Architecture Plan, seção 5, requisito de Breno).
        Box(modifier = Modifier.fillMaxWidth().height(112.dp)) {
            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.TopCenter),
            ) {
                val nodeCount = nodes.size
                repeat((nodeCount - 1).coerceAtLeast(0)) { index ->
                    val currentCenter = size.width * (index + 0.5f) / nodeCount
                    val nextCenter = size.width * (index + 1.5f) / nodeCount
                    drawLine(
                        color = c.outlineVariant,
                        start =
                            androidx.compose.ui.geometry.Offset(
                                currentCenter + 20.dp.toPx(),
                                size.height / 2,
                            ),
                        end =
                            androidx.compose.ui.geometry.Offset(
                                nextCenter - 20.dp.toPx(),
                                size.height / 2,
                            ),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                nodes.forEach { node ->
                    Inicio2TrailItem(
                        node = node,
                        color = c,
                        modifier = Modifier.weight(1f),
                        onClick = { onEstagioClick(node.id) },
                    )
                }
            }
        }
        state.supportingMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun Inicio2TrailItem(
    node: Inicio2TrailNode,
    color: io.signallq.app.ui.LkTokens,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val icon =
        when {
            node.id == "Internet" -> Icons.Outlined.Public
            node.id == "Equipamento" -> Icons.Outlined.Router
            node.id == "Mesh" || node.id == "Nó mesh" -> Icons.Outlined.Hub
            node.id == "Wi-Fi" -> Icons.Outlined.Wifi
            node.id == "Este aparelho" -> Icons.Outlined.Smartphone
            else -> Icons.Outlined.Wifi
        }
    // Só nós com estágio avaliado (badge visível) abrem a sheet de explicação -- Mesh, Este
    // aparelho e o nó "Internet" fora do Wi-Fi não têm o que explicar (sem badge, sem toque).
    val descricaoAcessivel =
        node.tom?.let { tom -> "${node.label}: ${tom.accessibleLabel()}. Toque para ver detalhes." }
    val clicavelModifier =
        if (descricaoAcessivel != null) {
            Modifier.clickable(
                onClickLabel = descricaoAcessivel,
                role = Role.Button,
                onClick = onClick,
            )
        } else {
            Modifier
        }
    Column(
        modifier =
            modifier
                .padding(horizontal = LkSpacing.xs)
                .then(clicavelModifier)
                .semantics(mergeDescendants = true) {
                    if (descricaoAcessivel != null) contentDescription = descricaoAcessivel
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LkSpacing.xs),
    ) {
        Box(
            // Área de toque mínima de 48dp (era LkSpacing.xxl/32dp) -- Architecture Plan,
            // seção 5, requisito de acessibilidade confirmado por Breno em 2026-09-26.
            modifier = Modifier.size(LkSpacing.compositionLarge),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = color.textSecondary, modifier = Modifier.size(LkSpacing.lg))
            node.tom?.let { tom ->
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(LkSpacing.lg),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tom.corDeFundoBadge(color),
                        contentColor = tom.corDeConteudoBadge(color),
                    ) {
                        Icon(
                            imageVector = tom.iconeBadge(),
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp).size(LkSpacing.md),
                        )
                    }
                }
            }
        }
        Text(
            text = node.label,
            style = MaterialTheme.typography.labelSmall,
            color = color.textPrimary,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

/** Cor de fundo do badge de estágio sobreposto ao ícone da trilha (não confundir com
 *  [SignallQFeedbackTone.cor] do Hero, que pinta o círculo inteiro). */
private fun SignallQFeedbackTone.corDeFundoBadge(c: io.signallq.app.ui.LkTokens): androidx.compose.ui.graphics.Color =
    when (this) {
        SignallQFeedbackTone.Success -> c.successContainer
        SignallQFeedbackTone.Warning -> c.warningContainer
        SignallQFeedbackTone.Error -> c.errorContainer
        SignallQFeedbackTone.Neutral -> c.surfaceContainerHigh
        SignallQFeedbackTone.Incerto -> c.surfaceContainerHigh
    }

private fun SignallQFeedbackTone.corDeConteudoBadge(c: io.signallq.app.ui.LkTokens): androidx.compose.ui.graphics.Color =
    when (this) {
        SignallQFeedbackTone.Success -> c.onSuccessContainer
        SignallQFeedbackTone.Warning -> c.onWarningContainer
        SignallQFeedbackTone.Error -> c.onErrorContainer
        SignallQFeedbackTone.Neutral -> c.onSurfaceVariant
        SignallQFeedbackTone.Incerto -> c.onSurfaceVariant
    }

private fun SignallQFeedbackTone.iconeBadge(): androidx.compose.ui.graphics.vector.ImageVector =
    when (this) {
        SignallQFeedbackTone.Success -> Icons.Outlined.CheckCircle
        SignallQFeedbackTone.Warning -> Icons.Outlined.WarningAmber
        SignallQFeedbackTone.Error -> Icons.Outlined.ErrorOutline
        SignallQFeedbackTone.Neutral -> Icons.Outlined.Info
        SignallQFeedbackTone.Incerto -> Icons.Outlined.HelpOutline
    }
