package io.signallq.app.core.diagnostico

import io.signallq.app.core.network.EstadoConexao
import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.core.network.contracts.connectivity.ConnectivityStatus
import io.signallq.app.core.network.contracts.connectivity.ProbeResult
import io.signallq.app.core.network.contracts.topologia.NivelConfianca
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Matriz completa `ConnectivityStatus` (10 valores) x `NivelConfianca` (3 valores) para
 * [ClassificadorConectividadeAoVivo] (Architecture Plan "Status de conectividade ao vivo
 * na Home", seção 10 -- estratégia de testes). Cobre a regra dura de que
 * `NivelConfianca.BAIXA` sempre força `Incerto` nos dois estágios, independente do
 * `ConnectivityStatus` resolvido (`AGENTS.md` §8: nunca apresentar causa raiz sem
 * evidência suficiente).
 */
class ClassificadorConectividadeAoVivoTest {
    private fun diagnostico(
        status: ConnectivityStatus,
        confidence: NivelConfianca,
    ) = ConnectivityDiagnosis(
        transport = EstadoConexao.wifi,
        wifiConnected = status != ConnectivityStatus.WIFI_DISCONNECTED,
        localAddressAvailable = status != ConnectivityStatus.NO_LOCAL_ADDRESS,
        gatewayConfigured = true,
        gatewayReachable = ProbeResult.Success(),
        dnsConfigured = true,
        dnsReachable = ProbeResult.Success(),
        externalIpReachable = ProbeResult.Success(),
        hostnameReachable = ProbeResult.Success(),
        androidInternetCapability = true,
        androidValidated = status == ConnectivityStatus.INTERNET_AVAILABLE,
        captivePortalDetected = status == ConnectivityStatus.CAPTIVE_PORTAL,
        mobileFallbackAvailable = false,
        status = status,
        confidence = confidence,
        evidence = emptyList(),
        startedAtEpochMs = 0L,
        finishedAtEpochMs = 0L,
    )

    private fun tomDe(
        resultado: List<StatusEstagio>,
        estagio: EstagioRede,
    ) = resultado.single { it.estagio == estagio }.tom

    // ── NivelConfianca.BAIXA sempre vence -- Incerto nos dois estágios, para todo status ──

    @Test
    fun `confianca baixa forca incerto nos dois estagios para todos os status`() {
        ConnectivityStatus.entries.forEach { status ->
            val resultado = ClassificadorConectividadeAoVivo.classificar(diagnostico(status, NivelConfianca.BAIXA))

            assertEquals(
                "status=$status deveria produzir Incerto no Wi-Fi com confianca BAIXA",
                TomDiagnostico.INCERTO,
                tomDe(resultado, EstagioRede.WIFI),
            )
            assertEquals(
                "status=$status deveria produzir Incerto no Provedor com confianca BAIXA",
                TomDiagnostico.INCERTO,
                tomDe(resultado, EstagioRede.PROVEDOR),
            )
        }
    }

    // ── INTERNET_AVAILABLE -- sucesso nos dois estagios (ALTA e MEDIA, unico caso real e ALTA) ──

    @Test
    fun `internet available com confianca alta produz sucesso nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.INTERNET_AVAILABLE, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `internet available com confianca media produz sucesso nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.INTERNET_AVAILABLE, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── GATEWAY_UNREACHABLE / NO_LOCAL_ADDRESS -- erro no Wi-Fi, Provedor neutro (sondagem
    // sequencial nunca alcança a etapa externa; não há evidência sobre o provedor) ──

    @Test
    fun `gateway unreachable com confianca alta e erro no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.GATEWAY_UNREACHABLE, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `gateway unreachable com confianca media e erro no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.GATEWAY_UNREACHABLE, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `no local address com confianca alta e erro no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.NO_LOCAL_ADDRESS, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `no local address com confianca media e erro no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.NO_LOCAL_ADDRESS, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── DNS_FAILURE / EXTERNAL_ROUTE_FAILURE -- erro no Provedor, sucesso no Wi-Fi ──

    @Test
    fun `dns failure com confianca alta e erro no provedor e sucesso no wifi`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.DNS_FAILURE, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `dns failure com confianca media e erro no provedor e sucesso no wifi`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.DNS_FAILURE, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `external route failure com confianca alta e erro no provedor e sucesso no wifi`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.EXTERNAL_ROUTE_FAILURE, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `external route failure com confianca media e erro no provedor e sucesso no wifi`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.EXTERNAL_ROUTE_FAILURE, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ERRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── CAPTIVE_PORTAL -- atencao no Wi-Fi (rede local exige o portal), neutro no Provedor ──

    @Test
    fun `captive portal com confianca alta e atencao no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.CAPTIVE_PORTAL, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.ATENCAO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `captive portal com confianca media e atencao no wifi e neutro no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.CAPTIVE_PORTAL, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.ATENCAO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.NEUTRO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── PARTIAL_CONNECTIVITY -- sucesso no Wi-Fi, atencao no Provedor ──

    @Test
    fun `partial connectivity com confianca alta e sucesso no wifi e atencao no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.PARTIAL_CONNECTIVITY, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ATENCAO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `partial connectivity com confianca media e sucesso no wifi e atencao no provedor`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.PARTIAL_CONNECTIVITY, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.SUCESSO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.ATENCAO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── WIFI_WITHOUT_INTERNET -- causa nao atribuivel a uma camada especifica (doc-comment
    // do proprio ConnectivityStatus): incerto nos dois estagios, mesmo com confianca alta.

    @Test
    fun `wifi without internet com confianca alta e incerto nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.WIFI_WITHOUT_INTERNET, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `wifi without internet com confianca media e incerto nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.WIFI_WITHOUT_INTERNET, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── INCONCLUSIVE / WIFI_DISCONNECTED -- incerto nos dois estagios (defensivo) ──

    @Test
    fun `inconclusive com confianca alta e incerto nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.INCONCLUSIVE, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `inconclusive com confianca media e incerto nos dois estagios`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.INCONCLUSIVE, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `wifi disconnected com confianca alta e incerto nos dois estagios (defensivo)`() {
        // Decisao 4.5 do plano -- o coordenador da Home so chama isto com Wi-Fi conectado,
        // mas o classificador trata defensivamente, sem lancar excecao.
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.WIFI_DISCONNECTED, NivelConfianca.ALTA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    @Test
    fun `wifi disconnected com confianca media e incerto nos dois estagios (defensivo)`() {
        val resultado =
            ClassificadorConectividadeAoVivo.classificar(
                diagnostico(ConnectivityStatus.WIFI_DISCONNECTED, NivelConfianca.MEDIA),
            )
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.WIFI))
        assertEquals(TomDiagnostico.INCERTO, tomDe(resultado, EstagioRede.PROVEDOR))
    }

    // ── contrato: sempre devolve exatamente os dois estagios, para qualquer combinacao ──

    @Test
    fun `sempre devolve exatamente os dois estagios para toda a matriz`() {
        ConnectivityStatus.entries.forEach { status ->
            NivelConfianca.entries.forEach { confianca ->
                val resultado = ClassificadorConectividadeAoVivo.classificar(diagnostico(status, confianca))
                assertEquals(
                    "status=$status confianca=$confianca deveria devolver 2 estagios",
                    setOf(EstagioRede.WIFI, EstagioRede.PROVEDOR),
                    resultado.map { it.estagio }.toSet(),
                )
            }
        }
    }
}
