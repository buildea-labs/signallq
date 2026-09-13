package io.signallq.app.feature.router

import org.junit.Assert.assertEquals
import org.junit.Test

class TpLinkStokLuciClientTest {
    @Test
    fun `probe confirms stok bootstrap without submitting a password`() {
        val modulus1024 = "a".repeat(256)
        val modulus512 = "b".repeat(128)
        val transport =
            RecordingTransport(
                listOf(
                    """{"success":true,"data":{"password":["$modulus1024","010001"]}}""",
                    """{"success":true,"data":{"key":["$modulus512","010001"],"seq":10}}""",
                ),
            )

        assertEquals(TpLinkProbeResult.STOK_LUCI_PASSWORD_ONLY, TpLinkStokLuciClient(transport).probe())
        assertEquals(listOf("operation=read", "operation=read"), transport.bodies)
    }

    private class RecordingTransport(
        private val responses: List<String>,
    ) : TpLinkHttpTransport {
        val bodies = mutableListOf<String>()

        override fun post(
            path: String,
            body: String,
            cookie: String?,
        ): TpLinkHttpResponse {
            bodies += body
            return TpLinkHttpResponse(200, responses[bodies.lastIndex], null)
        }
    }
}
