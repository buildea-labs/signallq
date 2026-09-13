package io.signallq.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.signallq.app.ui.SignallQTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoricoConclusaoLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `conclusao longa mantem o texto completo na apresentacao de duas linhas`() {
        val conclusao =
            "O Wi-Fi está bom, mas alguns indicadores de internet merecem atenção " +
                "porque a resposta da conexão oscilou durante a medição"

        composeRule.setContent {
            SignallQTheme {
                Box(Modifier.width(180.dp)) {
                    HistoricoConclusaoTexto(
                        texto = conclusao,
                        cor = Color.Unspecified,
                        modifier = Modifier.testTag("historico_conclusao"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("historico_conclusao").assertTextEquals(conclusao)
    }
}
