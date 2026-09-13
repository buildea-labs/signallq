@file:Suppress("ComplexCondition", "MagicNumber", "ReturnCount", "ThrowsCount")

package io.signallq.app.feature.router

import io.signallq.app.core.network.contracts.localdevice.LocalNetworkDeviceSnapshot
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Read-only LAN driver for the observed Archer C6/A6 v2 stok-luci firmware.
 *
 * The administrative UI supplies only a password; this driver deliberately owns the
 * protocol username (admin) and never accepts, persists, logs or returns it.
 */
class TpLinkArcherC6Driver private constructor(
    private val client: TpLinkStokLuciClient,
) {
    constructor(host: String) : this(newClient(host, UrlConnectionTpLinkTransport(host)))

    internal constructor(host: String, transport: TpLinkHttpTransport) : this(newClient(host, transport))

    fun loginAndRead(
        password: String,
        capturedAtEpochMs: Long = System.currentTimeMillis(),
    ): TpLinkReadResult {
        if (password.isEmpty()) return TpLinkReadResult.Failure(TpLinkFailure.INVALID_CREDENTIALS)
        return try {
            client.login(password)
            val status = client.read("status", "all")
            val deviceInfo = client.read("cloud_account", "get_deviceInfo")
            val topology = runCatching { client.read("onemesh_network", "mesh_topology") }.getOrNull()
            val snapshot =
                TpLinkArcherMapper.map(status, deviceInfo, topology, capturedAtEpochMs)
                    ?: return TpLinkReadResult.Failure(TpLinkFailure.UNSUPPORTED_MODEL)
            TpLinkReadResult.Success(snapshot)
        } catch (error: TpLinkProtocolException) {
            client.invalidate()
            TpLinkReadResult.Failure(error.failure)
        } catch (_: IOException) {
            client.invalidate()
            TpLinkReadResult.Failure(TpLinkFailure.COMMUNICATION)
        } catch (_: Exception) {
            client.invalidate()
            TpLinkReadResult.Failure(TpLinkFailure.INVALID_RESPONSE)
        }
    }

    fun invalidateSession() = client.invalidate()

    companion object {
        internal fun newClient(
            host: String,
            transport: TpLinkHttpTransport,
        ): TpLinkStokLuciClient {
            require(isPrivateOrLoopbackIpv4(host)) { "host deve ser um IPv4 privado/local" }
            return TpLinkStokLuciClient(transport)
        }

        /**
         * Credential-free family probe. It verifies the two stok-luci bootstrap
         * endpoints only; a model is considered C6/A6 only after [loginAndRead].
         */
        fun probe(host: String): TpLinkProbeResult =
            try {
                newClient(host, UrlConnectionTpLinkTransport(host)).probe()
            } catch (_: IOException) {
                TpLinkProbeResult.NOT_SUPPORTED
            } catch (_: IllegalArgumentException) {
                TpLinkProbeResult.NOT_SUPPORTED
            }
    }
}

sealed interface TpLinkReadResult {
    data class Success(
        val snapshot: LocalNetworkDeviceSnapshot,
    ) : TpLinkReadResult

    data class Failure(
        val reason: TpLinkFailure,
    ) : TpLinkReadResult
}

/** Closed internal cause set. UI must translate it once and never expose HTTP/crypto detail. */
enum class TpLinkFailure { INVALID_CREDENTIALS, SESSION_EXPIRED, UNSUPPORTED_MODEL, COMMUNICATION, INVALID_RESPONSE }

enum class TpLinkProbeResult(
    val driverId: String?,
) {
    STOK_LUCI_PASSWORD_ONLY("tplink-stok-luci"),
    NOT_SUPPORTED(null),
}

internal class TpLinkProtocolException(
    val failure: TpLinkFailure,
) : IOException()

internal interface TpLinkHttpTransport {
    @Throws(IOException::class)
    fun post(
        path: String,
        body: String,
        cookie: String? = null,
    ): TpLinkHttpResponse
}

