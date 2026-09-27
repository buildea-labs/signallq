package io.signallq.app.core.database.wificasa

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Valores possíveis de [MarcadorMapeamentoEntity.tipo]. */
object TipoMarcadorMapeamento {
    const val COMODO = "comodo"
    const val ROTEADOR = "roteador"
}

/**
 * Um marcador (cômodo ou roteador) dentro de uma sessão de [MapeamentoWifiEntity].
 *
 * Tabela: marcador_mapeamento
 *
 * `ON DELETE CASCADE` -- apagar o mapeamento apaga seus próprios marcadores (mesmo padrão de
 * `chat_messages` em relação a `chat_sessions`).
 *
 * [posX]/[posY] são normalizados (0f..1f) -- o grid é declarado livremente pelo usuário, sem
 * planta real nem escala (spec 3.1). A UI multiplica pelo tamanho real do canvas ao renderizar.
 *
 * **Categoria de sinal (Excelente/Bom/Regular/Fraco) nunca é persistida aqui.** [rssiDbm] +
 * [bandaWifi] são o dado medido; a categoria é inferência determinística recalculada em runtime
 * via `signalQuality`/`classificarRssiWifiLocal` (`core/diagnostico`, `MetricClassifier`) --
 * ver AGENTS.md §8 e critério de aceite da spec ("nenhuma alteração no motor de classificação").
 */
@Entity(
    tableName = "marcador_mapeamento",
    foreignKeys = [
        ForeignKey(
            entity = MapeamentoWifiEntity::class,
            parentColumns = ["id"],
            childColumns = ["mapeamentoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["mapeamentoId"]),
        Index(value = ["mapeamentoId", "criadoEmEpochMs"]),
    ],
)
data class MarcadorMapeamentoEntity(
    @PrimaryKey
    val id: String,
    val mapeamentoId: String,
    val rotulo: String,
    /** Ver [TipoMarcadorMapeamento]. */
    val tipo: String,
    val posX: Float,
    val posY: Float,
    /** `null` = medição não concluída (ex.: marcador de roteador posicionado sem amostragem). */
    val rssiDbm: Int?,
    /** Nome do enum `BandaWifi` (`core/diagnostico`), mesmo padrão de `medicao.bandaWifi`. */
    val bandaWifi: String?,
    val criadoEmEpochMs: Long,
)
