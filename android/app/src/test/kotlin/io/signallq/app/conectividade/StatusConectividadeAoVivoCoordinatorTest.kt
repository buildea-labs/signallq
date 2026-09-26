package io.signallq.app.conectividade

import io.signallq.app.core.network.EstadoConexao
import io.signallq.app.core.network.connectivity.ConnectivityDiagnosisSource
import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.core.network.contracts.connectivity.ConnectivityStatus
import io.signallq.app.core.network.contracts.connectivity.ProbeResult
import io.signallq.app.core.network.contracts.topologia.NivelConfianca
import io.signallq.app.ui.component.SignallQFeedbackTone
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o coordenador de polling foreground do badge "ao vivo" (Architecture Plan "Status de
 * conectividade ao vivo na Home", seções 6/7/10) com `TestDispatcher` e um fake de
 * [ConnectivityDiagnosisSource] -- sem Robolectric/Android real.
 *
 * Deliberadamente NÃO usa `runTest {}`: o coordenador mantém um loop `while (isActive) { ...;
 * delay(...) }` de vida própria, injetado com o mesmo `testScheduler` do teste. `runTest`
 * chama `advanceUntilIdle()` na finalização e um loop que sempre reagenda um novo `delay`
 * nunca fica "idle" -- isso trava o teste (a virtual-time nunca para de avançar). Cada teste
 * aqui controla o `TestScheduler` manualmente (`runCurrent()`/`advanceTimeBy()`) e cancela o
 * `TestScope` no fim (`scope.cancel()`), então não há teardown implícito que dependa do loop
 * chegar a um estado ocioso.
 */
class StatusConectividadeAoVivoCoordinatorTest {
    private fun diagnosticoSucesso(): ConnectivityDiagnosis =
        ConnectivityDiagnosis(
            transport = EstadoConexao.wifi,
            wifiConnected = true,
            localAddressAvailable = true,
            gatewayConfigured = true,
            gatewayReachable = ProbeResult.Success(),
            dnsConfigured = true,
            dnsReachable = ProbeResult.Success(),
            externalIpReachable = ProbeResult.Success(),
            hostnameReachable = ProbeResult.Success(),
            androidInternetCapability = true,
            androidValidated = true,
            captivePortalDetected = false,
            mobileFallbackAvailable = false,
            status = ConnectivityStatus.INTERNET_AVAILABLE,
            confidence = NivelConfianca.ALTA,
            evidence = emptyList(),
            startedAtEpochMs = 0L,
            finishedAtEpochMs = 1L,
        )

    /** Fake que conta chamadas; comportamento fixo por instância (sucesso ou exceção). */
    private class FakeConnectivityDiagnosisSource(
        private val resultado: () -> ConnectivityDiagnosis,
    ) : ConnectivityDiagnosisSource {
        var chamadas = 0
            private set

        override suspend fun existeRedeWifiAtiva(): Boolean = true

        override suspend fun diagnosticar(): ConnectivityDiagnosis {
            chamadas++
            return resultado()
        }
    }

    @Test
    fun `iniciar produz o primeiro status apos a 1a rodada`() {
        val source = FakeConnectivityDiagnosisSource { diagnosticoSucesso() }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        assertNull(coordinator.status.value)
        coordinator.iniciar()
        scope.testScheduler.runCurrent()

        assertEquals(SignallQFeedbackTone.Success, coordinator.status.value?.geral)
        assertEquals(1, source.chamadas)

        coordinator.parar()
        scope.cancel()
    }

    @Test
    fun `loop nunca fixed-rate -- proxima rodada so apos o intervalo desde o fim da anterior`() {
        val source = FakeConnectivityDiagnosisSource { diagnosticoSucesso() }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        coordinator.iniciar()
        scope.testScheduler.runCurrent()
        assertEquals(1, source.chamadas)

        // Antes do intervalo completo: ainda não deve ter rodado de novo.
        scope.testScheduler.advanceTimeBy(StatusConectividadeAoVivoCoordinator.INTERVALO_ENTRE_RODADAS_MS - 1)
        scope.testScheduler.runCurrent()
        assertEquals(1, source.chamadas)

        scope.testScheduler.advanceTimeBy(2)
        scope.testScheduler.runCurrent()
        assertEquals(2, source.chamadas)

        coordinator.parar()
        scope.cancel()
    }

    @Test
    fun `iniciar chamado duas vezes nao inicia uma segunda rodada sobreposta`() {
        val source = FakeConnectivityDiagnosisSource { diagnosticoSucesso() }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        coordinator.iniciar()
        coordinator.iniciar()
        coordinator.iniciar()
        scope.testScheduler.runCurrent()

        assertEquals(1, source.chamadas)

        coordinator.parar()
        scope.cancel()
    }

    @Test
    fun `parar cancela o loop e volta para carregando -- sem staleness`() {
        val source = FakeConnectivityDiagnosisSource { diagnosticoSucesso() }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        coordinator.iniciar()
        scope.testScheduler.runCurrent()
        assertTrue(coordinator.status.value != null)

        coordinator.parar()
        assertNull(coordinator.status.value)
        assertNull(coordinator.ultimoDiagnostico.value)

        // Nenhuma rodada nova deve rodar depois de parar, mesmo avançando o tempo.
        scope.testScheduler.advanceTimeBy(StatusConectividadeAoVivoCoordinator.INTERVALO_ENTRE_RODADAS_MS * 2)
        scope.testScheduler.runCurrent()
        assertEquals(1, source.chamadas)
        assertNull(coordinator.status.value)

        scope.cancel()
    }

    @Test
    fun `reiniciar apos parar comeca do zero -- nunca reexibe o ultimo valor como atual`() {
        val source = FakeConnectivityDiagnosisSource { diagnosticoSucesso() }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        coordinator.iniciar()
        scope.testScheduler.runCurrent()
        assertTrue(coordinator.status.value != null)

        coordinator.parar()
        coordinator.iniciar()

        // Antes da 1a leitura da nova rodada, o estado deve ser "carregando" (null),
        // nunca o último valor da rodada anterior.
        assertNull(coordinator.status.value)

        coordinator.parar()
        scope.cancel()
    }

    @Test
    fun `excecao inesperada vira Incerto sem derrubar a Home e sem evidencia bruta`() {
        val source =
            FakeConnectivityDiagnosisSource { throw IllegalStateException("falha inesperada de sondagem") }
        val scope = TestScope(StandardTestDispatcher())
        val coordinator = StatusConectividadeAoVivoCoordinator(source, scope)

        coordinator.iniciar()
        scope.testScheduler.runCurrent()

        assertEquals(SignallQFeedbackTone.Incerto, coordinator.status.value?.geral)
        assertNull(coordinator.status.value?.causaPrincipal)
        assertNull(coordinator.ultimoDiagnostico.value)

        coordinator.parar()
        scope.cancel()
    }
}
