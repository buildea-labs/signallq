package io.signallq.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guarda de regressão: os hosts *.giammattey-luiz.workers.dev deixaram de resolver (todos os
 * Workers passaram a responder em *.gmmattey.workers.dev). O host é fixo no BuildConfig, então
 * uma versão publicada com o endereço morto só se corrige com nova versão do app.
 */
class WorkerHostsBuildConfigTest {
    private val hostMorto = "giammattey-luiz.workers.dev"

    private val urls =
        mapOf(
            "ADMIN_INGEST_URL" to BuildConfig.ADMIN_INGEST_URL,
            "GAME_LATENCY_PROBE_URL" to BuildConfig.GAME_LATENCY_PROBE_URL,
            "SERVICE_STATUS_API_URL" to BuildConfig.SERVICE_STATUS_API_URL,
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
