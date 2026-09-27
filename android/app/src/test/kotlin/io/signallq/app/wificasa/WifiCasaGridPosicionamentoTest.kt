package io.signallq.app.wificasa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiCasaGridPosicionamentoTest {
    @Test
    fun `clampNormalizado mantem valores dentro de 0f e 1f`() {
        assertEquals(0f, clampNormalizado(-0.5f), 0.0001f)
        assertEquals(1f, clampNormalizado(1.5f), 0.0001f)
        assertEquals(0.5f, clampNormalizado(0.5f), 0.0001f)
    }

    @Test
    fun `pixelParaNormalizado converte pixel em fracao 0f a 1f`() {
        val ponto = pixelParaNormalizado(pixelX = 150f, pixelY = 300f, larguraPx = 300f, alturaPx = 600f)
        assertEquals(0.5f, ponto.x, 0.0001f)
        assertEquals(0.5f, ponto.y, 0.0001f)
    }

    @Test
    fun `pixelParaNormalizado faz clamping quando toque sai da area do canvas`() {
        val ponto = pixelParaNormalizado(pixelX = -10f, pixelY = 1000f, larguraPx = 300f, alturaPx = 300f)
        assertEquals(0f, ponto.x, 0.0001f)
        assertEquals(1f, ponto.y, 0.0001f)
    }

    @Test
    fun `pixelParaNormalizado com canvas sem tamanho retorna origem sem crashar`() {
        val ponto = pixelParaNormalizado(pixelX = 10f, pixelY = 10f, larguraPx = 0f, alturaPx = 0f)
        assertEquals(0f, ponto.x, 0.0001f)
        assertEquals(0f, ponto.y, 0.0001f)
    }

    @Test
    fun `normalizadoParaPixel e o inverso de pixelParaNormalizado`() {
        val normalizado = pixelParaNormalizado(pixelX = 90f, pixelY = 60f, larguraPx = 300f, alturaPx = 200f)
        val pixel = normalizadoParaPixel(normalizado, larguraPx = 300f, alturaPx = 200f)
        assertEquals(90f, pixel.x, 0.01f)
        assertEquals(60f, pixel.y, 0.01f)
    }

    private data class MarcadorTeste(
        val id: String,
        val x: Float,
        val y: Float,
    )

    @Test
    fun `encontrarMarcadorTocado retorna o marcador mais proximo dentro do raio`() {
        val marcadores =
            listOf(
                MarcadorTeste("longe", 0.9f, 0.9f),
                MarcadorTeste("perto", 0.51f, 0.51f),
            )
        val tocado =
            encontrarMarcadorTocado(
                marcadores = marcadores,
                posX = { it.x },
                posY = { it.y },
                toqueX = 0.5f,
                toqueY = 0.5f,
            )
        assertEquals("perto", tocado?.id)
    }

    @Test
    fun `encontrarMarcadorTocado retorna null quando nenhum marcador esta perto o bastante`() {
        val marcadores = listOf(MarcadorTeste("longe", 0.9f, 0.9f))
        val tocado =
            encontrarMarcadorTocado(
                marcadores = marcadores,
                posX = { it.x },
                posY = { it.y },
                toqueX = 0.1f,
                toqueY = 0.1f,
            )
        assertNull(tocado)
    }

    @Test
    fun `encontrarMarcadorTocado em lista vazia retorna null`() {
        val tocado =
            encontrarMarcadorTocado(
                marcadores = emptyList<MarcadorTeste>(),
                posX = { it.x },
                posY = { it.y },
                toqueX = 0.5f,
                toqueY = 0.5f,
            )
        assertNull(tocado)
    }
}
