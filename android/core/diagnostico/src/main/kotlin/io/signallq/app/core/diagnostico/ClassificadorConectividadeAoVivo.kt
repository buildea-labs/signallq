package io.signallq.app.core.diagnostico

import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.core.network.contracts.connectivity.ConnectivityStatus
import io.signallq.app.core.network.contracts.topologia.NivelConfianca

/**
 * Estágio de rede que o badge "ao vivo" da Home distingue (Architecture Plan
 * "Status de conectividade ao vivo na Home", decisão 4.2): [WIFI] é a rede interna
 * (enlace/DHCP/gateway); [PROVEDOR] é a rota externa (DNS/rota externa/internet).
 */
enum class EstagioRede { WIFI, PROVEDOR }

/**
 * Tom de diagnóstico independente de UI — equivalente puro de `SignallQFeedbackTone`
 * (que vive em `:app`). `:core:diagnostico` não pode depender de um tipo de `:app`
 * (Architecture Plan, decisão 4.2 e gate condição 3); o mapper de `:app` traduz este
 * enum para `SignallQFeedbackTone` no ponto de composição.
 */
enum class TomDiagnostico { NEUTRO, SUCESSO, ATENCAO, ERRO, INCERTO }

/** Tom de diagnóstico atribuído a um [EstagioRede] específico, a partir de evidência real. */
data class StatusEstagio(
    val estagio: EstagioRede,
    val tom: TomDiagnostico,
)

/**
 * Classificador puro (sem Android, sem I/O) que traduz um [ConnectivityDiagnosis] —
 * produzido por `ConnectivityDiagnosisEngine`/`ConnectivityDiagnosisSource`
 * (`:coreNetwork`, GH#1512) — em um tom por estágio de rede (Wi-Fi vs. provedor),
 * para o badge "ao vivo" da Home (Architecture Plan, decisão 4.2).
 *
 * Regra dura (`AGENTS.md` §8, Architecture Plan seção 7): [NivelConfianca.BAIXA] em
 * qualquer diagnóstico força os dois estágios a [TomDiagnostico.INCERTO], mesmo que o
 * [ConnectivityStatus] resolvido sugira uma causa específica — nunca apresentar causa
 * raiz sem evidência suficiente. Essa checagem vem antes de qualquer mapeamento por
 * status.
 *
 * `when` sobre [ConnectivityStatus] é exaustivo de propósito (sem `else`): um valor
 * novo no enum precisa passar por revisão explícita deste mapeamento, nunca cair
 * silenciosamente em um branch genérico.
 */
object ClassificadorConectividadeAoVivo {
    fun classificar(diagnostico: ConnectivityDiagnosis): List<StatusEstagio> {
        if (diagnostico.confidence == NivelConfianca.BAIXA) {
            return incerto()
        }

        return when (diagnostico.status) {
            // Internet plena — os dois estágios confirmados por evidência forte.
            ConnectivityStatus.INTERNET_AVAILABLE ->
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.SUCESSO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.SUCESSO),
                )

            // Etapa que falhou é local (DHCP/gateway), antes de qualquer tentativa de
            // rota externa -- atribuível ao estágio Wi-Fi. A sondagem do
            // ConnectivityDiagnosisEngine é sequencial (gateway -> DNS -> rota externa,
            // Architecture Plan seção 2): quando o gateway está inalcançável (ou não há
            // endereço local), a sondagem nunca chega a testar DNS/rota externa. Não existe
            // nenhuma evidência sobre o provedor nesse cenário -- marcar PROVEDOR como
            // SUCESSO seria inventar um resultado positivo para uma etapa nunca alcançada
            // (AGENTS.md §8). PROVEDOR fica NEUTRO (não avaliado), não INCERTO -- INCERTO é
            // para quando avaliamos e a evidência foi ambígua, aqui simplesmente não
            // avaliamos nada.
            ConnectivityStatus.NO_LOCAL_ADDRESS,
            ConnectivityStatus.GATEWAY_UNREACHABLE,
            -> erroWifiSemEvidenciaExterna()

