package io.signallq.app.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.signallq.app.core.network.EstadoConexao
import io.signallq.app.ui.SignallQTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * CTA contextual do `SignallQOfflineBanner` (issue #1811). O diagnóstico guiado é uma
 * sondagem Wi-Fi: sem esse transporte, o banner só orienta a conectar-se e não fabrica uma
 * falha de gateway.
 *
 * O fluxo com Wi-Fi ativo delega à navegação externa quando ela existe. O diálogo real segue
 * coberto isoladamente por `DiagnosticoOfflineDialogTest`, sem depender do transporte de um
 * ambiente Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SignallQOfflineBannerCtaTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `sem wifi cta orienta como continuar`() {
        composeRule.setContent {
            SignallQTheme {
                SignallQOfflineBanner(estadoConexao = EstadoConexao.desconectado)
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Você está offline").assertExists()
        composeRule.onNodeWithText("Como continuar").assertExists()
    }

    @Test
    fun `tap sem wifi mostra orientacao e nao abre diagnostico wifi`() {
        composeRule.setContent {
            SignallQTheme {
                SignallQOfflineBanner(estadoConexao = EstadoConexao.desconectado)
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Como continuar").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Conecte-se a uma rede Wi-Fi e tente diagnosticar novamente. Recursos locais continuam disponíveis.").assertExists()
        composeRule.onNodeWithText("Diagnóstico guiado").assertDoesNotExist()
    }

    @Test
    fun `tap com wifi sem internet abre diagnostico guiado existente`() {
        composeRule.setContent {
            SignallQTheme {
                SignallQOfflineBanner(estadoConexao = EstadoConexao.wifi)
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Diagnosticar problema").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Diagnóstico guiado").assertExists()
    }

    @Test
    fun `tap com wifi abre diagnostico externo quando fornecido`() {
        var chamadas = 0
        composeRule.setContent {
            SignallQTheme {
                SignallQOfflineBanner(
                    estadoConexao = EstadoConexao.wifi,
                    onDiagnosticarProblema = { chamadas++ },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Diagnosticar problema").performClick()
        composeRule.waitForIdle()

        assert(chamadas == 1) { "esperava 1 chamada ao callback externo, houve $chamadas" }
        composeRule.onNodeWithText("Diagnóstico guiado").assertDoesNotExist()
    }
}
