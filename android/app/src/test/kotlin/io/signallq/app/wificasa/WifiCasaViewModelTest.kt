package io.signallq.app.wificasa

import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import io.mockk.every
import io.mockk.mockk
import io.signallq.app.core.database.wificasa.MapeamentoWifiDao
import io.signallq.app.core.database.wificasa.MapeamentoWifiEntity
import io.signallq.app.core.database.wificasa.MarcadorMapeamentoEntity
import io.signallq.app.core.database.wificasa.StatusMapeamentoWifi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fake em memória de [MapeamentoWifiDao] -- mesmo espírito de um DAO Room, sem precisar de
 * Robolectric/androidTest para exercitar a orquestração do [WifiCasaViewModel] (padrão adotado
 * porque testes de `MainViewModel` no módulo já evitam instanciar dependências Hilt/Room
 * completas, ver `MainViewModelHistoricoTest.kt`).
 */
private class FakeMapeamentoWifiDao : MapeamentoWifiDao {
    private val mapeamentos = MutableStateFlow<List<MapeamentoWifiEntity>>(emptyList())
    private val marcadores = MutableStateFlow<List<MarcadorMapeamentoEntity>>(emptyList())

    override fun observarMapeamentos(): Flow<List<MapeamentoWifiEntity>> =
        mapeamentos.map { it.sortedByDescending { m -> m.atualizadoEmEpochMs } }

    override fun observarMapeamento(id: String): Flow<MapeamentoWifiEntity?> = mapeamentos.map { list -> list.find { it.id == id } }

    override fun observarMarcadores(mapeamentoId: String): Flow<List<MarcadorMapeamentoEntity>> =
        marcadores.map { list -> list.filter { it.mapeamentoId == mapeamentoId }.sortedBy { it.criadoEmEpochMs } }

    override suspend fun buscarMarcadores(mapeamentoId: String): List<MarcadorMapeamentoEntity> =
        marcadores.value.filter { it.mapeamentoId == mapeamentoId }.sortedBy { it.criadoEmEpochMs }

    override suspend fun buscarMapeamento(id: String): MapeamentoWifiEntity? = mapeamentos.value.find { it.id == id }

    override suspend fun buscarEmAndamento(): MapeamentoWifiEntity? =
        mapeamentos.value
            .filter { it.status == StatusMapeamentoWifi.EM_ANDAMENTO }
            .maxByOrNull { it.atualizadoEmEpochMs }

    override suspend fun buscarBaselinePendente(networkId: String): MapeamentoWifiEntity? =
        mapeamentos.value
            .filter { it.status == StatusMapeamentoWifi.BASELINE_PENDENTE && it.networkId == networkId }
            .maxByOrNull { it.atualizadoEmEpochMs }

    override suspend fun salvarMapeamento(mapeamento: MapeamentoWifiEntity) {
        mapeamentos.update { atual -> atual.filterNot { it.id == mapeamento.id } + mapeamento }
    }

    override suspend fun salvarMarcador(marcador: MarcadorMapeamentoEntity) {
        marcadores.update { atual -> atual.filterNot { it.id == marcador.id } + marcador }
    }

    override suspend fun apagarMarcador(id: String) {
        marcadores.update { atual -> atual.filterNot { it.id == id } }
    }

    override suspend fun atualizarRotuloMarcador(
        marcadorId: String,
        novoRotulo: String,
    ) {
        marcadores.update { atual ->
            atual.map { if (it.id == marcadorId) it.copy(rotulo = novoRotulo) else it }
        }
    }

    override suspend fun apagarMapeamento(id: String) {
        mapeamentos.update { atual -> atual.filterNot { it.id == id } }
        marcadores.update { atual -> atual.filterNot { it.mapeamentoId == id } }
    }

    override suspend fun atualizarStatus(
        id: String,
        status: String,
        atualizadoEmEpochMs: Long,
    ) {
        mapeamentos.update { atual ->
            atual.map { if (it.id == id) it.copy(status = status, atualizadoEmEpochMs = atualizadoEmEpochMs) else it }
        }
    }

    override suspend fun atualizarNetworkId(
        id: String,
        networkId: String?,
        atualizadoEmEpochMs: Long,
    ) {
        mapeamentos.update { atual ->
            atual.map { if (it.id == id) it.copy(networkId = networkId, atualizadoEmEpochMs = atualizadoEmEpochMs) else it }
        }
    }

    override suspend fun atualizarComparadoComSessaoId(
        id: String,
        comparadoComSessaoId: String?,
        atualizadoEmEpochMs: Long,
    ) {
        mapeamentos.update { atual ->
            atual.map {
                if (it.id == id) it.copy(comparadoComSessaoId = comparadoComSessaoId, atualizadoEmEpochMs = atualizadoEmEpochMs) else it
            }
        }
    }