            // Gateway (Wi-Fi) e DNS já foram confirmados com sucesso antes desta etapa
            // (sondagem sequencial); é a etapa externa que falhou -- atribuível ao
            // provedor. Aqui SUCESSO no Wi-Fi é evidência real, não invenção.
            ConnectivityStatus.DNS_FAILURE,
            ConnectivityStatus.EXTERNAL_ROUTE_FAILURE,
            -> erroEm(EstagioRede.PROVEDOR)

            // Rede exige portal cativo -- há conectividade de enlace (Wi-Fi ok), mas a
            // internet real depende de uma ação fora do controle do roteador do usuário
            // (login/aceite). Não é "sem internet" nem culpa do provedor propriamente
            // dito -- tratamos como atenção no estágio Wi-Fi (é o roteador/rede local que
            // está exigindo o portal), sem alarmar como erro total.
            ConnectivityStatus.CAPTIVE_PORTAL ->
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.ATENCAO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.NEUTRO),
                )

            // Sinal misto (uma sondagem externa confirmou alcance, outra não) -- Wi-Fi
            // (gateway+DNS) já confirmado, o estágio afetado é o provedor, com atenção
            // (não erro) porque há conectividade parcial real, não ausência total.
            ConnectivityStatus.PARTIAL_CONNECTIVITY ->
                listOf(
                    StatusEstagio(EstagioRede.WIFI, TomDiagnostico.SUCESSO),
                    StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.ATENCAO),
                )

            // Gateway e DNS ok, mas a rota externa não confirma internet e a evidência não
            // permite atribuir a causa a uma camada específica (ver doc-comment do próprio
            // enum) -- honesto tratar como incerto nos dois estágios em vez de "chutar"
            // Wi-Fi ou provedor sem evidência suficiente.
            ConnectivityStatus.WIFI_WITHOUT_INTERNET -> incerto()

            // Evidência insuficiente para concluir qual camada falhou, ou Wi-Fi
            // desconectado (não deveria chegar aqui segundo a decisão 4.5 do plano --
            // tratado defensivamente como incerto, nunca lançando exceção).
            ConnectivityStatus.INCONCLUSIVE,
            ConnectivityStatus.WIFI_DISCONNECTED,
            -> incerto()
        }
    }

    private fun erroEm(estagio: EstagioRede): List<StatusEstagio> =
        listOf(
            StatusEstagio(estagio, TomDiagnostico.ERRO),
            StatusEstagio(estagioOposto(estagio), TomDiagnostico.SUCESSO),
        )

    /**
     * Wi-Fi com erro confirmado, mas sem nenhuma evidência sobre o provedor -- a sondagem
     * sequencial nunca alcançou a etapa externa (ver comentário do `when` para
     * [ConnectivityStatus.NO_LOCAL_ADDRESS]/[ConnectivityStatus.GATEWAY_UNREACHABLE]). Não
     * reusa [erroEm] porque aquela função assume "oposto = sucesso", suposição que só vale
     * quando a etapa oposta foi de fato testada.
     */
    private fun erroWifiSemEvidenciaExterna(): List<StatusEstagio> =
        listOf(
            StatusEstagio(EstagioRede.WIFI, TomDiagnostico.ERRO),
            StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.NEUTRO),
        )

    private fun estagioOposto(estagio: EstagioRede): EstagioRede =
        when (estagio) {
            EstagioRede.WIFI -> EstagioRede.PROVEDOR
            EstagioRede.PROVEDOR -> EstagioRede.WIFI
        }

    private fun incerto(): List<StatusEstagio> =
        listOf(
            StatusEstagio(EstagioRede.WIFI, TomDiagnostico.INCERTO),
            StatusEstagio(EstagioRede.PROVEDOR, TomDiagnostico.INCERTO),
        )
}