internal data class TpLinkHttpResponse(
    val code: Int,
    val body: String,
    val sysauth: String?,
)

/** No redirects: credentials may only ever be posted to the validated LAN origin. */
internal class UrlConnectionTpLinkTransport(
    host: String,
) : TpLinkHttpTransport {
    private val baseUrl = "http://$host"

    override fun post(
        path: String,
        body: String,
        cookie: String?,
    ): TpLinkHttpResponse {
        val connection =
            (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                instanceFollowRedirects = false
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
                if (cookie != null) setRequestProperty("Cookie", "sysauth=$cookie")
            }
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        val auth =
            connection.headerFields["Set-Cookie"]?.firstNotNullOfOrNull { header ->
                Regex("(?:^|;)\\s*sysauth=([^;]+)").find(header)?.groupValues?.get(1)
            }
        connection.disconnect()
        return TpLinkHttpResponse(code, text, auth)
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 15_000
    }
}

internal class TpLinkStokLuciClient(
    private val transport: TpLinkHttpTransport,
) {
    private var session: Session? = null

    fun login(password: String) {
        val keys = plain("/cgi-bin/luci/;stok=/login?form=keys")
        val passwordKey = keys.optJSONObject("data")?.optJSONArray("password") ?: invalidResponse()
        val passwordModulus = passwordKey.optString(0).takeIf { it.isNotBlank() } ?: invalidResponse()
        val passwordExponent = passwordKey.optString(1).takeIf { it.isNotBlank() } ?: invalidResponse()
        val auth = plain("/cgi-bin/luci/;stok=/login?form=auth")
        val authData = auth.optJSONObject("data") ?: invalidResponse()
        val signingKey = authData.optJSONArray("key") ?: invalidResponse()
        val signingModulus = signingKey.optString(0).takeIf { it.isNotBlank() } ?: invalidResponse()
        val signingExponent = signingKey.optString(1).takeIf { it.isNotBlank() } ?: invalidResponse()
        val sequence = authData.optLong("seq", Long.MIN_VALUE).takeIf { it != Long.MIN_VALUE } ?: invalidResponse()

        val key = TpLinkStokLuciCrypto.decimalAscii16()
        val iv = TpLinkStokLuciCrypto.decimalAscii16()
        val passwordHash = TpLinkStokLuciCrypto.md5Hex(PROTOCOL_USERNAME + password)
        val encryptedPassword = TpLinkStokLuciCrypto.rsaPkcs1Hex(passwordModulus, passwordExponent, password)
        val data = TpLinkStokLuciCrypto.encrypt(key, iv, "operation=login&password=$encryptedPassword")
        val signPlain = "k=$key&i=$iv&h=$passwordHash&s=${sequence + data.length}"
        val sign =
            signPlain.chunked(SIGN_CHUNK_LENGTH).joinToString("") {
                TpLinkStokLuciCrypto.rsaPkcs1Hex(signingModulus, signingExponent, it)
            }
        val response =
            transport.post(
                "/cgi-bin/luci/;stok=/login?form=login",
                "sign=${encode(sign)}&data=${encode(data)}",
            )
        if (response.code == 401 || response.code == 403) throw TpLinkProtocolException(TpLinkFailure.INVALID_CREDENTIALS)
        val payload = decryptEnvelope(response.body, key, iv)
        if (!payload.optBoolean("success", false)) throw TpLinkProtocolException(TpLinkFailure.INVALID_CREDENTIALS)
        val stok = payload.optJSONObject("data")?.optString("stok").orEmpty()
        val sysauth = response.sysauth.orEmpty()
        if (stok.isBlank() || sysauth.isBlank()) throw TpLinkProtocolException(TpLinkFailure.INVALID_RESPONSE)
        session = Session(key, iv, passwordHash, sequence, stok, sysauth)
    }

    fun probe(): TpLinkProbeResult {
        val keys = plain("/cgi-bin/luci/;stok=/login?form=keys")
        val password = keys.optJSONObject("data")?.optJSONArray("password")
        if (!keys.optBoolean("success", false) || password?.length() != 2 || password.optString(0).length < 128) {
            return TpLinkProbeResult.NOT_SUPPORTED
        }
        val auth = plain("/cgi-bin/luci/;stok=/login?form=auth")
        val authData = auth.optJSONObject("data")
        val signing = authData?.optJSONArray("key")
        return if (
            auth.optBoolean("success", false) &&
            signing?.length() == 2 &&
            signing.optString(0).length >= 64 &&
            authData.optLong("seq", Long.MIN_VALUE) != Long.MIN_VALUE
        ) {
            TpLinkProbeResult.STOK_LUCI_PASSWORD_ONLY
        } else {
            TpLinkProbeResult.NOT_SUPPORTED
        }
    }

    fun read(
        category: String,
        form: String,
    ): JSONObject {
        val active = session ?: throw TpLinkProtocolException(TpLinkFailure.SESSION_EXPIRED)
        val data = TpLinkStokLuciCrypto.encrypt(active.key, active.iv, "operation=read")
        val sign = "h=${active.passwordHash}&s=${active.sequence + data.length}"
        val response =
            transport.post(
                "/cgi-bin/luci/;stok=${encode(active.stok)}/admin/$category?form=$form",
                "sign=${encode(sign)}&data=${encode(data)}",
                active.sysauth,
            )
        if (response.code == 401 || response.code == 403) throw TpLinkProtocolException(TpLinkFailure.SESSION_EXPIRED)
        val payload = decryptEnvelope(response.body, active.key, active.iv)
        if (!payload.optBoolean("success", false)) throw TpLinkProtocolException(TpLinkFailure.INVALID_RESPONSE)
        return payload.optJSONObject("data") ?: invalidResponse()
    }

    fun invalidate() {
        session = null
    }

    private fun plain(path: String): JSONObject {
        val response = transport.post(path, "operation=read")
        if (response.code !in 200..299) throw TpLinkProtocolException(TpLinkFailure.COMMUNICATION)
        return parse(response.body)
    }

    private fun decryptEnvelope(
        body: String,
        key: String,
        iv: String,
    ): JSONObject =
        try {
            val encrypted = parse(body).optString("data").takeIf { it.isNotBlank() } ?: invalidResponse()
            parse(TpLinkStokLuciCrypto.decrypt(key, iv, encrypted))
        } catch (error: TpLinkProtocolException) {
            throw error
        } catch (_: Exception) {
            throw TpLinkProtocolException(TpLinkFailure.INVALID_RESPONSE)
        }

    private fun parse(body: String): JSONObject =
        try {
            JSONObject(body)
        } catch (_: Exception) {
            throw TpLinkProtocolException(TpLinkFailure.INVALID_RESPONSE)
        }

    private fun invalidResponse(): Nothing = throw TpLinkProtocolException(TpLinkFailure.INVALID_RESPONSE)

    private data class Session(
        val key: String,
        val iv: String,
        val passwordHash: String,
        val sequence: Long,
        val stok: String,
        val sysauth: String,
    )

    private companion object {
        const val PROTOCOL_USERNAME = "admin"
        const val SIGN_CHUNK_LENGTH = 53

        fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    }
}

private fun isPrivateOrLoopbackIpv4(host: String): Boolean {
    val parts = host.split('.')
    if (parts.size != 4) return false
    val values = parts.map { it.toIntOrNull() ?: return false }
    if (values.any { it !in 0..255 }) return false
    return values[0] == 10 ||
        values[0] == 127 ||
        values[0] == 192 &&
        values[1] == 168 ||
        values[0] == 172 &&
        values[1] in 16..31 ||
        values[0] == 169 &&
        values[1] == 254
}