    fun mapeamentoAtual(id: String) = mapeamentos.value.find { it.id == id }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WifiCasaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun wifiInfoMock(
        rssi: Int = -55,
        ssid: String = "\"CasaWifi\"",
        bssid: String = "aa:bb:cc:dd:ee:ff",
        frequencia: Int = 5180,
        networkId: Int = 0,
    ): WifiInfo {
        val info = mockk<WifiInfo>()
        every { info.rssi } returns rssi
        every { info.linkSpeed } returns 200
        every { info.ssid } returns ssid
        every { info.bssid } returns bssid
        every { info.wifiStandard } returns 5
        every { info.frequency } returns frequencia
        every { info.networkId } returns networkId
        return info
    }

    private fun wifiManagerMock(connectionInfo: WifiInfo = wifiInfoMock()): WifiManager {
        val wifiManager = mockk<WifiManager>()
        every { wifiManager.isWifiEnabled } returns true
        every { wifiManager.connectionInfo } returns connectionInfo
        return wifiManager
    }

    private fun viewModel(
        dao: FakeMapeamentoWifiDao = FakeMapeamentoWifiDao(),
        wifiManager: WifiManager = wifiManagerMock(),
    ) = WifiCasaViewModel(dao = dao, wifiManager = wifiManager)

    @Test
    fun `sem mapeamento em andamento inicia na tela INICIAL`() =
        runTest(dispatcher) {
            val vm = viewModel()
            dispatcher.scheduler.runCurrent()

            assertEquals(WifiCasaTela.INICIAL, vm.uiState.value.telaAtual)
            assertEquals(false, vm.uiState.value.carregando)
        }

    @Test
    fun `iniciarNovoMapeamento cria mapeamento em andamento e vai para GRID`() =
        runTest(dispatcher) {
            val vm = viewModel()
            dispatcher.scheduler.runCurrent()

            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()

            val estado = vm.uiState.value
            assertEquals(WifiCasaTela.GRID, estado.telaAtual)
            assertNotNull(estado.mapeamentoAtual)
            assertEquals(StatusMapeamentoWifi.EM_ANDAMENTO, estado.mapeamentoAtual!!.status)
            assertEquals(false, estado.somenteLeitura)
        }

    @Test
    fun `retoma mapeamento em andamento existente direto na GRID`() =
        runTest(dispatcher) {
            val dao = FakeMapeamentoWifiDao()
            dao.salvarMapeamento(
                MapeamentoWifiEntity(
                    id = "map-retomado",
                    nome = "Mapeamento antigo",
                    networkId = null,
                    criadoEmEpochMs = 1_000L,
                    atualizadoEmEpochMs = 1_000L,
                    status = StatusMapeamentoWifi.EM_ANDAMENTO,
                ),
            )
            val vm = viewModel(dao = dao)
            dispatcher.scheduler.runCurrent()

            val estado = vm.uiState.value
            assertEquals(WifiCasaTela.GRID, estado.telaAtual)
            assertEquals("map-retomado", estado.mapeamentoAtual?.id)
        }

