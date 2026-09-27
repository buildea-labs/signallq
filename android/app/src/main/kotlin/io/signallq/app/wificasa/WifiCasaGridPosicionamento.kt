package io.signallq.app.wificasa

import kotlin.math.sqrt

private const val LIMITE_MINIMO = 0f
private const val LIMITE_MAXIMO = 1f

/** Raio de hit-test em fração do menor lado do canvas -- ~12% de diâmetro do alvo tocável. */
internal const val RAIO_HIT_TEST_PADRAO_NORMALIZADO = 0.06f

/** Posição normalizada (0f..1f) no grid livre do WiFi Casa -- nunca pixel/dp (spec 3.1). */
data class PontoNormalizado(
    val x: Float,
    val y: Float,
)

internal fun clampNormalizado(valor: Float): Float = valor.coerceIn(LIMITE_MINIMO, LIMITE_MAXIMO)

/**
 * Converte um toque em pixels (relativo ao canto superior esquerdo do canvas) para uma posição
 * normalizada, já com clamping em 0f..1f (`.agents/architecture-plan.md`, seção 4.4).
 */
internal fun pixelParaNormalizado(
    pixelX: Float,
    pixelY: Float,
    larguraPx: Float,
    alturaPx: Float,
): PontoNormalizado {
    if (larguraPx <= 0f || alturaPx <= 0f) return PontoNormalizado(0f, 0f)
    return PontoNormalizado(clampNormalizado(pixelX / larguraPx), clampNormalizado(pixelY / alturaPx))
}

/** Inverso de [pixelParaNormalizado] -- usado para desenhar um marcador salvo no canvas real. */
internal fun normalizadoParaPixel(
    ponto: PontoNormalizado,
    larguraPx: Float,
    alturaPx: Float,
): PontoNormalizado = PontoNormalizado(ponto.x * larguraPx, ponto.y * alturaPx)

private fun distanciaNormalizada(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return sqrt(dx * dx + dy * dy)
}

/**
 * Encontra o marcador mais próximo do toque, dentro de [raioHitTest] (posições normalizadas) --
 * `null` quando nenhum marcador está próximo o bastante. Função genérica (sem depender de
 * `MarcadorUi`/Compose) para facilitar teste isolado.
 */
internal fun <T> encontrarMarcadorTocado(
    marcadores: List<T>,
    posX: (T) -> Float,
    posY: (T) -> Float,
    toqueX: Float,
    toqueY: Float,
    raioHitTest: Float = RAIO_HIT_TEST_PADRAO_NORMALIZADO,
): T? =
    marcadores
        .map { it to distanciaNormalizada(posX(it), posY(it), toqueX, toqueY) }
        .filter { (_, distancia) -> distancia <= raioHitTest }
        .minByOrNull { (_, distancia) -> distancia }
        ?.first
