package io.signallq.app.servicestatus

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.signallq.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class StatusServico(
    val id: String,
    val nome: String,
    val categoria: String,
    val elegivelParaAlerta: Boolean,
    val monitoramentoAtivo: Boolean,
)

data class IncidenteServico(
    val id: String,
    val serviceId: String,
    val state: String,
    val summary: String,
    val revision: Int,
)

data class StatusServicosUiState(
    val servicos: List<StatusServico> = emptyList(),
    val incidentes: List<IncidenteServico> = emptyList(),
    val seguindo: Set<String> = emptySet(),
    val carregando: Boolean = false,
    val atualizacaoIndisponivel: Boolean = false,
)

/** Consumidor somente de leitura da API de status operada pelo Linka. */
@Singleton
class ServiceStatusRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val prefs = context.getSharedPreferences("service_status", Context.MODE_PRIVATE)
        private val client = OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build()
        private val _uiState = MutableStateFlow(StatusServicosUiState(seguindo = subscriptions()))
        val uiState: StateFlow<StatusServicosUiState> = _uiState

        suspend fun atualizar(): Boolean =
            withContext(Dispatchers.IO) {
                _uiState.value = _uiState.value.copy(carregando = true)
                runCatching {
                    val servicos = parseServicos(get("catalog"))
                    val incidentes = parseIncidentes(get("incidents"))
                    _uiState.value = StatusServicosUiState(servicos, incidentes, subscriptions())
                    true
                }.getOrElse {
                    _uiState.value = _uiState.value.copy(carregando = false, atualizacaoIndisponivel = true)
                    false
                }
            }

        fun definirSeguimento(
            serviceId: String,
            ativo: Boolean,
        ) {
            val atual = subscriptions().toMutableSet()
            if (ativo) atual += serviceId else atual -= serviceId
            prefs.edit().putStringSet(KEY_SUBSCRIPTIONS, atual).apply()
            _uiState.value = _uiState.value.copy(seguindo = atual)
            ServiceStatusScheduler.atualizarAgendamento(context, atual.isNotEmpty())
        }

        /** Retorna somente incidentes novos/revisados dos serviços escolhidos. */
        suspend fun incidentesParaNotificar(): List<Pair<StatusServico, IncidenteServico>> {
            if (!atualizar()) return emptyList()
            val state = _uiState.value
            val vistos = JSONObject(prefs.getString(KEY_REVISIONS, "{}") ?: "{}")
            val novos =
                state.incidentes.filter { incident ->
                    incident.serviceId in state.seguindo &&
                        incident.state != "resolved" &&
                        vistos.optInt(incident.id, -1) != incident.revision
                }
            if (novos.isNotEmpty()) {
                novos.forEach { vistos.put(it.id, it.revision) }
                prefs.edit().putString(KEY_REVISIONS, vistos.toString()).apply()
            }
            return novos.mapNotNull { incident ->
                state.servicos.firstOrNull { it.id == incident.serviceId }?.let { it to incident }
            }
        }

        private fun subscriptions(): Set<String> = prefs.getStringSet(KEY_SUBSCRIPTIONS, emptySet()).orEmpty()

        private fun get(path: String): String {
            val request = Request.Builder().url("${BuildConfig.SERVICE_STATUS_API_URL}/$path").build()
            client.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "status API ${response.code}" }
                return response.body.string()
            }
        }

        private fun parseServicos(body: String): List<StatusServico> =
            JSONObject(body).getJSONArray("services").toObjects { item ->
                StatusServico(
                    id = item.getString("id"),
                    nome = item.getString("name"),
                    categoria = item.optString("category"),
                    elegivelParaAlerta = item.optBoolean("notification_eligible"),
                    monitoramentoAtivo = item.optBoolean("monitoring_enabled"),
                )
            }

        private fun parseIncidentes(body: String): List<IncidenteServico> =
            JSONObject(body).getJSONArray("incidents").toObjects { item ->
                IncidenteServico(
                    id = item.getString("id"),
                    serviceId = item.getString("service_id"),
                    state = item.optString("state"),
                    summary = item.optString("summary"),
                    revision = item.optInt("revision", 0),
                )
            }

        private fun <T> JSONArray.toObjects(map: (JSONObject) -> T): List<T> =
            (0 until length()).map { map(getJSONObject(it)) }

        private companion object {
            const val KEY_SUBSCRIPTIONS = "subscriptions"
            const val KEY_REVISIONS = "incident_revisions"
        }
    }
