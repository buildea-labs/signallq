package io.signallq.app.conectividade

import io.signallq.app.core.diagnostico.ClassificadorConectividadeAoVivo
import io.signallq.app.core.network.connectivity.ConnectivityDiagnosisSource
import io.signallq.app.core.network.contracts.connectivity.ConnectivityDiagnosis
import io.signallq.app.di.ApplicationScope
import io.signallq.app.ui.screen.Inicio2StatusAoVivo
import io.signallq.app.ui.screen.Inicio2StatusAoVivoMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordenador do polling foreground do badge "ao vivo" da Home (Architecture Plan "Status de
 * conectividade ao vivo na Home", seção 6). Consome [ConnectivityDiagnosisSource] diretamente
 * (nunca `ConnectivityDiagnosisRepository` de `:feature:speedtest` -- decisão 4.3: o polling
 * ambiente não deve inundar `ConnectivityDiagnosisHistoryDao` com ruído).
 *
 * Não vive em `MainViewModel` (regra de higiene, seção 4.2 -- arquivo já é dívida crítica):
 * este componente concentra sozinho a responsabilidade de "sondar + classificar + expor
 * estado ao vivo", chamado de `AppShell`/`Inicio2Screen` via wiring simples, no mesmo espírito
 * de [io.signallq.app.core.network.MonitorRede] (`iniciar()`/`encerrar()` explícitos, sem
 * `hiltViewModel()` em Composable leaf -- este app é 100% data-driven, ver comentário em
 * `MainActivity.kt`).
 *
 * Loop nunca fixed-rate: só agenda a próxima rodada depois que [ConnectivityDiagnosisSource
 * .diagnosticar] retorna (seção 6/7). Exceção inesperada vira [Inicio2StatusAoVivoMapper
 * .incerto] em vez de derrubar a Home (seção 7).
 */
@Singleton
class StatusConectividadeAoVivoCoordinator
    @Inject
    constructor(
        private val connectivityDiagnosisSource: ConnectivityDiagnosisSource,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) {
        private val _status = MutableStateFlow<Inicio2StatusAoVivo?>(null)

        /** `null` = carregando (sem 1ª leitura ainda, ou acabou de (re)iniciar) -- regra "sem
         *  staleness" (seção 6): nunca reexibe o último valor como se fosse atual. */
        val status: StateFlow<Inicio2StatusAoVivo?> = _status.asStateFlow()

        private val _ultimoDiagnostico = MutableStateFlow<ConnectivityDiagnosis?>(null)

        /** Evidência bruta da última rodada bem-sucedida -- consumida só pela sheet de
         *  explicação por estágio (seção 5, "detalhes técnicos"), nunca pela trilha/Hero
         *  (que só conhecem [status], já traduzido). `null` em qualquer rodada com exceção
         *  inesperada, para não expor evidência de uma rodada anterior como se fosse atual. */
        val ultimoDiagnostico: StateFlow<ConnectivityDiagnosis?> = _ultimoDiagnostico.asStateFlow()

        private var job: Job? = null

        /** Idempotente: chamar de novo com uma rodada já em andamento não inicia uma segunda
         *  (seção 6: nunca sondagens sobrepostas). */
        fun iniciar() {
            if (job?.isActive == true) return
            _status.value = null
            _ultimoDiagnostico.value = null
            job =
                applicationScope.launch {
                    while (isActive) {
                        executarRodada()
                        delay(INTERVALO_ENTRE_RODADAS_MS)
                    }
                }
        }

        /** Para o loop e volta ao estado "carregando" -- ao reentrar na Home/foreground, a
         *  próxima [iniciar] começa do zero, nunca mostrando o último valor como atual. */
        fun parar() {
            job?.cancel()
            job = null
            _status.value = null
            _ultimoDiagnostico.value = null
        }

        private suspend fun executarRodada() {
            runCatching { connectivityDiagnosisSource.diagnosticar() }
                .onSuccess { diagnostico ->
                    _status.value = Inicio2StatusAoVivoMapper.mapear(ClassificadorConectividadeAoVivo.classificar(diagnostico))
                    _ultimoDiagnostico.value = diagnostico
                }.onFailure {
                    // AGENTS.md, seção 8/Architecture Plan seção 7: exceção inesperada nunca
                    // derruba a Home nem vira sucesso silencioso -- vira Incerto explícito, sem
                    // evidência bruta associada (nenhum ConnectivityDiagnosis foi produzido).
                    _status.value = Inicio2StatusAoVivoMapper.incerto()
                    _ultimoDiagnostico.value = null
                }
        }

        companion object {
            /** Intervalo entre rodadas (Architecture Plan, seção 6 -- recomendação de Breno,
             *  faixa aceitável 4-6s). */
            internal const val INTERVALO_ENTRE_RODADAS_MS = 5_000L
        }
    }
