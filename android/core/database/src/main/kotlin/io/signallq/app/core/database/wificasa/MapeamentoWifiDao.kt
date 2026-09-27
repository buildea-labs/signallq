package io.signallq.app.core.database.wificasa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * DAO da feature "WiFi Casa" -- cobre as duas tabelas (`mapeamento_wifi`/`marcador_mapeamento`),
 * mesmo padrão de `ChatSessionDao` cobrindo `chat_sessions`+`chat_messages`
 * (`.agents/architecture-plan.md`, seção 5).
 */
@Dao
interface MapeamentoWifiDao {
    @Query("SELECT * FROM mapeamento_wifi ORDER BY atualizadoEmEpochMs DESC")
    fun observarMapeamentos(): Flow<List<MapeamentoWifiEntity>>

    @Query("SELECT * FROM mapeamento_wifi WHERE id = :id")
    fun observarMapeamento(id: String): Flow<MapeamentoWifiEntity?>

    @Query("SELECT * FROM marcador_mapeamento WHERE mapeamentoId = :mapeamentoId ORDER BY criadoEmEpochMs ASC")
    fun observarMarcadores(mapeamentoId: String): Flow<List<MarcadorMapeamentoEntity>>

    @Query("SELECT * FROM marcador_mapeamento WHERE mapeamentoId = :mapeamentoId ORDER BY criadoEmEpochMs ASC")
    suspend fun buscarMarcadores(mapeamentoId: String): List<MarcadorMapeamentoEntity>

    @Query("SELECT * FROM mapeamento_wifi WHERE id = :id")
    suspend fun buscarMapeamento(id: String): MapeamentoWifiEntity?

    /** RF-09: retomar mapeamento incompleto -- o mais recente ainda `em_andamento`. */
    @Query(
        "SELECT * FROM mapeamento_wifi WHERE status = '${StatusMapeamentoWifi.EM_ANDAMENTO}' " +
            "ORDER BY atualizadoEmEpochMs DESC LIMIT 1",
    )
    suspend fun buscarEmAndamento(): MapeamentoWifiEntity?

    /** RF-07: baseline pendente na mesma rede, candidato à comparação Antes×Depois. */
    @Query(
        "SELECT * FROM mapeamento_wifi WHERE status = '${StatusMapeamentoWifi.BASELINE_PENDENTE}' " +
            "AND networkId = :networkId ORDER BY atualizadoEmEpochMs DESC LIMIT 1",
    )
    suspend fun buscarBaselinePendente(networkId: String): MapeamentoWifiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarMapeamento(mapeamento: MapeamentoWifiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarMarcador(marcador: MarcadorMapeamentoEntity)

    @Query("DELETE FROM marcador_mapeamento WHERE id = :id")
    suspend fun apagarMarcador(id: String)

    /** RF-05: edita o rótulo (nome do cômodo) de um marcador já persistido, sem exigir remedição. */
    @Query("UPDATE marcador_mapeamento SET rotulo = :novoRotulo WHERE id = :marcadorId")
    suspend fun atualizarRotuloMarcador(
        marcadorId: String,
        novoRotulo: String,
    )

    @Query("DELETE FROM mapeamento_wifi WHERE id = :id")
    suspend fun apagarMapeamento(id: String)

    @Query("UPDATE mapeamento_wifi SET status = :status, atualizadoEmEpochMs = :atualizadoEmEpochMs WHERE id = :id")
    suspend fun atualizarStatus(
        id: String,
        status: String,
        atualizadoEmEpochMs: Long,
    )

    @Query("UPDATE mapeamento_wifi SET networkId = :networkId, atualizadoEmEpochMs = :atualizadoEmEpochMs WHERE id = :id")
    suspend fun atualizarNetworkId(
        id: String,
        networkId: String?,
        atualizadoEmEpochMs: Long,
    )

    /**
     * Vincula a sessão nova (depois) ao baseline (antes) e marca o baseline como comparado --
     * transação única (`.agents/architecture-plan.md`, seção 4.2, "Resolução do vínculo é lógica
     * de aplicação, não trigger de banco").
     */
    @Transaction
    suspend fun vincularComparacao(
        idDepois: String,
        idAntes: String,
        atualizadoEmEpochMs: Long,
    ) {
        atualizarComparadoComSessaoId(idDepois, idAntes, atualizadoEmEpochMs)
        atualizarStatus(idAntes, StatusMapeamentoWifi.BASELINE_COMPARADO, atualizadoEmEpochMs)
    }

    @Query(
        "UPDATE mapeamento_wifi SET comparadoComSessaoId = :comparadoComSessaoId, " +
            "atualizadoEmEpochMs = :atualizadoEmEpochMs WHERE id = :id",
    )
    suspend fun atualizarComparadoComSessaoId(
        id: String,
        comparadoComSessaoId: String?,
        atualizadoEmEpochMs: Long,
    )
}
