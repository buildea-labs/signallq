package io.signallq.app.ui.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.signallq.app.ui.SignallQTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GaugeCircularTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `numero chega a nova amostra por animacao curta`() {
        var atualizarVelocidade: (Float) -> Unit = {}
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            var velocidade by remember { mutableFloatStateOf(10f) }
            atualizarVelocidade = { velocidade = it }
            SignallQTheme {
                GaugeCircular(
                    progressoGlobal = 0.5f,
                    rotulo = "DOWNLOAD",
                    velocidadeMbps = velocidade,
                    corFase = Color.Green,
                    unidade = "Mbps",
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithText("10.0").assertExists()

        composeRule.runOnIdle { atualizarVelocidade(20f) }
        composeRule.mainClock.advanceTimeByFrame()

        // O valor alvo não aparece no primeiro frame: o número acompanha a amostra sem pular.
        composeRule.onNodeWithText("20.0").assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithText("20.0").assertExists()
    }

    @Test
    fun `valor ausente limpa a velocidade visual sem manter uma amostra antiga`() {
        assertEquals(0f, normalizarVelocidadeDoGauge(Float.NaN), 0f)
        assertEquals(0f, velocidadeVisualDoGauge(velocidadeAlvo = 0f, velocidadeAnimada = 42.5f), 0f)
    }
}
