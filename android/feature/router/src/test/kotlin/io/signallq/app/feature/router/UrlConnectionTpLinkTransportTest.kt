package io.signallq.app.feature.router

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlConnectionTpLinkTransportTest {
    @Test
    fun `posts form body preserves session cookie and never follows redirect`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200).setHeader("Set-Cookie", "sysauth=opaque; Path=/").setBody("{}"))
            server.start()
            val transport = UrlConnectionTpLinkTransport("127.0.0.1:${server.port}")
            val response = transport.post("/cgi-bin/luci/;stok=/login?form=keys", "operation=read", "old")
            val request = server.takeRequest()
            assertEquals("/cgi-bin/luci/;stok=/login?form=keys", request.path)
            assertEquals("operation=read", request.body.readUtf8())
            assertEquals("sysauth=old", request.getHeader("Cookie"))
            assertEquals("opaque", response.sysauth)
            assertTrue(response.code in 200..299)
        }
    }
}
