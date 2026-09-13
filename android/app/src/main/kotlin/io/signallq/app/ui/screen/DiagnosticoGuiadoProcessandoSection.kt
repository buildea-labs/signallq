package io.signallq.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.signallq.app.ui.LkSpacing
import io.signallq.app.ui.LocalLkTokens
import io.signallq.app.ui.component.SignallQScreenState
import io.signallq.app.ui.component.SignallQStatefulScreen

internal const val TAG_ASSIST_PROCESSANDO = "assist_processando"
internal const val TAG_ASSIST_PROCESSANDO_INDICADOR = "assist_processando_indicador"

/** Estado transitório da chamada direta do SignallQ Assist ao NDS. */
@Composable
internal fun DiagnosticoGuiadoProcessandoSection(
    estado: SignallQScreenState<Unit>,
    modifier: Modifier = Modifier,
    onTentarNovamente: () -> Unit,
) {
    when (estado) {
        SignallQScreenState.Loading -> AssistProcessandoContent(modifier)
        else ->
            SignallQStatefulScreen(
                state = estado,
                modifier = modifier,
                onAction = onTentarNovamente,
                actionLabel = "Tentar novamente",
            ) { }
    }
}

/**
 * A chamada ao Assist é um processamento único, não conteúdo parcial chegando à tela. Por isso,
 * ela não usa o skeleton genérico: mantém uma mensagem estável enquanto a análise é preparada.
 */
@Composable
private fun AssistProcessandoContent(modifier: Modifier) {
    val c = LocalLkTokens.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(LkSpacing.xl)
                .testTag(TAG_ASSIST_PROCESSANDO),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LkSpacing.lg, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = c.textTertiary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "O Assist está analisando sua conexão",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Estamos reunindo os dados da sua rede para preparar uma resposta clara.",
            style = MaterialTheme.typography.bodyLarge,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        CircularProgressIndicator(
            modifier =
                Modifier
                    .size(28.dp)
                    .testTag(TAG_ASSIST_PROCESSANDO_INDICADOR)
                    .semantics { contentDescription = "Análise em andamento" },
            color = c.primary,
            trackColor = c.bgSecondary,
            strokeWidth = 3.dp,
        )
    }
}
