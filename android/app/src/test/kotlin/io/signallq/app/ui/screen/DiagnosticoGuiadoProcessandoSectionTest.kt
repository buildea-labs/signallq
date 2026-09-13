package io.signallq.app.ui.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.signallq.app.ui.SignallQTheme
import io.signallq.app.ui.component.SignallQScreenState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h640dp")
class DiagnosticoGuiadoProcessandoSectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `loading mostra uma mensagem dedicada do Assist sem skeleton`() {
        composeRule.setThemedContent {
            DiagnosticoGuiadoProcessandoSection(
                estado = SignallQScreenState.Loading,
                onTentarNovamente = {},
            )
        }

        composeRule.onNodeWithTag(TAG_ASSIST_PROCESSANDO).assertIsDisplayed()
        composeRule.onNodeWithText("O Assist está analisando sua conexão").assertIsDisplayed()
        composeRule
            .onNodeWithText("Estamos reunindo os dados da sua rede para preparar uma resposta clara.")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Análise em andamento").assertIsDisplayed()
        composeRule.onAllNodesWithText("Conteúdo carregando").assertCountEquals(0)
    }

    @Test
    fun `erro mantém acao para tentar novamente`() {
        var tentativas = 0
        composeRule.setThemedContent {
            DiagnosticoGuiadoProcessandoSection(
                estado =
                    SignallQScreenState.RecoverableError(
                        title = "Não foi possível acessar o Assist no momento",
                        message = "Tente novamente para buscar uma análise atualizada.",
                    ),
                onTentarNovamente = { tentativas++ },
            )
        }

        composeRule.onNodeWithText("Não foi possível acessar o Assist no momento").assertIsDisplayed()
        composeRule.onNodeWithText("Tentar novamente").assertHasClickAction().performClick()

        assertEquals(1, tentativas)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) = setContent { SignallQTheme(content = content) }
}
