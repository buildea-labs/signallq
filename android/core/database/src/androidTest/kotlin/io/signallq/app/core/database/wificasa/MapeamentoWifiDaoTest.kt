package io.signallq.app.core.database.wificasa

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.signallq.app.core.database.SignallQDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapeamentoWifiDaoTest {
    private lateinit var db: SignallQDatabase
    private lateinit var dao: MapeamentoWifiDao

    @Before
    fun criarBanco() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SignallQDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        dao = db.mapeamentoWifiDao()
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    private fun mapeamento(
        id: String = "map-1",
        nome: String = "Mapeamento 1",
        networkId: String? = "wifi-ssid:Casa",
        criadoEm: Long = 1_000L,
        atualizadoEm: Long = 1_000L,
        status: String = StatusMapeamentoWifi.EM_ANDAMENTO,
        comparadoComSessaoId: String? = null,
    ) = MapeamentoWifiEntity(
        id = id,
        nome = nome,
        networkId = networkId,
        criadoEmEpochMs = criadoEm,
        atualizadoEmEpochMs = atualizadoEm,
        status = status,
        comparadoComSessaoId = comparadoComSessaoId,
    )

    private fun marcador(
        id: String = "marc-1",
        mapeamentoId: String = "map-1",
        rotulo: String = "Sala",
        tipo: String = TipoMarcadorMapeamento.COMODO,
        posX: Float = 0.5f,
        posY: Float = 0.5f,
        rssiDbm: Int? = -55,
        bandaWifi: String? = "ghz5",
        criadoEm: Long = 1_000L,
    ) = MarcadorMapeamentoEntity(
        id = id,
        mapeamentoId = mapeamentoId,
        rotulo = rotulo,
        tipo = tipo,
        posX = posX,
        posY = posY,
        rssiDbm = rssiDbm,
        bandaWifi = bandaWifi,
        criadoEmEpochMs = criadoEm,
    )

    @Test
    fun salvarMapeamento_leDevolve() =
        runTest {
            dao.salvarMapeamento(mapeamento())

            val resultado = dao.observarMapeamento("map-1").first()
            assertNotNull(resultado)
            assertEquals("Mapeamento 1", resultado!!.nome)
            assertEquals(StatusMapeamentoWifi.EM_ANDAMENTO, resultado.status)
        }

    @Test
    fun salvarMarcador_leDevolve() =
        runTest {
            dao.salvarMapeamento(mapeamento())
            dao.salvarMarcador(marcador())

            val marcadores = dao.observarMarcadores("map-1").first()
            assertEquals(1, marcadores.size)
            assertEquals("Sala", marcadores[0].rotulo)
            assertEquals(-55, marcadores[0].rssiDbm)
        }

    @Test
    fun apagarMapeamento_marcadoresCascateiam() =
        runTest {
            dao.salvarMapeamento(mapeamento())
            dao.salvarMarcador(marcador(id = "marc-1"))
            dao.salvarMarcador(marcador(id = "marc-2", rotulo = "Quarto"))

            val antes = dao.observarMarcadores("map-1").first()
            assertEquals(2, antes.size)

            dao.apagarMapeamento("map-1")

            val depois = dao.observarMarcadores("map-1").first()
            assertTrue(depois.isEmpty())
            assertNull(dao.observarMapeamento("map-1").first())
        }

    @Test
    fun apagarMapeamentoBaseline_comparadoComSessaoIdViraNullNaSessaoIrma() =
        runTest {
            dao.salvarMapeamento(mapeamento(id = "antes", status = StatusMapeamentoWifi.BASELINE_COMPARADO))
            dao.salvarMapeamento(
                mapeamento(id = "depois", status = StatusMapeamentoWifi.CONCLUIDO, comparadoComSessaoId = "antes"),
            )

            dao.apagarMapeamento("antes")

            val depois = dao.observarMapeamento("depois").first()
            assertNotNull("apagar o baseline nao pode apagar a sessao 'depois'", depois)
            assertNull("vinculo deve virar NULL (SET_NULL), nao lixo apontando pra id inexistente", depois!!.comparadoComSessaoId)
        }

    @Test
    fun buscarBaselinePendente_filtraPorNetworkIdEStatus() =
        runTest {
            dao.salvarMapeamento(
                mapeamento(id = "map-a", networkId = "wifi-ssid:Casa", status = StatusMapeamentoWifi.BASELINE_PENDENTE),
            )
            dao.salvarMapeamento(
                mapeamento(id = "map-b", networkId = "wifi-ssid:Outra", status = StatusMapeamentoWifi.BASELINE_PENDENTE),
            )
            dao.salvarMapeamento(
                mapeamento(id = "map-c", networkId = "wifi-ssid:Casa", status = StatusMapeamentoWifi.CONCLUIDO),
            )

            val resultado = dao.buscarBaselinePendente("wifi-ssid:Casa")
            assertNotNull(resultado)
            assertEquals("map-a", resultado!!.id)
        }

    @Test
    fun buscarBaselinePendente_semCorrespondenciaRetornaNull() =
        runTest {
            dao.salvarMapeamento(
                mapeamento(id = "map-a", networkId = "wifi-ssid:Casa", status = StatusMapeamentoWifi.EM_ANDAMENTO),
            )

            assertNull(dao.buscarBaselinePendente("wifi-ssid:Casa"))
            assertNull(dao.buscarBaselinePendente("wifi-ssid:Inexistente"))
        }

    @Test
    fun vincularComparacao_atualizaOsDoisLadosEmTransacao() =
        runTest {
            dao.salvarMapeamento(mapeamento(id = "antes", status = StatusMapeamentoWifi.BASELINE_PENDENTE))
            dao.salvarMapeamento(mapeamento(id = "depois", status = StatusMapeamentoWifi.CONCLUIDO))

            dao.vincularComparacao(idDepois = "depois", idAntes = "antes", atualizadoEmEpochMs = 5_000L)

            val antes = dao.observarMapeamento("antes").first()
            val depois = dao.observarMapeamento("depois").first()
            assertEquals(StatusMapeamentoWifi.BASELINE_COMPARADO, antes!!.status)
            assertEquals("antes", depois!!.comparadoComSessaoId)
        }

    @Test
    fun atualizarRotuloMarcador_alteraRotuloEPreservaRssi() =
        runTest {
            dao.salvarMapeamento(mapeamento())
            dao.salvarMarcador(marcador(rotulo = "Sala", rssiDbm = -55))

            dao.atualizarRotuloMarcador("marc-1", "Sala de estar")

            val marcadores = dao.observarMarcadores("map-1").first()
            assertEquals(1, marcadores.size)
            assertEquals("Sala de estar", marcadores[0].rotulo)
            assertEquals(-55, marcadores[0].rssiDbm)
        }

    @Test
    fun buscarEmAndamento_retornaMaisRecente() =
        runTest {
            dao.salvarMapeamento(mapeamento(id = "map-antigo", status = StatusMapeamentoWifi.EM_ANDAMENTO, atualizadoEm = 100L))
            dao.salvarMapeamento(mapeamento(id = "map-recente", status = StatusMapeamentoWifi.EM_ANDAMENTO, atualizadoEm = 900L))
            dao.salvarMapeamento(mapeamento(id = "map-concluido", status = StatusMapeamentoWifi.CONCLUIDO, atualizadoEm = 950L))

            val resultado = dao.buscarEmAndamento()
            assertNotNull(resultado)
            assertEquals("map-recente", resultado!!.id)
        }
}
