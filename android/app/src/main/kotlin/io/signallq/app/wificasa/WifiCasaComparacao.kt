package io.signallq.app.wificasa

import io.signallq.app.core.database.wificasa.MarcadorMapeamentoEntity
import io.signallq.app.core.database.wificasa.TipoMarcadorMapeamento
import io.signallq.app.core.diagnostico.BandaWifi
import io.signallq.app.ui.screen.signalQuality

/**
 * Ordem de qualidade da categoria textual exibida (Excelente > Bom > Regular > Fraco) -- usada só
 * para decidir a seta melhorou/piorou/não mudou da comparação Antes×Depois (RF-08), não para
 * classificar sinal (isso continua sendo responsabilidade exclusiva de `signalQuality`/
 * `MetricClassifier`, AGENTS.md §8).
 */
private val ORDEM_CATEGORIA = listOf("Fraco", "Regular", "Bom", "Excelente")

private fun rankCategoria(categoria: String?): Int = categoria?.let { ORDEM_CATEGORIA.indexOf(it) } ?: -1

/**
 * Normaliza um rótulo para casamento entre marcadores de duas sessões -- trim + case-insensitive
 * (`.agents/architecture-plan.md`, seção 6, passo 7). "Sala", " sala ", "SALA" casam entre si;
 * "Sala 1" e "Sala 2" não.
 */
internal fun normalizarRotuloMapeamento(rotulo: String): String = rotulo.trim().lowercase()

private fun categoriaDoMarcador(marcador: MarcadorMapeamentoEntity): String? {
    val rssi = marcador.rssiDbm ?: return null
    val banda = runCatching { BandaWifi.valueOf(marcador.bandaWifi.orEmpty()) }.getOrDefault(BandaWifi.desconhecida)
    return signalQuality(rssi, banda)
}

/**
 * Casa os marcadores de cômodo (RF-08 nunca compara o marcador de roteador) de duas sessões pelo
 * rótulo normalizado e calcula melhorou/piorou/não mudou/novo/removido. Função pura, sem
 * Room/Compose -- testável isoladamente (`.agents/architecture-plan.md`, seção 11).
 */
internal fun compararMapeamentosWifiCasa(
    marcadoresAntes: List<MarcadorMapeamentoEntity>,
    marcadoresDepois: List<MarcadorMapeamentoEntity>,
): List<ComparacaoMarcadorUi> {
    val antesPorRotulo =
        marcadoresAntes
            .filter { it.tipo == TipoMarcadorMapeamento.COMODO }
            .associateBy { normalizarRotuloMapeamento(it.rotulo) }
    val depoisPorRotulo =
        marcadoresDepois
            .filter { it.tipo == TipoMarcadorMapeamento.COMODO }
            .associateBy { normalizarRotuloMapeamento(it.rotulo) }
    val chaves = antesPorRotulo.keys + depoisPorRotulo.keys

    return chaves
        .map { chave ->
            val antes = antesPorRotulo[chave]
            val depois = depoisPorRotulo[chave]
            val rotulo = depois?.rotulo ?: antes?.rotulo.orEmpty()
            val categoriaAntes = antes?.let(::categoriaDoMarcador)
            val categoriaDepois = depois?.let(::categoriaDoMarcador)
            val resultado =
                when {
                    antes == null -> ResultadoComparacaoMarcador.NOVO
                    depois == null -> ResultadoComparacaoMarcador.REMOVIDO
                    else -> {
                        val rankAntes = rankCategoria(categoriaAntes)
                        val rankDepois = rankCategoria(categoriaDepois)
                        when {
                            rankDepois > rankAntes -> ResultadoComparacaoMarcador.MELHOROU
                            rankDepois < rankAntes -> ResultadoComparacaoMarcador.PIOROU
                            else -> ResultadoComparacaoMarcador.NAO_MUDOU
                        }
                    }
                }
            ComparacaoMarcadorUi(
                rotulo = rotulo,
                categoriaAntes = categoriaAntes,
                categoriaDepois = categoriaDepois,
                resultado = resultado,
            )
        }.sortedBy { it.rotulo.lowercase() }
}
