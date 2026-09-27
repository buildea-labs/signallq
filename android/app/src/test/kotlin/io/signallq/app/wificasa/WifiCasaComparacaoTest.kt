package io.signallq.app.wificasa

import io.signallq.app.core.database.wificasa.MarcadorMapeamentoEntity
import io.signallq.app.core.database.wificasa.TipoMarcadorMapeamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiCasaComparacaoTest {
    private fun marcador(
        id: String,
        rotulo: String,
        rssiDbm: Int,
        bandaWifi: String = "ghz5",
        tipo: String = TipoMarcadorMapeamento.COMODO,
    ) = MarcadorMapeamentoEntity(
        id = id,
        mapeamentoId = "map",
        rotulo = rotulo,
        tipo = tipo,
        posX = 0.5f,
        posY = 0.5f,
        rssiDbm = rssiDbm,
        bandaWifi = bandaWifi,
        criadoEmEpochMs = 1_000L,
    )

    @Test
    fun `marcador que piorou de excelente para fraco e classificado como PIOROU`() {
        val antes = listOf(marcador("a1", "Sala", rssiDbm = -50)) // Excelente
        val depois = listOf(marcador("d1", "Sala", rssiDbm = -85)) // Fraco (critico)

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(1, resultado.size)
        assertEquals(ResultadoComparacaoMarcador.PIOROU, resultado[0].resultado)
        assertEquals("Excelente", resultado[0].categoriaAntes)
        assertEquals("Fraco", resultado[0].categoriaDepois)
    }

    @Test
    fun `marcador que melhorou de fraco para excelente e classificado como MELHOROU`() {
        val antes = listOf(marcador("a1", "Quarto", rssiDbm = -85))
        val depois = listOf(marcador("d1", "Quarto", rssiDbm = -50))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(ResultadoComparacaoMarcador.MELHOROU, resultado[0].resultado)
    }

    @Test
    fun `marcador com mesma categoria antes e depois e NAO_MUDOU`() {
        val antes = listOf(marcador("a1", "Cozinha", rssiDbm = -50))
        val depois = listOf(marcador("d1", "Cozinha", rssiDbm = -52))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(ResultadoComparacaoMarcador.NAO_MUDOU, resultado[0].resultado)
    }

    @Test
    fun `marcador so no depois e classificado como NOVO`() {
        val antes = emptyList<MarcadorMapeamentoEntity>()
        val depois = listOf(marcador("d1", "Varanda", rssiDbm = -60))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(1, resultado.size)
        assertEquals(ResultadoComparacaoMarcador.NOVO, resultado[0].resultado)
        assertNull(resultado[0].categoriaAntes)
    }

    @Test
    fun `marcador so no antes e classificado como REMOVIDO`() {
        val antes = listOf(marcador("a1", "Garagem", rssiDbm = -60))
        val depois = emptyList<MarcadorMapeamentoEntity>()

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(1, resultado.size)
        assertEquals(ResultadoComparacaoMarcador.REMOVIDO, resultado[0].resultado)
        assertNull(resultado[0].categoriaDepois)
    }

    @Test
    fun `casamento de rotulo ignora espaco e maiusculas`() {
        val antes = listOf(marcador("a1", " Sala ", rssiDbm = -60))
        val depois = listOf(marcador("d1", "SALA", rssiDbm = -60))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(1, resultado.size)
        assertEquals(ResultadoComparacaoMarcador.NAO_MUDOU, resultado[0].resultado)
    }

    @Test
    fun `rotulos diferentes nao sao casados por engano`() {
        val antes = listOf(marcador("a1", "Sala 1", rssiDbm = -60))
        val depois = listOf(marcador("d1", "Sala 2", rssiDbm = -60))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(2, resultado.size)
        assertTrue(resultado.any { it.resultado == ResultadoComparacaoMarcador.REMOVIDO })
        assertTrue(resultado.any { it.resultado == ResultadoComparacaoMarcador.NOVO })
    }

    @Test
    fun `marcador de roteador nunca entra na comparacao`() {
        val antes =
            listOf(
                marcador("a1", "Sala", rssiDbm = -60),
                marcador("a2", "Roteador", rssiDbm = -30, tipo = TipoMarcadorMapeamento.ROTEADOR),
            )
        val depois = listOf(marcador("d1", "Sala", rssiDbm = -60))

        val resultado = compararMapeamentosWifiCasa(antes, depois)

        assertEquals(1, resultado.size)
        assertEquals("Sala", resultado[0].rotulo)
    }

    @Test
    fun `sem nenhum marcador correspondente retorna lista vazia`() {
        val resultado = compararMapeamentosWifiCasa(emptyList(), emptyList())
        assertTrue(resultado.isEmpty())
    }
}