    @Test
    fun `capturar e confirmar marcador persiste rssi e categoria calculada`() =
        runTest(dispatcher) {
            val vm = viewModel(wifiManager = wifiManagerMock(connectionInfo = wifiInfoMock(rssi = -50)))
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()

            vm.iniciarCapturaMarcador(posX = 0.3f, posY = 0.4f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            assertEquals(WifiCasaTela.CAPTURA, vm.uiState.value.telaAtual)

            vm.atualizarRotuloCaptura("Sala")
            dispatcher.scheduler.runCurrent()
            assertTrue(
                vm.uiState.value.captura!!
                    .podeConfirmar,
            )

            vm.confirmarCaptura()
            dispatcher.scheduler.runCurrent()

            val estado = vm.uiState.value
            assertEquals(WifiCasaTela.GRID, estado.telaAtual)
            assertNull(estado.captura)
            assertEquals(1, estado.marcadores.size)
            assertEquals("Sala", estado.marcadores[0].rotulo)
            assertEquals(-50, estado.marcadores[0].rssiDbm)
            assertEquals("Excelente", estado.marcadores[0].categoria)
        }

    @Test
    fun `cancelarCaptura descarta o marcador e volta para GRID`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()

            vm.iniciarCapturaMarcador(posX = 0.1f, posY = 0.1f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            vm.cancelarCaptura()
            dispatcher.scheduler.runCurrent()

            val estado = vm.uiState.value
            assertEquals(WifiCasaTela.GRID, estado.telaAtual)
            assertNull(estado.captura)
            assertTrue(estado.marcadores.isEmpty())
        }

    @Test
    fun `removerMarcador tira o marcador da lista`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()
            vm.iniciarCapturaMarcador(0.2f, 0.2f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            vm.atualizarRotuloCaptura("Quarto")
            dispatcher.scheduler.runCurrent()
            vm.confirmarCaptura()
            dispatcher.scheduler.runCurrent()
            val idMarcador =
                vm.uiState.value.marcadores
                    .single()
                    .id

            vm.removerMarcador(idMarcador)
            dispatcher.scheduler.runCurrent()

            assertTrue(
                vm.uiState.value.marcadores
                    .isEmpty(),
            )
        }

    @Test
    fun `editarRotuloMarcador atualiza o rotulo mantendo o rssi ja capturado`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()
            vm.iniciarCapturaMarcador(0.2f, 0.2f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            vm.atualizarRotuloCaptura("Quarto")
            dispatcher.scheduler.runCurrent()
            vm.confirmarCaptura()
            dispatcher.scheduler.runCurrent()
            val marcadorOriginal =
                vm.uiState.value.marcadores
                    .single()

            vm.editarRotuloMarcador(marcadorOriginal.id, "Quarto do casal")
            dispatcher.scheduler.runCurrent()

            val marcadorEditado =
                vm.uiState.value.marcadores
                    .single()
            assertEquals("Quarto do casal", marcadorEditado.rotulo)
            assertEquals(marcadorOriginal.rssiDbm, marcadorEditado.rssiDbm)
        }

    @Test
    fun `editarRotuloMarcador com rotulo vazio nao altera o marcador`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()
            vm.iniciarCapturaMarcador(0.2f, 0.2f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            vm.atualizarRotuloCaptura("Quarto")
            dispatcher.scheduler.runCurrent()
            vm.confirmarCaptura()
            dispatcher.scheduler.runCurrent()
            val marcadorOriginal =
                vm.uiState.value.marcadores
                    .single()

            vm.editarRotuloMarcador(marcadorOriginal.id, "   ")
            dispatcher.scheduler.runCurrent()

            assertEquals(
                "Quarto",
                vm.uiState.value.marcadores
                    .single()
                    .rotulo,
            )
        }

    @Test
    fun `concluirMapeamento como baseline marca status BASELINE_PENDENTE`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()
            val id =
                vm.uiState.value.mapeamentoAtual!!
                    .id

            vm.concluirMapeamento(comoBaseline = true)
            dispatcher.scheduler.runCurrent()

            assertEquals(WifiCasaTela.INICIAL, vm.uiState.value.telaAtual)
        }

    @Test
    fun `concluir mapeamento com baseline pendente da mesma rede abre comparacao`() =
        runTest(dispatcher) {
            val dao = FakeMapeamentoWifiDao()
            val wifiManager = wifiManagerMock(connectionInfo = wifiInfoMock(bssid = "11:22:33:44:55:66"))
            val networkId = "wifi-bssid:11:22:33:44:55:66"
            dao.salvarMapeamento(
                MapeamentoWifiEntity(
                    id = "baseline",
                    nome = "Antes",
                    networkId = networkId,
                    criadoEmEpochMs = 1_000L,
                    atualizadoEmEpochMs = 1_000L,
                    status = StatusMapeamentoWifi.BASELINE_PENDENTE,
                ),
            )
            dao.salvarMarcador(
                MarcadorMapeamentoEntity(
                    id = "marc-antes",
                    mapeamentoId = "baseline",
                    rotulo = "Sala",
                    tipo = "comodo",
                    posX = 0.5f,
                    posY = 0.5f,
                    rssiDbm = -85,
                    bandaWifi = "ghz5",
                    criadoEmEpochMs = 1_000L,
                ),
            )

            val vm = viewModel(dao = dao, wifiManager = wifiManager)
            vm.atualizarPermissaoLocalizacao(true)
            dispatcher.scheduler.runCurrent()
            vm.iniciarNovoMapeamento()
            dispatcher.scheduler.runCurrent()
            vm.iniciarCapturaMarcador(0.5f, 0.5f, ehRoteador = false)
            dispatcher.scheduler.runCurrent()
            vm.atualizarRotuloCaptura("Sala")
            dispatcher.scheduler.runCurrent()
            vm.confirmarCaptura()
            dispatcher.scheduler.runCurrent()

            vm.concluirMapeamento(comoBaseline = false)
            dispatcher.scheduler.runCurrent()

            val estado = vm.uiState.value
            assertEquals(WifiCasaTela.COMPARACAO, estado.telaAtual)
            assertNotNull(estado.comparacao)
            assertEquals(1, estado.comparacao!!.size)
            assertEquals("Sala", estado.comparacao!![0].rotulo)
            assertEquals(StatusMapeamentoWifi.BASELINE_COMPARADO, dao.mapeamentoAtual("baseline")?.status)
        }
}
