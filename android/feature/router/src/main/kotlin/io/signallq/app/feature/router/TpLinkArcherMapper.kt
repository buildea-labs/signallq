package io.signallq.app.feature.router

import io.signallq.app.core.network.contracts.localdevice.ClientSnapshot
import io.signallq.app.core.network.contracts.localdevice.DataFreshness
import io.signallq.app.core.network.contracts.localdevice.DeviceCapabilities
import io.signallq.app.core.network.contracts.localdevice.DeviceType
import io.signallq.app.core.network.contracts.localdevice.LanSnapshot
import io.signallq.app.core.network.contracts.localdevice.LocalNetworkDeviceSnapshot
import io.signallq.app.core.network.contracts.localdevice.SupportLevel
import io.signallq.app.core.network.contracts.localdevice.WanSnapshot
import io.signallq.app.core.network.contracts.localdevice.WifiRadioSnapshot
import io.signallq.app.core.network.contracts.localdevice.WifiSnapshot
import org.json.JSONArray
import org.json.JSONObject

/** Maps only read-only Archer C6/A6 stok-luci evidence to the shared safe contract. */
object TpLinkArcherMapper {
    fun map(
        status: JSONObject,
        deviceInfo: JSONObject,
        topology: JSONObject?,
        capturedAtEpochMs: Long,
    ): LocalNetworkDeviceSnapshot? {
        val model = deviceInfo.optStringOrNull("model") ?: topology?.optStringOrNull("model")
        if (!isSupportedModel(model, topology?.optStringOrNull("name"))) return null

        val radios =
            listOf(
                radio(status, "2.4 GHz", "wireless_2g"),
                radio(status, "5 GHz", "wireless_5g"),
            ).filterNotNull()
        val clients = mutableListOf<ClientSnapshot>()
        clients += clients(status.optJSONArray("access_devices_wired"), "wired")
        clients += clients(topology?.optJSONArray("mesh_nclient_list"), null)

        return LocalNetworkDeviceSnapshot(
            deviceType = DeviceType.ROUTER,
            supportLevel = SupportLevel.LAB_VALIDATED,
            capabilities =
                DeviceCapabilities(
                    suportaWan = true,
                    suportaWifi = true,
                    suportaLan = true,
                    suportaClientes = true,
                    suportaDiagnosticoNativo = false,
                    suportaGerenciamento = false,
                ),
            vendor = "TP-Link",
            modelo = model,
            firmwareVersion =
                deviceInfo.optStringOrNull("firmware_version")
                    ?: deviceInfo.optStringOrNull("firmware"),
            fiber = null,
            wan =
                WanSnapshot(
                    ipExterno = status.optStringOrNull("wan_ipv4_ipaddr"),
                    gateway = status.optStringOrNull("wan_ipv4_gateway"),
                    dnsPrimario = status.optStringOrNull("wan_ipv4_pridns"),
                    dnsSecundario = status.optStringOrNull("wan_ipv4_snddns"),
                    tipoConexao = status.optStringOrNull("wan_ipv4_conntype"),
                    nomeInterface = null,
                    uptimeSegundos = status.optIntOrNull("wan_ipv4_uptime"),
                ),
            wifi = WifiSnapshot(radios),
            lan =
                LanSnapshot(
                    ipRoteador = status.optStringOrNull("lan_ipv4_ipaddr"),
                    mascara = status.optStringOrNull("lan_ipv4_netmask"),
                    dhcpHabilitado = status.optOnOff("lan_ipv4_dhcp_enable"),
                    faixaDhcpInicio = status.optStringOrNull("lan_ipv4_dhcp_start"),
                    faixaDhcpFim = status.optStringOrNull("lan_ipv4_dhcp_end"),
                ),
            clientes = clients.distinctBy { "${it.mac}|${it.ip}" },
            warnings = emptyList(),
            freshness = DataFreshness(capturedAtEpochMs, expirado = false),
        )
    }

    private fun radio(
        status: JSONObject,
        band: String,
        prefix: String,
    ): WifiRadioSnapshot? {
        val ssid = status.optStringOrNull("${prefix}_ssid")
        val channel = status.optIntOrNull("${prefix}_current_channel")
        if (ssid == null && channel == null) return null
        return WifiRadioSnapshot(
            banda = band,
            ssid = ssid,
            canal = channel,
            larguraCanal = status.optStringOrNull("${prefix}_htmode"),
            potenciaTx = status.optStringOrNull("${prefix}_txpower"),
            criptografia = status.optStringOrNull("${prefix}_encryption"),
            habilitado = status.optOnOff("${prefix}_enable"),
        )
    }

    private fun clients(
        array: JSONArray?,
        defaultConnection: String?,
    ): List<ClientSnapshot> =
        buildList {
            if (array == null) return@buildList
            for (index in 0 until array.length()) {
                val value = array.optJSONObject(index) ?: continue
                add(
                    ClientSnapshot(
                        mac = value.optStringOrNull("macaddr") ?: value.optStringOrNull("mac"),
                        ip = value.optStringOrNull("ipaddr") ?: value.optStringOrNull("ip"),
                        hostname = value.optStringOrNull("hostname"),
                        tipoConexao = value.optStringOrNull("wire_type") ?: defaultConnection,
                    ),
                )
            }
        }

    private fun isSupportedModel(
        model: String?,
        name: String?,
    ): Boolean =
        sequenceOf(model, name).filterNotNull().any {
            it.contains("Archer C6", ignoreCase = true) ||
                it.contains("Archer A6", ignoreCase = true) ||
                it.equals("ArcherA6v2", ignoreCase = true)
        }
}

private fun JSONObject.optStringOrNull(name: String): String? =
    optString(name, "").trim().takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

private fun JSONObject.optIntOrNull(name: String): Int? =
    if (!has(name) || isNull(name)) null else optInt(name)

private fun JSONObject.optOnOff(name: String): Boolean? =
    when (optStringOrNull(name)?.lowercase()) {
        "on", "true", "1", "enabled" -> true
        "off", "false", "0", "disabled" -> false
        else -> null
    }
