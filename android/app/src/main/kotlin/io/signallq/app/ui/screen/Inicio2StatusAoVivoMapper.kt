package io.signallq.app.ui.screen

import io.signallq.app.core.diagnostico.EstagioRede
import io.signallq.app.core.diagnostico.StatusEstagio
import io.signallq.app.core.diagnostico.TomDiagnostico
import io.signallq.app.ui.component.SignallQFeedbackTone

/**
 * Traduz `List<StatusEstagio>` (`ClassificadorConectividadeAoVivo`, `:core:diagnostico`,
 * um [TomDiagnostico] puro sem conhecimento de UI) em [Inicio2StatusAoVivo] (tipo de `:app`,
 * `SignallQFeedbackTone`) -- Architecture Plan "Status de conectividade ao vivo na Home",
 * decisão 4.2: a tradução do enum puro para o enum de UI é responsabilidade de `:app`,
 * nunca de `:core:diagnostico`.
 */
internal object Inicio2StatusAoVivoMapper {
    // Node ids da trilha (Inicio2ConnectionTrail) afetados por cada estágio. [EstagioRede.WIFI]
    // cobre tanto o roteador ("Equipamento") quanto o nome da rede ("Wi-Fi") -- os dois
    // representam a mesma camada local sondada (gateway). [EstagioRede.PROVEDOR] cobre só
    // "Internet" (rota externa/DNS).
    private val nodeIdsPorEstagio =
        mapOf(
            EstagioRede.WIFI to listOf("Equipamento", "Wi-Fi"),
            EstagioRede.PROVEDOR to listOf("Internet"),
        )

    /** Ranking para "pior caso" (Architecture Plan, decisão 4.4): ERRO > ATENÇÃO > INCERTO >
     *  SUCESSO > NEUTRO. */
    private fun TomDiagnostico.rank(): Int =
        when (this) {
            TomDiagnostico.NEUTRO -> 0
            TomDiagnostico.SUCESSO -> 1
            TomDiagnostico.INCERTO -> 2
            TomDiagnostico.ATENCAO -> 3
            TomDiagnostico.ERRO -> 4
        }

    private fun TomDiagnostico.paraFeedbackTone(): SignallQFeedbackTone =
        when (this) {
            TomDiagnostico.NEUTRO -> SignallQFeedbackTone.Neutral
            TomDiagnostico.SUCESSO -> SignallQFeedbackTone.Success
            TomDiagnostico.ATENCAO -> SignallQFeedbackTone.Warning
            TomDiagnostico.ERRO -> SignallQFeedbackTone.Error
            TomDiagnostico.INCERTO -> SignallQFeedbackTone.Incerto
        }

    fun mapear(estagios: List<StatusEstagio>): Inicio2StatusAoVivo {
        if (estagios.isEmpty()) return incerto()

        val porEstagio =
            buildMap {
                estagios.forEach { estagio ->
                    nodeIdsPorEstagio[estagio.estagio]?.forEach { nodeId ->
                        put(nodeId, estagio.tom.paraFeedbackTone())
                    }
                }
            }
        val piorRank = estagios.maxOf { it.tom.rank() }
        val piorTom = estagios.first { it.tom.rank() == piorRank }.tom
        val estagiosNoPior = estagios.filter { it.tom.rank() == piorRank }
        // Nunca atribuir causa quando o pior tom é Incerto (regra dura, AGENTS.md seção 8:
        // nunca apresentar causa raiz sem evidência suficiente) ou quando dois estágios
        // empatam no mesmo tom pior (não inventar uma causa combinada).
        val causaPrincipal =
            if (piorTom == TomDiagnostico.INCERTO) {
                null
            } else {
                estagiosNoPior.singleOrNull()?.estagio
            }
        return Inicio2StatusAoVivo(
            porEstagio = porEstagio,
            geral = piorTom.paraFeedbackTone(),
            causaPrincipal = causaPrincipal,
        )
    }

    /** Falha inesperada na sondagem (Architecture Plan, seção 7) -- nunca derruba a Home,
     *  vira Incerto nos estágios conhecidos, sem causa atribuída. */
    fun incerto(): Inicio2StatusAoVivo =
        Inicio2StatusAoVivo(
            porEstagio = nodeIdsPorEstagio.values.flatten().associateWith { SignallQFeedbackTone.Incerto },
            geral = SignallQFeedbackTone.Incerto,
            causaPrincipal = null,
        )
}
