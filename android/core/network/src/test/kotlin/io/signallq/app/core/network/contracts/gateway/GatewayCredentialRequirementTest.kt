package io.signallq.app.core.network.contracts.gateway

import org.junit.Assert.assertEquals
import org.junit.Test

class GatewayCredentialRequirementTest {
    @Test
    fun `Archer C6 pede somente senha`() {
        assertEquals(GatewayCredentialRequirement.PASSWORD_ONLY, credentialRequirementFor("tplink-archer-c6"))
    }

    @Test
    fun `driver desconhecido conserva formulario seguro completo`() {
        assertEquals(GatewayCredentialRequirement.USERNAME_AND_PASSWORD, credentialRequirementFor(null))
    }

    @Test
    fun `Nokia conserva usuario e senha`() {
        assertEquals(GatewayCredentialRequirement.USERNAME_AND_PASSWORD, credentialRequirementFor("nokia-g1425g-b"))
    }
}
