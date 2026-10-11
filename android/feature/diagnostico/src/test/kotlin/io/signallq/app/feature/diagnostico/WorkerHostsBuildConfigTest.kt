package io.signallq.app.feature.diagnostico

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guarda de regressão: os hosts *.giammattey-luiz.workers.dev deixaram de resolver; os Workers
 * respondem em *.gmmattey.workers.dev. O host é fixo no BuildConfig deste módulo.
 */
class WorkerHostsBuildConfigTest {
    private val hostMorto = "giammattey-luiz.workers.dev"

    private val urls =
        mapOf(
            "AI_WORKER_URL" to BuildConfig.AI_WORKER_URL,
            "DIAGNOSTIC_WORKER_URL" to BuildConfig.DIAGNOSTIC_WORKER_URL,
        )

    @Test
    fun `nenhuma URL de Worker aponta para o host antigo`() {
        urls.forEach { (nome, url) -> assertFalse("$nome usa o host morto: $url", url.contains(hostMorto)) }
    }

    @Test
    fun `URLs de Worker usam https`() {
        urls.forEach { (nome, url) -> assertTrue("$nome nao usa https: $url", url.startsWith("https://")) }
    }
}
