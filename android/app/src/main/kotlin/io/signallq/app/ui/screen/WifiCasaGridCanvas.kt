package io.signallq.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.signallq.app.ui.LkRadius
import io.signallq.app.ui.LkSpacing
import io.signallq.app.ui.LocalLkTokens
import io.signallq.app.ui.component.signalColor
import io.signallq.app.wificasa.MarcadorUi
import io.signallq.app.wificasa.encontrarMarcadorTocado
import io.signallq.app.wificasa.pixelParaNormalizado

private val TAMANHO_MARCADOR = 40.dp

/**
 * Grid 2D de posicionamento livre do WiFi Casa (`.agents/architecture-plan.md`, seção 4.4) --
 * primeiro componente de canvas de posicionamento livre por toque do app. Toda a lógica de
 * conversão coordenada normalizada↔pixel e hit-test vive em `WifiCasaGridPosicionamento.kt`
 * (função pura, testável sem Compose); este arquivo só desenha e traduz gestos.
 *
 * [onTapGrid] dispara quando o toque não acerta nenhum marcador existente -- normalmente abre o
 * fluxo de adicionar um marcador novo naquela posição. [onTapMarcador] dispara quando o toque
 * acerta um marcador já existente.
 */
@Composable
fun WifiCasaGridCanvas(
    marcadores: List<MarcadorUi>,
    onTapGrid: (posX: Float, posY: Float) -> Unit,
    onTapMarcador: (MarcadorUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalLkTokens.current
    val density = LocalDensity.current
    var tamanhoPx by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(LkRadius.card))
                .background(c.bgSecondary)
                .border(1.dp, c.border, RoundedCornerShape(LkRadius.card))
                .onSizeChanged { tamanhoPx = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(marcadores, tamanhoPx) {
                    detectTapGestures { offset ->
                        val largura = tamanhoPx.width
                        val altura = tamanhoPx.height
                        if (largura <= 0f || altura <= 0f) return@detectTapGestures
                        val ponto = pixelParaNormalizado(offset.x, offset.y, largura, altura)
                        val tocado =
                            encontrarMarcadorTocado(
                                marcadores = marcadores,
                                posX = { it.posX },
                                posY = { it.posY },
                                toqueX = ponto.x,
                                toqueY = ponto.y,
                            )
                        if (tocado != null) onTapMarcador(tocado) else onTapGrid(ponto.x, ponto.y)
                    }
                },
    ) {
        marcadores.forEach { marcador ->
            val offsetX = with(density) { (marcador.posX * tamanhoPx.width).toDp() } - (TAMANHO_MARCADOR / 2)
            val offsetY = with(density) { (marcador.posY * tamanhoPx.height).toDp() } - (TAMANHO_MARCADOR / 2)
            WifiCasaMarcadorVisual(
                marcador = marcador,
                offsetX = offsetX.coerceAtLeast(0.dp),
                offsetY = offsetY.coerceAtLeast(0.dp),
            )
        }
    }
}

@Composable
private fun WifiCasaMarcadorVisual(
    marcador: MarcadorUi,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
) {
    val c = LocalLkTokens.current
    val cor = marcador.rssiDbm?.let { signalColor(it, marcador.banda, c) } ?: c.textTertiary
    val descricao =
        buildString {
            append(if (marcador.ehRoteador) "Roteador" else marcador.rotulo)
            marcador.categoria?.let { append(", sinal $it") }
        }

    Box(
        modifier =
            Modifier
                .padding(start = offsetX, top = offsetY)
                .size(TAMANHO_MARCADOR)
                .clip(CircleShape)
                .background(cor.copy(alpha = 0.16f))
                .border(2.dp, cor, CircleShape)
                .semantics(mergeDescendants = true) { contentDescription = descricao },
        contentAlignment = Alignment.Center,
    ) {
        if (marcador.ehRoteador) {
            Icon(Icons.Outlined.Router, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = marcador.rotulo.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = cor,
            )
        }
    }
}

/**
 * Alternativa não-visual ao grid (NFR da spec -- TalkBack/Dynamic Type): mesma lista de
 * marcadores, sem posicionamento livre, lida a partir do mesmo `WifiCasaUiState` (nunca uma
 * segunda fonte de verdade -- só uma segunda forma de renderizar o mesmo estado).
 */
@Composable
fun WifiCasaListaMarcadoresAcessivel(
    marcadores: List<MarcadorUi>,
    onEditarMarcador: ((MarcadorUi) -> Unit)?,
    onRemoverMarcador: ((MarcadorUi) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val c = LocalLkTokens.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(LkSpacing.sm)) {
        marcadores.forEach { marcador ->
            val cor = marcador.rssiDbm?.let { signalColor(it, marcador.banda, c) } ?: c.textTertiary
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LkRadius.input))
                        .background(c.bgSecondary)
                        .padding(horizontal = LkSpacing.md, vertical = LkSpacing.sm)
                        .semantics(mergeDescendants = true) {
                            contentDescription =
                                buildString {
                                    append(if (marcador.ehRoteador) "Roteador" else marcador.rotulo)
                                    marcador.categoria?.let { append(", sinal $it") }
                                }
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (marcador.ehRoteador) {
                    Icon(Icons.Outlined.Router, contentDescription = null, tint = cor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(LkSpacing.sm))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (marcador.ehRoteador) "Roteador" else marcador.rotulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = c.textPrimary,
                    )
                    Text(
                        marcador.categoria ?: "Sem medição",
                        style = MaterialTheme.typography.bodySmall,
                        color = cor,
                    )
                }
                if (onEditarMarcador != null) {
                    Text(
                        "Editar",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.primary,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(LkRadius.pill))
                                .clickable { onEditarMarcador(marcador) }
                                .padding(horizontal = LkSpacing.sm, vertical = LkSpacing.xs)
                                .semantics { contentDescription = "Editar ${marcador.rotulo}" },
                    )
                }
                if (onRemoverMarcador != null) {
                    Text(
                        "Remover",
                        style = MaterialTheme.typography.labelLarge,
                        color = c.error,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(LkRadius.pill))
                                .clickable { onRemoverMarcador(marcador) }
                                .padding(horizontal = LkSpacing.sm, vertical = LkSpacing.xs)
                                .semantics { contentDescription = "Remover ${marcador.rotulo}" },
                    )
                }
            }
        }
    }
}
