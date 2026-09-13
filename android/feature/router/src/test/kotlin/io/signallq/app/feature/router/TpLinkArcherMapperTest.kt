package io.signallq.app.feature.router

import io.signallq.app.core.network.contracts.localdevice.DeviceType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TpLinkArcherMapperTest {
    @Test
    fun `maps C6 status without retaining wifi psk`() {
        val snapshot =
            TpLinkArcherMapper.map(
                status =
                    JSONObject(
                        """{
                        "wan_ipv4_ipaddr":"192.168.1.64", "wan_ipv4_gateway":"192.168.1.254",
                        "wan_ipv4_pridns":"1.1.1.1", "wan_ipv4_conntype":"dhcp",
                        "lan_ipv4_ipaddr":"192.168.0.1", "lan_ipv4_netmask":"255.255.255.0",
                        "lan_ipv4_dhcp_enable":"On", "wireless_2g_ssid":"Casa",
                        "wireless_2g_current_channel":10, "wireless_2g_psk_key":"never-copy-this",
                        "wireless_5g_ssid":"Casa-5G", "wireless_5g_current_channel":149,
                        "access_devices_wired":[{"macaddr":"AA-BB", "ipaddr":"192.168.0.2", "hostname":"TV"}]
                        }""",
                    ),
                deviceInfo = JSONObject("""{"model":"Archer C6","firmware_version":"1.1.10"}"""),
                topology = JSONObject("""{"mesh_nclient_list":[{"mac":"CC-DD","ip":"192.168.0.3","hostname":"Phone","wire_type":"wifi"}]}"""),
                capturedAtEpochMs = 42L,
            )!!

        assertEquals(DeviceType.ROUTER, snapshot.deviceType)
        assertNull(snapshot.fiber)
        assertTrue(snapshot.capabilities.suportaWan)
        assertTrue(snapshot.capabilities.suportaWifi)
        assertEquals(
            "Casa",
            snapshot.wifi!!
                .radios
                .first()
                .ssid,
        )
        assertEquals(2, snapshot.clientes.size)
        assertFalse(snapshot.toString().contains("never-copy-this"))
    }

    @Test
    fun `accepts A6 v2 alias and rejects unrelated models`() {
        val status = JSONObject("""{"lan_ipv4_ipaddr":"192.168.0.1"}""")
        assertTrue(
            TpLinkArcherMapper.map(status, JSONObject("""{"model":"Archer A6 v2"}"""), null, 1) != null,
        )
        assertNull(TpLinkArcherMapper.map(status, JSONObject("""{"model":"Archer C20"}"""), null, 1))
    }
}
