package io.signallq.app.feature.speedtest

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ExecutorSpeedtestCloudflareFalhaTest {
    private val executor = ExecutorSpeedtestCloudflare()

    @Test
    fun `sem transporte prevalece sobre erro de hostname`() {
        val causa =
            executor.mapearCausaFalha(
                erro = UnknownHostException("speed.cloudflare.com"),
                possuiTransporte = false,
            )

        assertEquals(CausaFalhaSpeedtest.SEM_CONEXAO, causa)
    }

    @Test
    fun `hostname inacessivel com transporte recebe causa segura de dns`() {
        val causa =
            executor.mapearCausaFalha(
                erro = IllegalStateException("download_failed:UnknownHostException: host privado"),
                possuiTransporte = true,
            )

        assertEquals(CausaFalhaSpeedtest.DNS_OU_HOSTNAME_INACESSIVEL, causa)
    }

    @Test
    fun `timeout nunca recebe causa de sucesso`() {
        val causa =
            executor.mapearCausaFalha(
                erro = IllegalStateException("download_failed", SocketTimeoutException("read timed out")),
                possuiTransporte = true,
            )

        assertEquals(CausaFalhaSpeedtest.TIMEOUT, causa)
    }

    @Test
    fun `erro nao reconhecido recebe fallback seguro`() {
        val causa =
            executor.mapearCausaFalha(
                erro = IllegalStateException("falha interna"),
                possuiTransporte = true,
            )

        assertEquals(CausaFalhaSpeedtest.FALHA_GENERICA, causa)
    }
}
