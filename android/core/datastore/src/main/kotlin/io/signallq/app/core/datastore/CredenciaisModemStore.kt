package io.signallq.app.core.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Credencial cifrada de um equipamento local identificado. A identidade e o BSSID
 * nunca saem deste store: eles existem apenas para impedir que uma senha de um
 * gateway seja oferecida a outro gateway na mesma topologia.
 */
data class CredenciaisGatewayPerfil(
    val driverId: String,
    val host: String,
    val username: String,
    val password: String,
    val bssidVinculado: String?,
)

/**
 * Armazena credenciais do modem (username/password) em EncryptedSharedPreferences.
 * Usa AES-256 GCM via AndroidKeyStore — dados ilegiveis sem o device key.
 *
 * Fallback: se o AndroidKeyStore nao estiver disponivel (ex: testes unitarios com
 * Robolectric), usa SharedPreferences normal. Em device real o KeyStore sempre existe.
 */
class CredenciaisModemStore(
    private val context: Context,
) {
    private val prefs: SharedPreferences by lazy { criarPrefs() }

    private fun criarPrefs(): SharedPreferences =
        try {
            val masterKey =
                MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                context,
                "signallq_modem_credentials",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            // AndroidKeyStore indisponivel (Robolectric) — fallback sem criptografia.
            context.getSharedPreferences("signallq_modem_credentials_fallback", Context.MODE_PRIVATE)
        }

    private val _usernameFlow = MutableStateFlow(DEFAULT_USERNAME)
    private val _passwordFlow = MutableStateFlow(DEFAULT_PASSWORD)
    private val _bssidVinculadoFlow = MutableStateFlow<String?>(null)

    private var inicializado = false

    val usernameFlow: StateFlow<String> get() {
        garantirInicializado()
        return _usernameFlow
    }

    val passwordFlow: StateFlow<String> get() {
        garantirInicializado()
        return _passwordFlow
    }

    /**
     * BSSID do gateway ao qual a credencial acima foi vinculada via "Manter conectado"
     * (GH#527). Guardado junto da credencial no store criptografado — nunca no DataStore
     * plaintext — porque, a partir da API 26, o Android trata BSSID como dado sensivel
     * de localizacao. Null = nenhum vinculo ativo (autoconexao desligada ou revogada).
     */
    val bssidVinculadoFlow: StateFlow<String?> get() {
        garantirInicializado()
        return _bssidVinculadoFlow
    }

    @Synchronized
    private fun garantirInicializado() {
        if (inicializado) return
        _usernameFlow.value = prefs.getString(CHAVE_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        _passwordFlow.value = prefs.getString(CHAVE_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
        _bssidVinculadoFlow.value = prefs.getString(CHAVE_BSSID_VINCULADO, null)
        inicializado = true
    }

    fun salvarUsername(username: String) {
        garantirInicializado()
        prefs.edit().putString(CHAVE_USERNAME, username).apply()
        _usernameFlow.value = username
    }

    fun salvarPassword(password: String) {
        garantirInicializado()
        prefs.edit().putString(CHAVE_PASSWORD, password).apply()
        _passwordFlow.value = password
    }

    /** Vincula (ou revoga, com null) o BSSID do gateway a credencial salva. */
    fun salvarBssidVinculado(bssid: String?) {
        garantirInicializado()
        val editor = prefs.edit()
        if (bssid != null) editor.putString(CHAVE_BSSID_VINCULADO, bssid) else editor.remove(CHAVE_BSSID_VINCULADO)
        editor.apply()
        _bssidVinculadoFlow.value = bssid
    }

    /**
     * Salva um perfil por driver e host. Não reutiliza as chaves globais
     * legadas: assim uma ONT Nokia e um roteador TP-Link atrás dela podem
     * coexistir sem sobrescrever usuário, senha ou BSSID uma da outra.
     */
    fun salvarPerfil(
        driverId: String,
        host: String,
        username: String,
        password: String,
        bssidVinculado: String?,
    ) {
        require(driverId.isNotBlank()) { "driverId não pode ser vazio" }
        require(host.isNotBlank()) { "host não pode ser vazio" }
        val editor = prefs.edit()
        editor.putString(chavePerfil(driverId, host, CAMPO_USERNAME), username)
        editor.putString(chavePerfil(driverId, host, CAMPO_PASSWORD), password)
        if (bssidVinculado == null) {
            editor.remove(chavePerfil(driverId, host, CAMPO_BSSID))
        } else {
            editor.putString(chavePerfil(driverId, host, CAMPO_BSSID), bssidVinculado)
        }
        editor.apply()
    }

    /** Retorna somente o perfil exato; não há fallback entre drivers ou hosts. */
    fun lerPerfil(
        driverId: String,
        host: String,
    ): CredenciaisGatewayPerfil? {
        if (driverId.isBlank() || host.isBlank()) return null
        val username = prefs.getString(chavePerfil(driverId, host, CAMPO_USERNAME), null)
        val password = prefs.getString(chavePerfil(driverId, host, CAMPO_PASSWORD), null)
        if (username == null && password == null) return null
        return CredenciaisGatewayPerfil(
            driverId = driverId,
            host = host,
            username = username.orEmpty(),
            password = password.orEmpty(),
            bssidVinculado = prefs.getString(chavePerfil(driverId, host, CAMPO_BSSID), null),
        )
    }

    /**
     * Promove a credencial global antiga depois que o driver a confirmou numa
     * leitura autenticada. O destino não é sobrescrito: uma tentativa de
     * migração tardia jamais substitui um perfil já validado.
     */
    fun promoverPerfilLegadoSeNecessario(
        driverId: String,
        host: String,
    ): CredenciaisGatewayPerfil? {
        lerPerfil(driverId, host)?.let { return it }
        garantirInicializado()
        val legado = lerPerfil(DRIVER_LEGADO, host) ?: lerPerfil(DRIVER_LEGADO, HOST_LEGADO_DESCONHECIDO)
        val username = legado?.username ?: _usernameFlow.value
        val password = legado?.password ?: _passwordFlow.value
        if (username == DEFAULT_USERNAME && password.isEmpty()) return null
        val perfil =
            CredenciaisGatewayPerfil(
                driverId = driverId,
                host = host,
                username = username,
                password = password,
                bssidVinculado = legado?.bssidVinculado ?: _bssidVinculadoFlow.value,
            )
        salvarPerfil(
            driverId = perfil.driverId,
            host = perfil.host,
            username = perfil.username,
            password = perfil.password,
            bssidVinculado = perfil.bssidVinculado,
        )
        return perfil
    }

    /** Cria o perfil legado uma única vez, sem adivinhar o driver do equipamento. */
    fun migrarPerfilLegadoSeNecessario(host: String?) {
        if (prefs.getBoolean(CHAVE_PERFIL_LEGADO_MIGRADO, false)) return
        garantirInicializado()
        val hostLegado = host?.takeIf { it.isNotBlank() } ?: HOST_LEGADO_DESCONHECIDO
        if (_usernameFlow.value != DEFAULT_USERNAME || _passwordFlow.value.isNotEmpty() || _bssidVinculadoFlow.value != null) {
            salvarPerfil(
                driverId = DRIVER_LEGADO,
                host = hostLegado,
                username = _usernameFlow.value,
                password = _passwordFlow.value,
                bssidVinculado = _bssidVinculadoFlow.value,
            )
        }
        prefs.edit().putBoolean(CHAVE_PERFIL_LEGADO_MIGRADO, true).apply()
    }

    fun limparPerfis() {
        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith(PREFIXO_PERFIL) }
            .forEach(editor::remove)
        editor.apply()
    }

    /**
     * Migra credenciais plaintext do DataStore para o store criptografado.
     * Chamado uma vez pelo PreferenciasAppRepository na inicializacao.
     * Retorna true se houve migracao (para que o caller remova as chaves do DataStore).
     */
    fun migrarSeNecessario(
        usernamePlaintext: String?,
        passwordPlaintext: String?,
    ): Boolean {
        if (prefs.getBoolean(CHAVE_MIGRADO, false)) return false

        val temDadosParaMigrar = !usernamePlaintext.isNullOrBlank() || !passwordPlaintext.isNullOrBlank()
        if (temDadosParaMigrar) {
            val editor = prefs.edit()
            if (!usernamePlaintext.isNullOrBlank()) {
                editor.putString(CHAVE_USERNAME, usernamePlaintext)
                _usernameFlow.value = usernamePlaintext
            }
            if (!passwordPlaintext.isNullOrBlank()) {
                editor.putString(CHAVE_PASSWORD, passwordPlaintext)
                _passwordFlow.value = passwordPlaintext
            }
            editor.putBoolean(CHAVE_MIGRADO, true)
            editor.apply()
            inicializado = true
            return true
        }

        prefs.edit().putBoolean(CHAVE_MIGRADO, true).apply()
        return false
    }

    companion object {
        private const val CHAVE_USERNAME = "modem_username"
        private const val CHAVE_PASSWORD = "modem_password"
        private const val CHAVE_BSSID_VINCULADO = "gateway_bssid_vinculado"
        private const val CHAVE_MIGRADO = "migrado_do_datastore"
        private const val CHAVE_PERFIL_LEGADO_MIGRADO = "perfil_legado_migrado"
        private const val PREFIXO_PERFIL = "gateway_profile_"
        private const val CAMPO_USERNAME = "username"
        private const val CAMPO_PASSWORD = "password"
        private const val CAMPO_BSSID = "bssid"
        const val DEFAULT_USERNAME = "userAdmin"
        const val DEFAULT_PASSWORD = ""
        const val DRIVER_LEGADO = "legacy"
        const val HOST_LEGADO_DESCONHECIDO = "unknown"

        private fun chavePerfil(
            driverId: String,
            host: String,
            campo: String,
        ): String {
            val identity = "$driverId\u0000$host"
            val encoded =
                Base64.encodeToString(
                    identity.toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
                )
            return "${PREFIXO_PERFIL}${encoded}_$campo"
        }
    }
}
