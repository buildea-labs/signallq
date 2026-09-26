package io.signallq.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.signallq.app.core.diagnostico.EstagioRede
import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.core.network.contracts.connectivity.ProbeFailureReason
import io.signallq.app.core.network.contracts.connectivity.ProbeResult
import io.signallq.app.ui.LkSpacing
import io.signallq.app.ui.LocalLkTokens
import io.signallq.app.ui.component.SignallQBadge
import io.signallq.app.ui.component.SignallQButton
import io.signallq.app.ui.component.SignallQButtonStyle
import io.signallq.app.ui.component.SignallQFeedbackTone
import io.signallq.app.ui.component.accessibleLabel
import io.signallq.app.ui.component.toBadgeTone

/**
 * Sheet de explicação por estágio, aberta ao tocar num ícone com badge da trilha da Home
 * (Architecture Plan "Status de conectividade ao vivo na Home", seção 5). Copy próprio
 * (não fixado pelo plano — delegado a Cora/Davi): resumo humano primeiro, sem jargão;
 * "Detalhes técnicos" (DNS/gateway/rota externa) fica numa seção secundária, só quando há
 * [diagnostico] bruto disponível (nem toda rodada mantém a evidência completa em memória).
 */
@Composable
internal fun Inicio2EstagioDetalheSheet(
    label: String,
    estagio: EstagioRede,
    tom: SignallQFeedbackTone,
    diagnostico: ConnectivityDiagnosis?,
    onDismiss: () -> Unit,
) {
    val c = LocalLkTokens.current
    var mostrarDetalhesTecnicos by remember { mutableStateOf(false) }
    SimpleInfoSheet(
        c = c,
        titulo = label,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = LkSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(LkSpacing.md),
        ) {
            SignallQBadge(label = tom.accessibleLabel(), tone = tom.toBadgeTone())
            Text(
                text = resumoTitulo(estagio, tom),
                style = MaterialTheme.typography.titleLarge,
                color = c.textPrimary,
            )
            Text(
                text = resumoExplicacao(estagio, tom),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary,
            )
            if (diagnostico != null) {
                SignallQButton(
                    label = if (mostrarDetalhesTecnicos) "Ocultar detalhes técnicos" else "Ver detalhes técnicos",
                    onClick = { mostrarDetalhesTecnicos = !mostrarDetalhesTecnicos },
                    style = SignallQButtonStyle.Text,
                )
                if (mostrarDetalhesTecnicos) {
                    HorizontalDivider(color = c.border, thickness = 1.dp)
                    InfoRow(c, "Roteador (gateway)", diagnostico.gatewayReachable.resumoTecnico())
                    InfoRow(c, "DNS", diagnostico.dnsReachable.resumoTecnico())
                    InfoRow(c, "Rota externa", diagnostico.externalIpReachable.resumoTecnico())
                }
            }
        }
    }
}

private fun resumoTitulo(
    estagio: EstagioRede,
    tom: SignallQFeedbackTone,
): String =
    when (tom) {
        SignallQFeedbackTone.Success -> "Está tudo bem por aqui"
        SignallQFeedbackTone.Warning ->
            when (estagio) {
                EstagioRede.WIFI -> "O sinal do seu Wi-Fi está oscilando"
                EstagioRede.PROVEDOR -> "Sua internet externa está mais lenta"
            }
        SignallQFeedbackTone.Error ->
            when (estagio) {
                EstagioRede.WIFI -> "Não conseguimos falar com seu roteador"
                EstagioRede.PROVEDOR -> "A internet externa não está respondendo"
            }
        SignallQFeedbackTone.Incerto -> "Ainda não temos certeza"
        SignallQFeedbackTone.Neutral -> "Conferindo agora"
    }

private fun resumoExplicacao(
    estagio: EstagioRede,
    tom: SignallQFeedbackTone,
): String =
    when (tom) {
        SignallQFeedbackTone.Success ->
            when (estagio) {
                EstagioRede.WIFI -> "Seu roteador está respondendo normalmente."
                EstagioRede.PROVEDOR -> "A internet fora da sua casa está respondendo normalmente."
            }
        SignallQFeedbackTone.Warning ->
            when (estagio) {
                EstagioRede.WIFI ->
                    "O sinal entre o seu aparelho e o roteador está instável. Vídeos e chamadas " +
                        "podem engasgar de vez em quando. Tente se aproximar do roteador."
                EstagioRede.PROVEDOR ->
                    "A internet que vem de fora de casa está respondendo mais devagar que o normal. " +
                        "Isso costuma passar sozinho, mas pode valer reiniciar o roteador se persistir."
            }
        SignallQFeedbackTone.Error ->
            when (estagio) {
                EstagioRede.WIFI ->
                    "Seu aparelho não conseguiu falar com o roteador. Aproxime-se dele, confira se " +
                        "ele está ligado ou tente reiniciá-lo."
                EstagioRede.PROVEDOR ->
                    "Seu Wi-Fi está funcionando bem, mas a internet que vem do seu provedor não está " +
                        "respondendo. Pode ser uma instabilidade temporária do provedor."
            }
        SignallQFeedbackTone.Incerto ->
            "Não conseguimos confirmar com segurança o que está acontecendo nesta parte da sua rede " +
                "agora. Vamos continuar checando; toque em \"Analisar minha conexão\" para um " +
                "diagnóstico completo."
        SignallQFeedbackTone.Neutral -> "Estamos conferindo esta parte da sua rede agora."
    }

private fun ProbeResult.resumoTecnico(): String =
    when (this) {
        is ProbeResult.Success -> "Respondeu" + (elapsedMs?.let { " em ${it}ms" } ?: "")
        is ProbeResult.Failure -> "Falhou (${reason.resumo()})"
        is ProbeResult.Timeout -> "Não respondeu a tempo (${afterMs}ms)"
        is ProbeResult.NotExecuted -> "Não verificado"
        is ProbeResult.Unavailable -> "Indisponível"
    }

private fun ProbeFailureReason.resumo(): String =
    when (this) {
        ProbeFailureReason.DNS_RESOLUTION_FAILED -> "resolução de nome falhou"
        ProbeFailureReason.HOST_UNREACHABLE -> "destino inalcançável"
        ProbeFailureReason.UNEXPECTED_RESPONSE -> "resposta inesperada"
        ProbeFailureReason.UNKNOWN -> "motivo desconhecido"
    }
